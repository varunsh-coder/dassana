package app.dassana.resourceid.sh;

import java.io.IOException;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.utils.IoUtils;

class AppTest {

  @Test
  void handleRequest() throws IOException {

    App app = new App();

    String alertJson = IoUtils
        .toUtf8String(Thread.currentThread().getContextClassLoader().getResourceAsStream("example1"
            + ".json"));
    JSONObject jsonObject = new JSONObject(alertJson);
    NormalizationResult normalizationResult = app.handleRequest(jsonObject.toMap(), null);

    Assertions.assertEquals("aws",normalizationResult.getCsp());
    Assertions.assertEquals("363265257036",normalizationResult.getResourceContainer());
    Assertions.assertEquals("control/aws-foundational-security-best-practices/v/1.0.0/APIGateway.4",
        normalizationResult.getPolicyId());
    Assertions.assertEquals("apigateway",normalizationResult.getService());
    Assertions.assertEquals("/restapis/6u20vtvjpk/stages/v1",normalizationResult.getResourceId());
    Assertions.assertEquals("arn:aws:apigateway:us-east-1::/restapis/6u20vtvjpk/stages/v1",normalizationResult.getArn());


  }
}