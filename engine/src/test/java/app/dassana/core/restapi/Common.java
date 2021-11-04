package app.dassana.core.restapi;

import app.dassana.core.client.infra.S3Store;
import app.dassana.core.contentmanager.infra.S3WorkflowManager;
import app.dassana.core.launch.model.ProcessingResponse;
import app.dassana.core.launch.model.Request;
import app.dassana.core.workflow.StepRunnerApi;
import app.dassana.core.workflow.infra.LambdaStepRunner;
import app.dassana.core.workflow.model.Step;
import app.dassana.core.workflow.model.StepRunResponse;
import app.dassana.core.workflow.model.Workflow;
import app.dassana.core.workflow.model.WorkflowOutputWithRisk;
import app.dassana.core.workflow.processor.EventBridgeHandler;
import app.dassana.core.workflow.processor.PostProcessor;
import io.micronaut.context.annotation.Replaces;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import javax.inject.Singleton;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

public class Common {


  @Singleton
  @Replaces(S3WorkflowManager.class)
  public static class FakeS3WorkflowManager extends S3WorkflowManager {

    public FakeS3WorkflowManager(S3Store s3Store) {
      super(s3Store);
    }

    @Override
    protected String getPath(Optional<WorkflowOutputWithRisk> normalizationResult) {
      return "alert/foo";
    }
  }


  @Singleton
  @Replaces(S3Store.class)
  public static class FakeS3Store extends S3Store {

    private final Map<String, Object> inMemStore = new HashMap<>();
    private long lastUpdated = 0L;

    @Override
    public Long getLastUpdatedValueFromS3() {
      return lastUpdated;
    }

    @Override
    public List<String> getAllContent() {
      List<String> objects = new LinkedList<>();
      for (Entry<String, Object> entry : inMemStore.entrySet()) {
        String s = entry.getKey();
        Object o = entry.getValue();
        if (!s.startsWith("alert/")) {
          objects.add(o.toString());
        }

      }
      return objects;
    }

    @Override
    public void upload(String key, String body) {
      inMemStore.put(key, body);
      lastUpdated = System.currentTimeMillis();
    }

    @Override
    public void delete(String key) {
      inMemStore.remove(key);
      lastUpdated = System.currentTimeMillis();
    }
  }


  @Singleton
  @Replaces(PostProcessor.class)
  public static class FakePostProcessor extends PostProcessor {

    @Override
    public String handlePostProcessor(Request request, WorkflowOutputWithRisk normalizationResult,
        String processedAlertWithS3key) throws Exception {
      return processedAlertWithS3key;
    }
  }


  @Singleton
  @Replaces(EventBridgeHandler.class)
  public static class FakeEventBridgeHandler extends EventBridgeHandler {

    @Override
    public void handleEventBridge(ProcessingResponse processingResponse, String normalizerId) {
    }
  }

  @Singleton
  @Replaces(LambdaStepRunner.class)
  public static class MockLambdaStepRunner implements StepRunnerApi {

    public MockLambdaStepRunner() {
    }


    @Override
    public StepRunResponse runStep(Workflow workflow, Step step, String inputJson,
        String simpleOutputJsonStr) throws Exception {

      StepRunResponse stepRunResponse = new StepRunResponse();
      if (step.getId().contentEquals("resource-info")) {

        //not the best solution but here we simulate the dynamic nature of functions, i.e. based on the input, we
        // generate the response from lambda function

        JSONObject jsonObject = new JSONObject(inputJson);
        JSONObject testHintJsonObj = jsonObject.optJSONObject("test-hint");

        if (testHintJsonObj != null) {
          String resInfo = testHintJsonObj.optString("resource-info");
          if (StringUtils.isNotBlank(resInfo)) {
            stepRunResponse.setResponse(IOUtils.toString(
                Thread.currentThread().getContextClassLoader().getResourceAsStream(resInfo), Charset.defaultCharset()));
          }
        }


      }

      if (step.getId().contentEquals("getTags")) {
        stepRunResponse.setResponse(IOUtils.toString(
            Thread.currentThread().getContextClassLoader().getResourceAsStream("responses/tags.json"),
            Charset.defaultCharset()));
      }

      if (step.getId().contentEquals("list-of-attached-eni")) {
        stepRunResponse.setResponse(IOUtils.toString(
            Thread.currentThread().getContextClassLoader().getResourceAsStream("responses/list-of-attached-eni.json"),
            Charset.defaultCharset()));
      }

      if (step.getId().contentEquals("lookup")) {
        stepRunResponse.setResponse(IOUtils.toString(
            Thread.currentThread().getContextClassLoader().getResourceAsStream("responses/lookup.json"),
            Charset.defaultCharset()));
      }

      if (step.getId().contentEquals("ec2Exposure")) {
        stepRunResponse.setResponse(IOUtils.toString(
                Thread.currentThread().getContextClassLoader().getResourceAsStream("responses/ec2-exposure.json"),
                Charset.defaultCharset()));
      }

      if (step.getId().contentEquals("riskyPermissions")) {
        stepRunResponse.setResponse(IOUtils.toString(
                Thread.currentThread().getContextClassLoader().getResourceAsStream("responses/risky-permissions.json"),
                Charset.defaultCharset()));
      }

      return stepRunResponse;
    }

  }

}
