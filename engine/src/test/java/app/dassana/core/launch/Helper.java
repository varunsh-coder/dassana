package app.dassana.core.launch;

import static app.dassana.core.launch.ApiHandler.API_INCLUDE_INPUT_REQUEST;
import static app.dassana.core.launch.ApiHandler.API_PARAM_SKIP_GENERAL_CONTEXT;
import static app.dassana.core.launch.ApiHandler.API_PARAM_SKIP_POLICY_CONTEXT;
import static app.dassana.core.launch.ApiHandler.API_PARAM_SKIP_POST_PROCESSOR;
import static app.dassana.core.launch.ApiHandler.API_PARAM_SKIP_S3UPLOAD;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Singleton;
import org.apache.commons.io.IOUtils;

@Singleton
public class Helper {

  public Helper() {
  }

  public String getFileContent(String fileName) throws IOException {
    return IOUtils.toString(Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName),
        Charset.defaultCharset());
  }


  public String executeApi(String inputJsonStr, boolean includeInputRequest) {
    APIGatewayProxyRequestEvent apiGatewayProxyRequestEvent = new APIGatewayProxyRequestEvent();

    apiGatewayProxyRequestEvent.setBody(inputJsonStr);
    apiGatewayProxyRequestEvent.setIsBase64Encoded(false);

    Map<String, String> queryParams = new HashMap<>();
    queryParams.put(API_PARAM_SKIP_GENERAL_CONTEXT, "false");
    queryParams.put(API_PARAM_SKIP_POLICY_CONTEXT, "false");
    queryParams.put(API_PARAM_SKIP_POST_PROCESSOR, "true");
    queryParams.put(API_PARAM_SKIP_S3UPLOAD, "true");
    if (includeInputRequest) {
      queryParams.put(API_INCLUDE_INPUT_REQUEST, "true");
    } else {
      queryParams.put(API_INCLUDE_INPUT_REQUEST, "false");
    }

    apiGatewayProxyRequestEvent.setQueryStringParameters(queryParams);

    ApiHandler apiHandler = new ApiHandler();
    APIGatewayProxyResponseEvent responseEvent = apiHandler.execute(apiGatewayProxyRequestEvent);
    if (responseEvent.getStatusCode() != 200) {
      throw new RuntimeException(responseEvent.getBody());
    }
    return responseEvent.getBody();
  }


  public String executeApi(String inputJsonStr) throws IOException {
    return executeApi(inputJsonStr, false);
  }

}
