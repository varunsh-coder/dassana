package app.dassana.core.launch;


import static app.dassana.core.contentmanager.ContentManager.GENERAL_CONTEXT;
import static app.dassana.core.contentmanager.ContentManager.NORMALIZE;
import static app.dassana.core.contentmanager.ContentManager.POLICY_CONTEXT;
import static app.dassana.core.launch.ApiHandler.MISSING_NORMALIZATION_MSG;

import app.dassana.core.contentmanager.RemoteContentDownloadApi;
import app.dassana.core.contentmanager.infra.S3Downloader;
import app.dassana.core.workflow.StepRunnerApi;
import app.dassana.core.workflow.infra.LambdaStepRunner;
import app.dassana.core.workflow.model.Step;
import app.dassana.core.workflow.model.StepRunResponse;
import app.dassana.core.workflow.model.Workflow;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.StringUtils;
import software.amazon.awssdk.services.s3.S3Client;

@MicronautTest
public class ApiTest {

  @Inject Helper helper;

  @Test
  void testIncludeJson() throws Exception {

    String file = "inputs/validSecurityHubAlert.json";
    Map<String, String> queryParams = helper.getQueryParams(false,
        false,
        false,
        true,
        false);
    String response = helper.executeRunApi(helper.getFileContent(file), queryParams);
    JSONObject jsonObject = new JSONObject(response);
    if (jsonObject.has("dassana")) {
      Assertions.fail("did not expect to see Dassana Key");
    }

    file = "inputs/validJsonButNotAnAlert1.json";
    response = helper.executeRunApi(helper.getFileContent(file), queryParams);

    jsonObject = new JSONObject(response);
    String msg = jsonObject.getString("msg");

    if (!msg.contentEquals(MISSING_NORMALIZATION_MSG)) {
      Assertions.fail("Expected to see missing normalization message");
    }

    file = "inputs/validAlertWithNoGeneralContext.json";
    response = helper.executeRunApi(helper.getFileContent(file), queryParams);

    jsonObject = new JSONObject(response);

    msg = jsonObject.getJSONObject(GENERAL_CONTEXT).getString("msg");
    if (!msg.contains(GENERAL_CONTEXT)) {
      Assertions.fail("Expected to see missing general-context message");
    }

  }

  @Test
  void testValidateApi() throws Exception {

    String executeValidateApi = helper.executeValidateApi(helper.getFileContent(
        "validationRequests/validWorkflow.json"));

    //{"isValidYaml":true,"isValidWorkflowType":true,"invalidRules":[]}
    JSONObject jsonObject = new JSONObject(executeValidateApi);
    Assertions.assertTrue(jsonObject.getJSONObject("validYaml").getBoolean("isValidYaml"));
    Assertions.assertTrue(jsonObject.getJSONObject("validWorkflowType").getBoolean("isValidType"));
    Assertions.assertEquals(0, jsonObject.getJSONArray("invalidRules").length());

    String executeValidateApi1 = helper.executeValidateApi(helper.getFileContent(
        "validationRequests/invalidWorkflowYaml.json"));

    jsonObject = new JSONObject(executeValidateApi1);
    Assertions.assertFalse(jsonObject.getJSONObject("validYaml").getBoolean("isValidYaml"));

    String executeValidateApi2 = helper.executeValidateApi(helper.getFileContent(
        "validationRequests/validWorkflowButWithInvalidRule.json"));

    jsonObject = new JSONObject(executeValidateApi2);
    Assertions.assertTrue(jsonObject.getJSONObject("validYaml").getBoolean("isValidYaml"));
    Assertions.assertEquals(1, jsonObject.getJSONArray("invalidRules").length());

    String executeValidateApi3 = helper.executeValidateApi(helper.getFileContent(
        "validationRequests/invalidWorkflowType.json"));

    jsonObject = new JSONObject(executeValidateApi3);
    Assertions.assertFalse(jsonObject.getJSONObject("validWorkflowType").getBoolean("isValidType"));


  }

  @Test
  void testWorkflowGet() {
    Map<String, String> queryParams = helper.getQueryParams(false, false, false, false, false);
    queryParams.put("workflowId", "foo");
    APIGatewayProxyResponseEvent apiGatewayProxyResponseEvent = helper.executeRunApiGet(queryParams);
    Assertions.assertEquals(404, (int) apiGatewayProxyResponseEvent.getStatusCode());
    queryParams.put("workflowId", "foo-cloud-normalize");
    Assertions.assertEquals(200, (int) helper.executeRunApiGet(queryParams).getStatusCode());


  }


  @Test
  void testDraftWorkflow() throws IOException {
    Map<String, String> queryParams = helper.getQueryParams(false,
        false,
        false,
        true,
        false);
    String response = helper
        .executeRunApi(helper.getFileContent("inputs/validAlertWithDraftWorkflow.json"), queryParams);
    JSONObject jsonObject = new JSONObject(response);
    Assertions.assertTrue(jsonObject.getString("msg").contentEquals(MISSING_NORMALIZATION_MSG));

  }


  @Test
  void ensureValidJsonIsAlwaysReturned() throws Exception {

    String[] fileNames = new String[]{"inputs/validJsonButNotAnAlert1.json", "inputs/validJsonButNotAnAlert2.json"};
    Map<String, String> queryParams = helper.getQueryParams(false,
        false,
        false,
        true,
        true);
    for (String fileName : fileNames) {
      String response = helper.executeRunApi(helper.getFileContent(fileName), queryParams);
      JSONObject jsonObject = new JSONObject(response);
      jsonObject.remove("dassana");//this key is always added
      JSONObject jsonObjectInput = new JSONObject(helper.getFileContent(fileName));
      Assertions.assertEquals(jsonObjectInput.toString(), jsonObject.toString());
    }


  }


  @Test()
  void ensureInValidJsonThrowsError() {
    boolean expectedExceptionThrown = false;
    try {
      helper.executeRunApi("foo", new HashMap<>());
    } catch (RuntimeException exception) {
      Assertions.assertTrue(exception.getMessage().contains("Dassana Engine can"));
      expectedExceptionThrown = true;
    }
    Assertions.assertTrue(expectedExceptionThrown);

  }


  @Test
  void ensureValidAlertGetsProcessed() throws IOException {
    Map<String, String> queryParams = helper.getQueryParams(false,
        false,
        false,
        true,
        true);

    String response = helper.executeRunApi(helper.getFileContent("inputs/validSecurityHubAlert.json"),
        queryParams);
    JSONObject responseJsonObj = new JSONObject(response);
    JSONObject dassana = responseJsonObj.getJSONObject("dassana");
    JSONObject normalize = dassana.getJSONObject(NORMALIZE);
    JSONObject generalContext = dassana.getJSONObject(GENERAL_CONTEXT);
    JSONObject policyContext = dassana.getJSONObject(POLICY_CONTEXT);

    Assertions.assertTrue(normalize.getString("workflowId").contentEquals("aws-config"));
    Assertions.assertTrue(generalContext.getString("workflowId").contentEquals("general-context-aws"));
    Assertions.assertTrue(generalContext.getJSONObject("risk").getString("riskValue").contentEquals("low"));

    Assertions.assertTrue(policyContext.getString("workflowId").contentEquals("security-group-wide-open"));

    String response2 = helper.executeRunApi(helper.getFileContent("inputs/validAlertWithNoGeneralContext.json"),
        queryParams);
    System.out.println(response2);


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

      return stepRunResponse;
    }
  }

}
