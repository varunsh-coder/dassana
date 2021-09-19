package app.dassana.core.launch;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import software.amazon.awssdk.utils.IoUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import static app.dassana.core.contentmanager.ContentManager.WORKFLOW_ID;
import static app.dassana.core.launch.ApiHandler.*;
import static app.dassana.core.launch.ApiHandler.API_INCLUDE_ALERT_IN_OUTPUT;

public class App {

  static APIGatewayProxyRequestEvent getApiGatewayProxyRequestEvent(Map<String, String> parameters) {
    APIGatewayProxyRequestEvent apiGatewayProxyRequestEvent = new APIGatewayProxyRequestEvent();
    if (parameters != null && parameters.size() > 0) {
      apiGatewayProxyRequestEvent.setQueryStringParameters(parameters);
    }
    apiGatewayProxyRequestEvent.setIsBase64Encoded(false);
    return apiGatewayProxyRequestEvent;
  }

  static Map<String, String> getQueryParams() {
    Map<String, String> queryParams = new HashMap<>();
    queryParams.put(API_PARAM_SKIP_GENERAL_CONTEXT, "false");
    queryParams.put(API_PARAM_SKIP_POLICY_CONTEXT, "false");
    queryParams.put(API_PARAM_SKIP_POST_PROCESSOR, "true");
    queryParams.put(API_PARAM_SKIP_S3UPLOAD, "true");
    queryParams.put(API_INCLUDE_ALERT_IN_OUTPUT, "false");

    return queryParams;
  }

  static APIGatewayProxyRequestEvent getResponse(String workflowId, String method, boolean isDefault) throws Exception {
    Map<String, String> queryParams = getQueryParams();
    queryParams.put(WORKFLOW_ID, workflowId);
    if(isDefault){
      queryParams.put("default", "true");
    }

    APIGatewayProxyRequestEvent apiGatewayProxyRequestEvent = getApiGatewayProxyRequestEvent(queryParams);
    apiGatewayProxyRequestEvent.setHttpMethod(method);
    apiGatewayProxyRequestEvent.setPath(WORKFLOW_PATH);

    return apiGatewayProxyRequestEvent;
  }

  static void testApi(){
    try {
      ApiHandler apiHandler = new ApiHandler();

      boolean isDefault = false;
      isDefault = true;

      String workflowId = "foo-cloud-normalize";
      //workflowId = "general-context-demo-cloud";
      //workflowId = "demo-cloud-resource-context";

      APIGatewayProxyRequestEvent apiGatewayProxyRequestEvent = getResponse(workflowId, "delete", isDefault);
      APIGatewayProxyResponseEvent responseEvent = apiHandler.execute(apiGatewayProxyRequestEvent);
      String body = responseEvent.getBody();
      System.out.println(body);
    }catch (Exception e){
      e.getStackTrace();
    }
  }

  public static void main(String[] args) throws IOException {
    //testApi();/*
    ApiHandler apiHandler = new ApiHandler();
    String event = IoUtils
        .toUtf8String(Thread.currentThread().getContextClassLoader().getResourceAsStream("TestEvent.json"));
    APIGatewayProxyRequestEvent apiGatewayProxyRequestEvent = new APIGatewayProxyRequestEvent();

    apiGatewayProxyRequestEvent.setBody(event);
    apiGatewayProxyRequestEvent.setIsBase64Encoded(false);

    Map<String, String> queryParams = new HashMap<>();
    queryParams.put("skipGeneralContext", "false");
    queryParams.put("skipPolicyContext", "false");
    queryParams.put("skipPostProcessor", "false");
    queryParams.put("skipS3Upload", "false");

    apiGatewayProxyRequestEvent.setQueryStringParameters(queryParams);
    apiGatewayProxyRequestEvent.setHttpMethod("post");
    apiGatewayProxyRequestEvent.setPath("/run");

    APIGatewayProxyResponseEvent responseEvent = apiHandler.execute(apiGatewayProxyRequestEvent);
    System.out.println(responseEvent.getBody());
    if (responseEvent.getStatusCode() != 200) {
      throw new RuntimeException(responseEvent.getBody());
    } else {
      System.exit(0);
    }
    //*/
  }

}
