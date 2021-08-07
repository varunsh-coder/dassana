package app.dassana.core.launch;


import static app.dassana.core.contentmanager.ContentManager.GENERAL_CONTEXT;
import static app.dassana.core.contentmanager.ContentManager.NORMALIZE;
import static app.dassana.core.contentmanager.ContentManager.POLICY_CONTEXT;

import app.dassana.core.contentmanager.RemoteContentDownloadApi;
import app.dassana.core.contentmanager.infra.S3Downloader;
import app.dassana.core.workflow.StepRunnerApi;
import app.dassana.core.workflow.infra.LambdaStepRunner;
import app.dassana.core.workflow.model.Step;
import app.dassana.core.workflow.model.StepRunResponse;
import app.dassana.core.workflow.model.Workflow;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.s3.S3Client;

@MicronautTest
public class ApiTest {

  @Inject Helper helper;


  @Test
  void ensureValidJsonIsAlwaysReturned() throws Exception {

    String[] fileNames = new String[]{"inputs/validJsonButNotAnAlert1.json", "inputs/validJsonButNotAnAlert2.json"};

    for (String fileName : fileNames) {
      String response = helper.executeApi(helper.getFileContent(fileName), true);
      JSONObject jsonObject = new JSONObject(response);
      jsonObject.remove("dassana");//this key is always added
      JSONObject jsonObjectInput = new JSONObject(helper.getFileContent(fileName));
      Assertions.assertEquals(jsonObjectInput.toString(), jsonObject.toString());
    }


  }


  @Test()
  void ensureInValidJsonThrowsError() throws IOException {
    boolean expectedExceptionThrown = false;
    try {
      helper.executeApi("foo");
    } catch (RuntimeException exception) {
      Assertions.assertTrue(exception.getMessage().contains("Dassana Engine can"));
      expectedExceptionThrown = true;
    }
    Assertions.assertTrue(expectedExceptionThrown);

  }


  @Test
  void ensureValidAlertGetsProcessed() throws IOException {

    String response = helper.executeApi(helper.getFileContent("inputs/validSecurityHubAlert.json"));
    JSONObject responseJsonObj = new JSONObject(response);
    JSONObject dassana = responseJsonObj.getJSONObject("dassana");
    JSONObject normalize = dassana.getJSONObject(NORMALIZE);
    JSONObject generalContext = dassana.getJSONObject(GENERAL_CONTEXT);
    JSONObject policyContext = dassana.getJSONObject(POLICY_CONTEXT);

    Assertions.assertTrue(normalize.getString("workflowId").contentEquals("aws-config"));
    Assertions.assertTrue(generalContext.getString("workflowId").contentEquals("general-context-aws"));
    Assertions.assertTrue(generalContext.getJSONObject("risk").getString("riskValue").contentEquals("low"));

    Assertions.assertTrue(policyContext.getString("workflowId").contentEquals("security-group-wide-open"));


  }


  @Singleton
  @Replaces(RemoteContentDownloadApi.class)
  public static class FakeS3Downloader extends S3Downloader {


    public FakeS3Downloader(S3Client s3Client) {
      super(s3Client);
    }

    @Override
    public Optional<File> downloadContent(Long lastDownloaded) {
      return Optional.empty();
    }
  }


  @Singleton
  @Replaces(LambdaStepRunner.class)
  public static class MockLambdaStepRunner implements StepRunnerApi {

    public MockLambdaStepRunner() {
    }


    @Override
    public StepRunResponse runStep(Workflow workflow, Step step, String inputJson,
        Map<String, Object> simpleOutput) throws Exception {
      StepRunResponse stepRunResponse = new StepRunResponse();
      if (step.getId().contentEquals("resource-info")) {
        stepRunResponse.setResponse(IOUtils.toString(
            Thread.currentThread().getContextClassLoader().getResourceAsStream("responses/normalization.json"),
            Charset.defaultCharset()));
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

      return stepRunResponse;
    }
  }

}
