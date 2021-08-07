package app.dassana.core.launch;

import app.dassana.core.launch.model.ProcessingResponse;
import app.dassana.core.launch.model.Request;
import app.dassana.core.workflow.RequestProcessor;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.function.aws.MicronautRequestHandler;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Introspected
public class ApiHandler extends
    MicronautRequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

  @Inject RequestProcessor requestProcessor;

  private static final Logger logger = LoggerFactory.getLogger(ApiHandler.class);

  public static final String API_PARAM_SKIP_GENERAL_CONTEXT = "skipGeneralContext";
  public static final String API_PARAM_SKIP_POLICY_CONTEXT = "skipPolicyContext";
  public static final String API_PARAM_SKIP_S3UPLOAD = "skipS3Upload";
  public static final String API_PARAM_SKIP_POST_PROCESSOR = "skipPostProcessor";
  public static final String API_PARAM_SKIP_REFRESH_FROM_S3 = "refreshWorkflowsFromS3";
  public static final String API_INCLUDE_INPUT_REQUEST = "includeInputRequest";

  public ApiHandler() {
  }

  @Override
  public APIGatewayProxyResponseEvent execute(APIGatewayProxyRequestEvent input) {
    APIGatewayProxyResponseEvent gatewayProxyResponseEvent = new APIGatewayProxyResponseEvent();

    try {
      String inputJson;
      if (input.getIsBase64Encoded()) {
        inputJson = new String(Base64.getDecoder().decode(input.getBody()));
      } else {
        inputJson = input.getBody();
      }
      ProcessingResponse processingResponse = requestProcessor
          .processRequest(getRequestFromQueryParam(input.getQueryStringParameters(), inputJson));
      gatewayProxyResponseEvent.setBody(processingResponse.getDecoratedJson());
      gatewayProxyResponseEvent.setStatusCode(200);

      Map<String, String> headers = new HashMap<>();
      headers.put("content-type", "application/json");
      gatewayProxyResponseEvent.setHeaders(headers);

    } catch (Exception e) {
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      e.printStackTrace(pw);
      gatewayProxyResponseEvent.setBody(sw.toString());
      gatewayProxyResponseEvent.setStatusCode(500);
    }
    return gatewayProxyResponseEvent;
  }

  /**
   * DO NOT put anything cloud implementation specific in this method i.e. do not have any aws/gcp/azure specific
   * constructs in this method as we intend to use this method as abstracted method which can be called by anyone who
   * speaks http layer. As you can see, there is no aws api gateway specific parameters.
   *
   * @param parameters   query params
   * @param inputJsonStr input to process
   * @return request obj which the engine will run
   */
  public Request getRequestFromQueryParam(Map<String, String> parameters, String inputJsonStr) {

    if (parameters == null) {
      parameters = new HashMap<>();
    }

    boolean skipGeneralContext = parameters
        .getOrDefault(API_PARAM_SKIP_GENERAL_CONTEXT, "false").contentEquals("true");

    boolean skipPolicyContext = parameters
        .getOrDefault(API_PARAM_SKIP_POLICY_CONTEXT, "false").contentEquals("true");

    boolean skipPostProcessor = parameters
        .getOrDefault(API_PARAM_SKIP_POST_PROCESSOR, "false").contentEquals("true");

    boolean skipS3Upload = parameters
        .getOrDefault(API_PARAM_SKIP_S3UPLOAD, "false").contentEquals("true");

    boolean refreshFromS3 = Boolean.parseBoolean(parameters.getOrDefault(API_PARAM_SKIP_REFRESH_FROM_S3, "false"));

    Request request = new Request(inputJsonStr);
    request.setRefreshFromS3(refreshFromS3);

    request.setQueueProcessing(false);
    request.setSkipPostProcessor(skipPostProcessor);
    request.setSkipGeneralContext(skipGeneralContext);
    request.setSkipPolicyContext(skipPolicyContext);
    request.setSkipS3Upload(skipS3Upload);
    return request;
  }


}
