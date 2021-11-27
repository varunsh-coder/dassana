package app.dassana.core.runmanager.launch.handler;

import static app.dassana.core.runmanager.contentmanager.ContentManager.GENERAL_CONTEXT;
import static app.dassana.core.runmanager.contentmanager.ContentManager.POLICY_CONTEXT;
import static app.dassana.core.runmanager.contentmanager.ContentManager.RESOURCE_CONTEXT;

import app.dassana.core.runmanager.contentmanager.ContentManagerApi;
import app.dassana.core.runmanager.workflow.S3WorkflowManager;
import app.dassana.core.runmanager.launch.model.ProcessingResponse;
import app.dassana.core.runmanager.launch.model.Request;
import app.dassana.core.workflowmanager.model.normalize.NormalizerWorkflow;
import app.dassana.core.workflowmanager.model.policycontext.PolicyContext;
import app.dassana.core.workflowmanager.model.resource.GeneralContext;
import app.dassana.core.workflowmanager.model.resource.ResourceContext;
import app.dassana.core.runmanager.util.StringyThings;
import app.dassana.core.runmanager.workflow.WorkflowRunner;
import app.dassana.core.workflowmanager.workflow.model.Workflow;
import app.dassana.core.workflowmanager.workflow.model.WorkflowOutputWithRisk;
import app.dassana.core.runmanager.workflow.Decorator;
import app.dassana.core.runmanager.workflow.PostProcessor;
import io.micronaut.core.util.StringUtils;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This is the most important class- it is responsible for processing alert end to end.
 */
@Singleton
public class RequestProcessor {

  @Inject private WorkflowRunner workflowRunner;
  @Inject private ContentManagerApi contentManagerApi;
  @Inject private Decorator decorator;
  @Inject private PostProcessor postProcessor;
  @Inject private S3WorkflowManager s3WorkflowManager;
  @Inject private EventBridgeHandler eventBridgeHandler;


  int cores = Runtime.getRuntime().availableProcessors();
  ExecutorService executorService = Executors.newFixedThreadPool(cores);
  CompletionService<Optional<WorkflowOutputWithRisk>> executorCompletionService = new ExecutorCompletionService<>(
      executorService);

  private static final Logger logger = LoggerFactory.getLogger(RequestProcessor.class);


  public void setWorkflows(final Request request) throws Exception {

    Set<Workflow> workflowSet = contentManagerApi.getWorkflowSet(request);
    request.setWorkflowSetToRun(workflowSet);

    //if a workflow id is provided, we run only that workflow and nothing else
    if (StringUtils.isNotEmpty(request.getWorkflowId())) {
      for (Workflow workflow : workflowSet) {
        if (workflow.getId().contentEquals(request.getWorkflowId())) {
          Set<Workflow> workflowSet1 = new HashSet<>();
          workflowSet1.add(workflow);
          request.setWorkflowSetToRun(workflowSet1);
          break;
        }
      }

    }
    Map<String, Workflow> workflowIdToWorkflowMap = new HashMap<>();
    for (Workflow workflow1 : request.getWorkflowSetToRun()) {
      workflowIdToWorkflowMap.put(workflow1.getId(), workflow1);
    }
    request.setWorkflowIdToWorkflowMap(workflowIdToWorkflowMap);

  }


  /**
   * This method is the actual processor of alerts. It is completely free of how the alert has been sent to Dassana.
   * Currently, this handler is hooked to two entry points - the API endpoint which is used for debugging and one hooked
   * to sqs reader. Make sure that this method remains free how the alert was sent to Dassana
   *
   * @param request this object represents the encapsulated alert.
   * @return Processing response which currently has only one String member - decoratedJson. This JSON is essentially
   * the original alert sent in the {@link Request#getInputJson()} decorated with the "dassana" object which represents
   * everything Dassana did
   */
  public ProcessingResponse processRequest(final Request request) throws Exception {

    setWorkflows(request);
    ProcessingResponse processingResponse = new ProcessingResponse(request);

    logger.info("Processing input {}", StringyThings.removeNewLines(request.getInputJson()));

    Optional<WorkflowOutputWithRisk> normalizationResult;
    Optional<WorkflowOutputWithRisk> policyContext = Optional.empty();
    Optional<WorkflowOutputWithRisk> resourceContext = Optional.empty();
    Optional<WorkflowOutputWithRisk> generalContext = Optional.empty();

    normalizationResult = workflowRunner
        .runWorkFlow(NormalizerWorkflow.class, request, "");

    List<Future<Optional<WorkflowOutputWithRisk>>> futureList = new LinkedList<>();

    //if normalization is done, we run the resource prioritization and contextualization in parallel

    if (normalizationResult.isPresent()) {
      WorkflowOutputWithRisk normalizedWorkflowOutput = normalizationResult.get();

      String normalizedWorkflowJsonStr = new JSONObject(normalizedWorkflowOutput.getOutput()).toString();

      String normalizerId = normalizedWorkflowOutput.getWorkflowId();

      futureList.add(executorCompletionService
          .submit(() ->
              workflowRunner
                  .runWorkFlow(GeneralContext.class, request, normalizedWorkflowJsonStr)));

      futureList.add(executorCompletionService
          .submit(() ->
              workflowRunner
                  .runWorkFlow(ResourceContext.class, request, normalizedWorkflowJsonStr)));

      futureList.add(executorCompletionService
          .submit(() ->
              workflowRunner
                  .runWorkFlow(PolicyContext.class, request, normalizedWorkflowJsonStr)));

      for (Future<Optional<WorkflowOutputWithRisk>> ignored : futureList) {

        Optional<WorkflowOutputWithRisk> workflowOutputWithRisk = executorCompletionService.take().get();

        if (workflowOutputWithRisk.isPresent()) {
          String workflowId = workflowOutputWithRisk.get().getWorkflowId();
          for (Workflow workflow : request.getWorkflowSetToRun()) {
            if (workflow.getId().contentEquals(workflowId)) {
              if (workflow.getType().contentEquals(POLICY_CONTEXT)) {
                policyContext = workflowOutputWithRisk;
              } else if (workflow.getType().contentEquals(GENERAL_CONTEXT)) {
                generalContext = workflowOutputWithRisk;
              } else if (workflow.getType().contentEquals(RESOURCE_CONTEXT)) {
                resourceContext = workflowOutputWithRisk;
              }
            }
          }

        }
      }

      String dassanaDecoratedJson =
          decorator.getDassanaDecoratedJson(request,
              normalizationResult.get(),
              policyContext,
              resourceContext,
              generalContext);

      //after alerts have been processed we upload the alert to a
      // s3 bucket. This allows any post processors to access the fully decorated json object.

      String workflowId = normalizationResult.get().getWorkflowId();
      NormalizerWorkflow workflow = (NormalizerWorkflow) request.getWorkflowIdToWorkflowMap().get(workflowId);
      String decoratedJsonWithS3key = s3WorkflowManager.uploadedToS3(normalizationResult, dassanaDecoratedJson);
      if (workflow.getPostProcessorSteps().size() > 0) {

        String finalJson = postProcessor
            .handlePostProcessor(request, normalizationResult.get(), decoratedJsonWithS3key);
        processingResponse.setDecoratedJson(finalJson);
      } else {
        processingResponse.setDecoratedJson(dassanaDecoratedJson);
      }

      eventBridgeHandler.handleEventBridge(processingResponse, normalizerId);

    } else {//normalization did not happen
      processingResponse.setNormalizerWorkflow(null);
      processingResponse.setDecoratedJson(request.getInputJson());
      eventBridgeHandler.handleEventBridge(processingResponse, null);
    }

    return processingResponse;

  }

}
