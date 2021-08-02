package app.dassana.core.launch;

import app.dassana.core.launch.model.ProcessingResponse;
import app.dassana.core.launch.model.RequestConfig;
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

  public ApiHandler() {
  }

  @Override
  public APIGatewayProxyResponseEvent execute(APIGatewayProxyRequestEvent input) {
    APIGatewayProxyResponseEvent gatewayProxyResponseEvent = new APIGatewayProxyResponseEvent();

    try {

      Map<String, String> parameters = input.getQueryStringParameters();

      if (parameters == null) {
        parameters = new HashMap<>();
      }

      boolean skipResourcePrioritization = parameters
          .getOrDefault("skipResourcePrioritization", "false").contentEquals("true");

      boolean skipResourceContextualization = parameters
          .getOrDefault("skipResourceContextualization", "false").contentEquals("true");

      boolean skipPostProcessor = parameters
          .getOrDefault("skipPostProcessor", "false").contentEquals("true");

      boolean skipS3Upload = parameters
          .getOrDefault("skipS3Upload", "false").contentEquals("true");

      boolean includeNormalizers = Boolean.parseBoolean(parameters.getOrDefault("includeNormalizers", "false"));

      boolean refreshFromS3 = Boolean.parseBoolean(parameters.getOrDefault("refreshWorkflowsFromS3", "false"));

      String json;

      RequestConfig requestConfig = new RequestConfig();
      requestConfig.setRefreshFromS3(refreshFromS3);

      if (input.getIsBase64Encoded()) {
        json = new String(Base64.getDecoder().decode(input.getBody()));
      } else {
        json = input.getBody();
      }
      requestConfig.setInputJson(json);
      requestConfig.setQueueProcessing(false);
      requestConfig.setSkipPostProcessor(skipPostProcessor);
      requestConfig.setSkipResourcePrioritization(skipResourcePrioritization);
      requestConfig.setSkipResourceContextualization(skipResourceContextualization);
      requestConfig.setSkipS3Upload(skipS3Upload);
      requestConfig.setIncludeLoadedWorkflows(includeNormalizers);

      ProcessingResponse processingResponse = requestProcessor.processRequest(requestConfig);
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


}
