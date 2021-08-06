package app.dassana.core.workflow;

import static app.dassana.core.contentmanager.ContentManager.GENERAL_CONTEXT;
import static app.dassana.core.contentmanager.ContentManager.NORMALIZE;
import static app.dassana.core.contentmanager.ContentManager.POLICY_CONTEXT;
import static app.dassana.core.contentmanager.ContentManager.POLICY_CONTEXT_CAT;
import static app.dassana.core.contentmanager.ContentManager.POLICY_CONTEXT_SUB_CAT;

import app.dassana.core.contentmanager.ContentManagerApi;
import app.dassana.core.launch.ApiHandler;
import app.dassana.core.launch.Handler;
import app.dassana.core.launch.model.ProcessingResponse;
import app.dassana.core.launch.model.Request;
import app.dassana.core.normalize.model.NormalizerWorkflow;
import app.dassana.core.policycontext.model.PolicyContext;
import app.dassana.core.resource.model.GeneralContext;
import app.dassana.core.risk.CombinedRisk;
import app.dassana.core.util.StringyThings;
import app.dassana.core.workflow.model.Step;
import app.dassana.core.workflow.model.Workflow;
import app.dassana.core.workflow.model.WorkflowOutputWithRisk;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;


/**
 * This is the most important class- it is responsible for processing alert end to end.
 */
@Singleton
public class RequestProcessor {


  private final String dassanaOutboundQueue = System.getenv("dassanaOutboundQueue");
  private final String dassanaBucket = System.getenv().get("dassanaBucket");

  @Inject private SqsClient sqsClient;
  @Inject private S3Client s3Client;
  @Inject private WorkflowRunner workflowRunner;
  @Inject private StepRunnerApi stepRunnerApi;
  @Inject private ContentManagerApi contentManagerApi;

  int cores = Runtime.getRuntime().availableProcessors();
  ExecutorService executorService = Executors.newFixedThreadPool(cores);
  CompletionService<Optional<WorkflowOutputWithRisk>> executorCompletionService = new ExecutorCompletionService<>(
      executorService);

  private static final Logger logger = LoggerFactory.getLogger(Handler.class);


  /**
   * This method is the actual processor of alerts. It is completely free of how the alert has been sent to Dassana.
   * Currently, this handler is hooked to two entry points - the API endpoint which is used for debugging and one hooked
   * to sqs reader. Make sure that this method remains free how the alert was sent to Dassana
   *
   * @param request this object represents the encapsulated alert.
   * @return Processing response which currently has only one String member - decoratedJson. This JSON is essentially
   * the original alert sent in the {@link Request#getInputJson()} decorated with the "dassana" object which represents
   * everything Dassana did
   * @throws Exception in case of any error thrown by downstream components. Currently, we do not handle downstream
   *                   exceptions so if there is any error in any phase- be it normalization, resource prioritization,
   *                   contextualization etc, we throw the exception. It is the responsibility of the alert ingestion
   *                   component to handle exceptions. For example, in case of API endpoint {@link ApiHandler}, we set
   *                   the status code to 500. In case of SQS handler {@link Handler}, we send the alert to dead letter
   *                   queue
   */
  public ProcessingResponse processRequest(final Request request) throws Exception {

    ProcessingResponse processingResponse = new ProcessingResponse();

    logger.info("Processing input {}", StringyThings.removeNewLines(request.getInputJson()));

    Optional<WorkflowOutputWithRisk> normalizationResult;
    Optional<WorkflowOutputWithRisk> policyContext = Optional.empty();
    Optional<WorkflowOutputWithRisk> generalContext = Optional.empty();

    normalizationResult = workflowRunner
        .runWorkFlow(NormalizerWorkflow.class, request, new HashMap<>());

    List<Future<Optional<WorkflowOutputWithRisk>>> futureList = new LinkedList<>();

    //if normalization is done, we run the resource prioritization and contextualization in parallel

    if (normalizationResult.isPresent()) {
      WorkflowOutputWithRisk normalizedWorkflowOutput = normalizationResult.get();

      normalizedWorkflowOutput.getSimpleOutput().put("workflowId", normalizedWorkflowOutput.getWorkflowId());

      if (!request.isSkipGeneralContext()) {
        futureList.add(executorCompletionService
            .submit(() ->
                workflowRunner
                    .runWorkFlow(GeneralContext.class, request, normalizedWorkflowOutput.getSimpleOutput())));
      }

      if (!request.isSkipPolicyContext()) {
        futureList.add(executorCompletionService
            .submit(() ->
                workflowRunner
                    .runWorkFlow(PolicyContext.class, request, normalizedWorkflowOutput.getSimpleOutput())));
      }

      for (Future<Optional<WorkflowOutputWithRisk>> ignored : futureList) {

        Optional<WorkflowOutputWithRisk> workflowOutputWithRisk = executorCompletionService.take().get();

        if (workflowOutputWithRisk.isPresent()) {
          String workflowId = workflowOutputWithRisk.get().getWorkflowId();
          for (Workflow workflow : contentManagerApi.getWorkflowSet(request)) {
            if (workflow.getId().contentEquals(workflowId)) {
              if (workflow.getType().contentEquals(POLICY_CONTEXT)) {
                policyContext = workflowOutputWithRisk;
              } else if (workflow.getType().contentEquals(GENERAL_CONTEXT)) {
                generalContext = workflowOutputWithRisk;
              }
            }
          }

        }
      }

      String dassanaDecoratedJson =
          getDassanaDecoratedJson(request, normalizationResult.get(), policyContext, generalContext);

      //after alerts have been processed we upload the alert to a
      // s3 bucket. This allows any post processors to access the fully decorated json object.
      if (!request.isSkipS3Upload()) {

        NormalizerWorkflow normalizerWorkflow = (NormalizerWorkflow) contentManagerApi
            .getWorkflowIdToWorkflowMap(request)
            .get(normalizationResult.get().getWorkflowId());

        String path = uploadedToS3(request, normalizationResult, dassanaDecoratedJson);
        //update decorated json with the s3 path
        String finalJson;

        if (request.isSkipS3Upload()) {
          finalJson = dassanaDecoratedJson;
        } else {
          finalJson = updateDecoratedJsonWithS3Path(dassanaDecoratedJson, path, dassanaBucket);
        }

        //we now run post processors of the normalization workflow, if any.
        Map<String, Object> stepIdToResponse = new HashMap<>();
        if (!request.isSkipPostProcessor()) {
          List<Step> postProcessorSteps = normalizerWorkflow.getPostProcessorSteps();
          for (Step step : postProcessorSteps) {
            String stepOutput = stepRunnerApi
                .runStep(normalizerWorkflow,
                    step,
                    finalJson,
                    normalizationResult.get().getSimpleOutput())
                .getResponse();
            stepIdToResponse.put(step.getId(), new JSONObject(stepOutput));
            logger.info("Post processor {} response :{}", step.getId(), stepOutput);
          }
          JSONObject finalJsonObj = new JSONObject(finalJson);
          JSONObject normalizeJsonObj = finalJsonObj.getJSONObject("dassana").getJSONObject(NORMALIZE);
          normalizeJsonObj.put("post-processor", stepIdToResponse);

          JSONObject dassana = finalJsonObj.getJSONObject("dassana");
          dassana.put("normalize", normalizeJsonObj);

          dassanaDecoratedJson = finalJsonObj.toString();
        }

        if (normalizerWorkflow.isOutputQueueEnabled() && request.isQueueProcessing()) {
          sqsClient.sendMessage(SendMessageRequest.builder().
              queueUrl(dassanaOutboundQueue).
              messageBody(dassanaDecoratedJson).build());

        }
      } else if (request.isQueueProcessing()) {
        sqsClient.sendMessage(SendMessageRequest.builder().
            queueUrl(dassanaOutboundQueue).
            messageBody(dassanaDecoratedJson).build());

      }

      processingResponse.setDecoratedJson(dassanaDecoratedJson);

    } else {
      processingResponse.setDecoratedJson(request.getInputJson());
    }

    return processingResponse;

  }


  private String updateDecoratedJsonWithS3Path(String dassanaDecoratedJson, String path,
      String dassanaBucket) {
    String dassanaKey = "dassana";
    JSONObject dassanaDecoratedJsonObj = new JSONObject(dassanaDecoratedJson);
    JSONObject updatedDassanaObj = dassanaDecoratedJsonObj.getJSONObject(dassanaKey)
        .put("alertKey", "s3://".concat(dassanaBucket).concat("/").concat(path));
    dassanaDecoratedJsonObj.put(dassanaKey, updatedDassanaObj);
    return dassanaDecoratedJsonObj.toString();

  }


  private String uploadedToS3(
      Request request,
      Optional<WorkflowOutputWithRisk> normalizationResult,
      String dassanaDecoratedJson) {

    DateTime now = DateTime.now(DateTimeZone.UTC);

    int min = now.minuteOfHour().get();
    int hour = now.hourOfDay().get();
    int day = now.dayOfMonth().get();
    int month = now.monthOfYear().get();
    int year = now.year().get();

    String alertsPrefix = "alerts".concat(
        "/year=".concat(String.valueOf(year)).concat(
            "/month=").concat(String.valueOf(month)).concat(
            "/day=").concat(String.valueOf(day)).concat(
            "/hour=").concat(String.valueOf(hour)).concat(
            "/min=").concat(String.valueOf(min)));

    String path = null;
    if (normalizationResult.isEmpty()) {
      path = alertsPrefix.concat("/unprocessed/").concat(UUID.randomUUID().toString());
    } else {
      WorkflowOutputWithRisk workflowOutputWithRisk = normalizationResult.get();

      path = alertsPrefix.concat(
          "/csp=").concat((String) workflowOutputWithRisk.getSimpleOutput().get("csp")).concat(
          "/resource_container=")
          .concat((String) workflowOutputWithRisk.getSimpleOutput().get("resourceContainer")).concat(
              "/region=").concat((String) workflowOutputWithRisk.getSimpleOutput().get("region")).concat(
              "/service=").concat((String) workflowOutputWithRisk.getSimpleOutput().get("service")).concat(
              "/normalizer_workflow=").concat(workflowOutputWithRisk.getWorkflowId()).concat(
              "/alertid=").concat((String) workflowOutputWithRisk.getSimpleOutput().get("alertId"));


    }

    PutObjectRequest putObjectRequest = PutObjectRequest.builder().bucket(dassanaBucket).
        key(path).
        build();

    s3Client.putObject(putObjectRequest, RequestBody.fromBytes(dassanaDecoratedJson.getBytes()));

    return path;


  }

  //todo: refactor to make it readable and maintainable
  private String getDassanaDecoratedJson(Request request,
      WorkflowOutputWithRisk normalizationOutput,
      Optional<WorkflowOutputWithRisk> policyContextWorkflowOutput,
      Optional<WorkflowOutputWithRisk> generalContextWorkflowOutput) {

    CombinedRisk combinedRisk = new CombinedRisk();

    //put the output back in original data
    var messageBody = new JSONObject(request.getInputJson());
    Map<String, Object> dassanaMap = new HashMap<>();
    //handle normalization decoration
    JSONObject jsonObjectForNormalization = new JSONObject();
    jsonObjectForNormalization.put("output", normalizationOutput.getSimpleOutput());
    jsonObjectForNormalization.put("workflowId", normalizationOutput.getWorkflowId());
    dassanaMap.put("normalize", jsonObjectForNormalization);
    if (generalContextWorkflowOutput.isPresent()) {
      JSONObject generalContextJsonObj = new JSONObject();
      generalContextJsonObj.put("workflowId", generalContextWorkflowOutput.get().getWorkflowId());
      generalContextJsonObj.put("output", generalContextWorkflowOutput.get().getSimpleOutput());
      Map<String, Object> riskObj = new HashMap<>();
      riskObj.put("riskValue", generalContextWorkflowOutput.get().getRisk().getRiskValue());
      riskObj.put("condition", generalContextWorkflowOutput.get().getRisk().getCondition());
      riskObj.put("name", generalContextWorkflowOutput.get().getRisk().getName());
      generalContextJsonObj.put("risk", riskObj);
      dassanaMap.put(GENERAL_CONTEXT, generalContextJsonObj);
      combinedRisk.setGeneralContextRisk(generalContextWorkflowOutput.get().getRisk());
    }

    if (policyContextWorkflowOutput.isPresent()) {
      PolicyContext policyContext = (PolicyContext) contentManagerApi.getWorkflowIdToWorkflowMap(request)
          .get(policyContextWorkflowOutput.get().getWorkflowId());

      JSONObject jsonObject = new JSONObject();
      jsonObject.put("workflowId", policyContext.getId());
      jsonObject.put(POLICY_CONTEXT_CAT, policyContext.getCategory());
      jsonObject.put(POLICY_CONTEXT_SUB_CAT, policyContext.getSubCategory());
      jsonObject.put("output", policyContextWorkflowOutput.get().getSimpleOutput());

      Map<String, Object> riskObj = new HashMap<>();
      riskObj.put("riskValue", policyContextWorkflowOutput.get().getRisk().getRiskValue());
      riskObj.put("condition", policyContextWorkflowOutput.get().getRisk().getCondition());
      riskObj.put("name", policyContextWorkflowOutput.get().getRisk().getName());
      jsonObject.put("risk", riskObj);
      dassanaMap.put(POLICY_CONTEXT, jsonObject);
      combinedRisk.setGeneralContextRisk(policyContextWorkflowOutput.get().getRisk());

    }
    messageBody.put("dassana", dassanaMap);

    if (!request.isIncludeOriginalJson()) {
      messageBody.clear();
      messageBody.put("dassana", dassanaMap);
    }

    return messageBody.toString();


  }


}
