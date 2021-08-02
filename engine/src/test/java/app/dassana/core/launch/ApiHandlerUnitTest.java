package app.dassana.core.launch;


import app.dassana.core.normalize.model.NormalizedWorkFlowOutput;
import app.dassana.core.workflow.StepRunnerApi;
import app.dassana.core.workflow.model.Step;
import app.dassana.core.workflow.model.StepRunResponse;
import app.dassana.core.workflow.model.Workflow;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@MicronautTest
public class ApiHandlerUnitTest {

  @Inject ApiHandler handler;

  @Test
  @Disabled
  void execute() throws Exception {

    String event = IOUtils
        .toString(Thread.currentThread().getContextClassLoader().getResourceAsStream("Event1.json"),
            Charset.defaultCharset());
    APIGatewayProxyRequestEvent apiGatewayProxyRequestEvent = new APIGatewayProxyRequestEvent();

    apiGatewayProxyRequestEvent.setBody(event);
    apiGatewayProxyRequestEvent.setIsBase64Encoded(false);

    Map<String, String> queryParams = new HashMap<>();
    queryParams.put("skipResourcePrioritization", "false");
    queryParams.put("skipResourceContextualization", "false");
    queryParams.put("skipPostProcessor", "true");
    queryParams.put("skipS3Upload", "true");

    apiGatewayProxyRequestEvent.setQueryStringParameters(queryParams);

    APIGatewayProxyResponseEvent responseEvent = handler.execute(apiGatewayProxyRequestEvent);
    if (responseEvent.getStatusCode() != 200) {
      throw new RuntimeException(responseEvent.getBody());
    }

  }


  @Singleton
  @Replaces(app.dassana.core.workflow.LambdaStepRunner.class)
  public static class MockLambdaStepRunner implements StepRunnerApi {

    public MockLambdaStepRunner() {
    }


    @Override
    public StepRunResponse runStep(Workflow workflow, Step step, String inputJson,
        NormalizedWorkFlowOutput normalizedWorkFlowOutput) throws Exception {

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
            Thread.currentThread().getContextClassLoader().getResourceAsStream("responses/tags.json"),
            Charset.defaultCharset()));
      }

      if (step.getId().contentEquals("riskCalc")) {
        stepRunResponse.setResponse(IOUtils.toString(
            Thread.currentThread().getContextClassLoader().getResourceAsStream("responses/resourceRiskCalcAction.json"),
            Charset.defaultCharset()));
      }

      return stepRunResponse;
    }
  }

}
