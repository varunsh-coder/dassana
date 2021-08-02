package app.dassana.core.launch;

import app.dassana.core.contentmanager.ContentManagerApi;
import app.dassana.core.contextualize.ContextualizeWorkflowRunner;
import app.dassana.core.contextualize.model.ContextWorkflow;
import app.dassana.core.launch.model.Message;
import app.dassana.core.launch.model.ProcessingResponse;
import app.dassana.core.launch.model.RequestConfig;
import app.dassana.core.launch.model.Timing;
import app.dassana.core.launch.model.severity;
import app.dassana.core.normalize.Normalize;
import app.dassana.core.normalize.model.NormalizationResult;
import app.dassana.core.resource.ResourceWorkflowRunner;
import app.dassana.core.resource.model.ResourcePriorityWorkflow;
import app.dassana.core.risk.TrueRisk;
import app.dassana.core.util.StringyThings;
import app.dassana.core.workflow.StepRunnerApi;
import app.dassana.core.workflow.model.Step;
import app.dassana.core.workflow.model.WorkflowOutputWithRisk;
import io.micronaut.core.util.StringUtils;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
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
  @Inject private ResourceWorkflowRunner resourceWorkFlowRunner;
  @Inject private ContextualizeWorkflowRunner contextualizeWorkflowRunner;
  @Inject private Normalize normalize;
  @Inject private StepRunnerApi stepRunnerApi;
  @Inject private ContentManagerApi contentManagerApi;

  int cores = Runtime.getRuntime().availableProcessors();
  ExecutorService executorService = Executors.newFixedThreadPool(cores);
  CompletionService<WorkflowOutputWithRisk> executorCompletionService = new ExecutorCompletionService<>(
      executorService);

  private static final Logger logger = LoggerFactory.getLogger(Handler.class);


  /**
   * This method is the actual processor of alerts. It is completely free of how the alert has been sent to Dassana.
   * Currently, this handler is hooked to two entry points - the API endpoint which is used for debugging and one hooked
   * to sqs reader. Make sure that this method remains free how the alert was sent to Dassana
   *
   * @param requestConfig this object represents the encapsulated alert.
   * @return Processing response which currently has only one String member - decoratedJson. This JSON is essentially
   * the original alert sent in the {@link RequestConfig#getInputJson()} decorated with the "dassana" object which
   * represents everything Dassana did
   * @throws Exception in case of any error thrown by downstream components. Currently, we do not handle downstream
   *                   exceptions so if there is any error in any phase- be it normalization, resource prioritization,
   *                   contextualization etc, we throw the exception. It is the responsibility of the alert ingestion
   *                   component to handle exceptions. For example, in case of API endpoint {@link ApiHandler}, we set
   *                   the status code to 500. In case of SQS handler {@link Handler}, we send the alert to dead letter
   *                   queue
   */
  ProcessingResponse processRequest(final RequestConfig requestConfig) throws Exception {

    requestConfig.setRequestArrivalTimeMs(System.currentTimeMillis());

    ProcessingResponse processingResponse = new ProcessingResponse();

    logger.info("Processing input {}", StringyThings.removeNewLines(requestConfig.getInputJson()));

    NormalizationResult normalizationResult = normalize.runNormalizers(requestConfig);

    var resourcePriorityResult = new WorkflowOutputWithRisk();
    resourcePriorityResult.setWorkflowUsed(new ResourcePriorityWorkflow());
    resourcePriorityResult.setTimeTakenMs(0L);
    var contextualizationResult = new WorkflowOutputWithRisk();
    contextualizationResult.setWorkflowUsed(new ContextWorkflow());
    contextualizationResult.setTimeTakenMs(0L);

    Future<WorkflowOutputWithRisk> resourcePriorityResultFuture;
    Future<WorkflowOutputWithRisk> contextualizationResultFuture;

    List<Future<WorkflowOutputWithRisk>> futureList = new LinkedList<>();

    //if normalization is done, we run the resource prioritization and contextualization in parallel
    NormalizationResult finalNormalizationResult;

    if (StringUtils.isNotEmpty(normalizationResult.getNormalizerWorkflowUsed().getId())) {
      if (!requestConfig.isSkipResourcePrioritization()) {

        finalNormalizationResult = normalizationResult;
        resourcePriorityResultFuture = executorCompletionService
            .submit(() -> resourceWorkFlowRunner.getResourceRisk(finalNormalizationResult, requestConfig));
        futureList.add(resourcePriorityResultFuture);

      }
      if (!requestConfig.isSkipResourceContextualization()) {
        contextualizationResultFuture = executorCompletionService
            .submit(() -> contextualizeWorkflowRunner.getContext(normalizationResult, requestConfig));
        futureList.add(contextualizationResultFuture);
      }

      for (Future<WorkflowOutputWithRisk> ignored : futureList) {
        try {
          WorkflowOutputWithRisk workflowOutputWithRisk = executorCompletionService.take().get();
          if (workflowOutputWithRisk != null && workflowOutputWithRisk.getWorkflowUsed() instanceof ContextWorkflow) {
            contextualizationResult = workflowOutputWithRisk;
          } else if (workflowOutputWithRisk != null && workflowOutputWithRisk
              .getWorkflowUsed() instanceof ResourcePriorityWorkflow) {
            resourcePriorityResult = workflowOutputWithRisk;
          }

        } catch (InterruptedException | ExecutionException e) {
          throw new RuntimeException(e);
        }
      }
    }

    String dassanaDecoratedJson =
        getDassanaDecoratedJson(requestConfig,
            normalizationResult,
            contextualizationResult,
            resourcePriorityResult);

    //after alerts have been processed we upload the alert to a
    // s3 bucket. This allows any post processors to access the fully decorated json object.
    if (!requestConfig.isSkipS3Upload() && StringUtils
        .isNotEmpty(normalizationResult.getNormalizerWorkflowUsed().getId())) {

      String path = uploadedToS3(normalizationResult, resourcePriorityResult, contextualizationResult,
          dassanaDecoratedJson);
      //update decorated json with the s3 path
      String finalJson;

      if (requestConfig.isSkipS3Upload()) {
        finalJson = dassanaDecoratedJson;
      } else {
        finalJson = updateDecoratedJsonWithS3Path(dassanaDecoratedJson, path, dassanaBucket);
      }

      //we now run post processors of the normalization workflow, if any.
      Map<String, Object> stepIdToResponse = new HashMap<>();
      if (!requestConfig.isSkipPostProcessor() && StringUtils
          .isNotEmpty(normalizationResult.getNormalizerWorkflowUsed().getId())) {
        List<Step> postProcessorSteps = normalizationResult.getNormalizerWorkflowUsed().getPostProcessorSteps();
        for (Step step : postProcessorSteps) {
          String stepOutput = stepRunnerApi
              .runStep(normalizationResult.getNormalizerWorkflowUsed(), step, finalJson,
                  normalizationResult.getNormalizedWorkFlowOutputWithId()).getResponse();
          stepIdToResponse.put(step.getId(), new JSONObject(stepOutput));
          logger.info("Post processor {} response :{}", step.getId(), stepOutput);
        }
        JSONObject finalJsonObj = new JSONObject(finalJson);
        JSONObject normalizeJsonObj = finalJsonObj.getJSONObject("dassana").getJSONObject("normalize");
        normalizeJsonObj.put("post-processor", stepIdToResponse);

        JSONObject dassana = finalJsonObj.getJSONObject("dassana");
        dassana.put("normalize", normalizeJsonObj);

        dassanaDecoratedJson = finalJsonObj.toString();
      }

      if (normalizationResult.getNormalizerWorkflowUsed().isOutputQueueEnabled() && requestConfig.isQueueProcessing()) {
        sqsClient.sendMessage(SendMessageRequest.builder().
            queueUrl(dassanaOutboundQueue).
            messageBody(dassanaDecoratedJson).build());

      }
    } else if (requestConfig.isQueueProcessing()) {
      sqsClient.sendMessage(SendMessageRequest.builder().
          queueUrl(dassanaOutboundQueue).
          messageBody(dassanaDecoratedJson).build());

    }

    processingResponse.setDecoratedJson(dassanaDecoratedJson);
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
      NormalizationResult normalizationResult,
      WorkflowOutputWithRisk resourcePriorityResult,
      WorkflowOutputWithRisk contextualizationResult,
      String dassanaDecoratedJson) {

    var contextWorkflow = (ContextWorkflow) contextualizationResult.getWorkflowUsed();
    var resourcePriorityWorkflow = (ResourcePriorityWorkflow) resourcePriorityResult
        .getWorkflowUsed();

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

    String path;
    if (StringUtils.isEmpty(normalizationResult.getNormalizerWorkflowUsed().getId())) {
      path = alertsPrefix.concat("/unprocessed/").concat(UUID.randomUUID().toString());
    } else {
      String alertId = normalizationResult.getNormalizedWorkFlowOutputWithId().getAlertId();

      path = alertsPrefix.concat(
          "/csp=").concat(normalizationResult.getNormalizedWorkFlowOutputWithId().getCsp()).concat(
          "/resource_container=")
          .concat(normalizationResult.getNormalizedWorkFlowOutputWithId().getResourceContainer()).concat(
              "/region=").concat(normalizationResult.getNormalizedWorkFlowOutputWithId().getRegion()).concat(
              "/service=").concat(normalizationResult.getNormalizedWorkFlowOutputWithId().getService()).concat(
              "/resource_priority=").concat(resourcePriorityResult.getRisk().getRiskValue())
          .concat(
              "/contextual_risk=").concat(contextualizationResult.getRisk().getRiskValue()).concat(
              "/contextual_risk_cat=").concat(contextWorkflow.getSubCategory()).concat(
              "/contextual_risk_sub_cat=").concat(contextWorkflow.getSubCategory())
          .concat(
              "/normalizer=").concat(normalizationResult.getNormalizerWorkflowUsed().getId()).concat(
              "/resource_prioritizer=").concat(resourcePriorityWorkflow.getId()).concat(
              "/context_policy=").concat(contextWorkflow.getId()).concat(
              "/alertid=").concat(alertId);
    }

    PutObjectRequest putObjectRequest = PutObjectRequest.builder().bucket(dassanaBucket).
        key(path).
        build();

    s3Client.putObject(putObjectRequest, RequestBody.fromBytes(dassanaDecoratedJson.getBytes()));

    return path;


  }

  //todo: refactor to make it readable and maintainable
  private String getDassanaDecoratedJson(RequestConfig requestConfig,
      NormalizationResult normalizationResult,
      WorkflowOutputWithRisk contextualizationResult,
      WorkflowOutputWithRisk resourcePriorityResult) {

    ContextWorkflow contextWorkflow = (ContextWorkflow) contextualizationResult.getWorkflowUsed();

    //put the output back in original data
    var messageBody = new JSONObject(requestConfig.getInputJson());
    Map<String, Object> dassanaMap = new HashMap<>();
    Timing timing = new Timing();

    //handle normalization decoration
    if (StringUtils.isNotEmpty(normalizationResult.getNormalizerWorkflowUsed().getId())) {
      dassanaMap.put("normalize", normalizationResult.getNormalizedWorkFlowOutputWithId());
      timing.setNormalize(normalizationResult.getTimeTaken());

      if (StringUtils.isNotEmpty(resourcePriorityResult.getWorkflowUsed().getId())) {
        JSONObject resourcePriorityOutput = new JSONObject();
        resourcePriorityOutput.put("output", resourcePriorityResult.getWorkflowOutput());
        dassanaMap.put("resourcePriority", resourcePriorityOutput);
      } else {
        Message message = new Message();
        message.setSeverity(severity.WARN);
        message.setMsg("No resource priority workflows were run, ensure that the filter is correctly set in the "
            + "workflow that you intended ro run");

        List<Message> messages = new LinkedList<>();
        messages.add(message);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("hints", messages);
        dassanaMap.put("resourcePriority", jsonObject);

      }

      if (StringUtils.isNotEmpty(contextualizationResult.getWorkflowUsed().getId())) {
        timing.setContextualization(contextualizationResult.getTimeTakenMs());
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("workflowId", contextWorkflow.getId());
        jsonObject.put("category", contextWorkflow.getCategory());
        jsonObject.put("subCategory", contextWorkflow.getSubCategory());
        jsonObject.put("output", contextualizationResult.getWorkflowOutput());
        dassanaMap.put("context", jsonObject);

      } else {
        JSONObject jsonObject = new JSONObject();
        List<Message> messages = new LinkedList<>();

        Message message = new Message();
        if (!requestConfig.isSkipResourceContextualization()) {
          message.setSeverity(severity.WARN);
          message.setMsg("No contextualization workflows were run, check filter config of the workflow you intended to "
              + "run");
        }
        messages.add(message);

        if (messages.size() > 0) {
          jsonObject.put("hints", messages);
        }
        dassanaMap.put("context", jsonObject);

      }

      TrueRisk trueRisk = new TrueRisk();
      trueRisk.setResourcePriority(resourcePriorityResult.getRisk());
      trueRisk.setContextualRisk(contextualizationResult.getRisk());

      timing.setResourcePrioritization(resourcePriorityResult.getTimeTakenMs());

      JSONObject jsonObjectForRisk = new JSONObject(trueRisk);
      List<Message> messages = new LinkedList<>();
      if (StringUtils.isNotEmpty(contextualizationResult.getWorkflowUsed().getId()) && StringUtils
          .isEmpty(contextualizationResult.getRisk().getRiskValue())) {
        Message message = new Message();
        message.setSeverity(severity.WARN);
        message
            .setMsg("Contextual Risk couldn't be determined. Ensure that risk-config of the workflow is correctly set");
        messages.add(message);
      }
      if (messages.size() > 0) {
        jsonObjectForRisk.put("hints", messages);
      }

      dassanaMap.put("risk", jsonObjectForRisk);

      timing.setTotalTimeTaken(System.currentTimeMillis() - requestConfig.getRequestArrivalTimeMs());
      dassanaMap.put("timing", timing);
      messageBody.put("dassana", dassanaMap);


    } else {
      Message message = new Message();
      message.setSeverity(severity.WARN);
      message.setMsg("No normalizers were run, ensure that the filter is correctly set in the workflow that you "
          + "intended ro run");
      List<Message> messages = new LinkedList<>();
      messages.add(message);
      JSONObject jsonObject = new JSONObject();
      jsonObject.put("hints", messages);
      dassanaMap.put("normalize", jsonObject);
    }

    return messageBody.toString();


  }


}
