package app.dassana.core.launch;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import software.amazon.awssdk.utils.IoUtils;

/**
 * This class stimulates running of Dassana API. In theory, you could use AWS SAM debugging capabilities, but AWS SAM
 * is quite rough, at least on MacOS with apple silicon- it takes a long time to compile and its docker container
 * seldom die etc.
 *
 * When you run this class, you are only running the api part locally, all Dassana Actions (lambda functions) are
 * invoked on the cloud side. 
 */
public class App {

  public static void main(String[] args) throws IOException {

    ApiHandler apiHandler = new ApiHandler();
    String event = IoUtils
        .toUtf8String(Thread.currentThread().getContextClassLoader().getResourceAsStream("TestEvent.json"));
    APIGatewayProxyRequestEvent apiGatewayProxyRequestEvent = new APIGatewayProxyRequestEvent();

    apiGatewayProxyRequestEvent.setBody(event);
    apiGatewayProxyRequestEvent.setIsBase64Encoded(false);

    Map<String, String> queryParams = new HashMap<>();
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
  }

}
