package app.dassana.resourceid.sh;

import java.io.IOException;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.utils.IoUtils;

class AppTest {


  @Test
  void testS3Alert() throws Exception {
    App app = new App();
    String alertJson = IoUtils
        .toUtf8String(Thread.currentThread().getContextClassLoader().getResourceAsStream("example3"
            + ".json"));
    JSONObject jsonObject = new JSONObject(alertJson);
    NormalizationResult normalizationResult = app.handleRequest(jsonObject.toMap(), null);
    String resourceId = normalizationResult.getResourceId();
    Assertions.assertTrue(resourceId.contentEquals("dassana-public-content"));
  }


  @Test
  void testRestApiAlert() throws IOException {

    App app = new App();

    String alertJson = IoUtils
        .toUtf8String(Thread.currentThread().getContextClassLoader().getResourceAsStream("example1"
            + ".json"));
    JSONObject jsonObject = new JSONObject(alertJson);
    NormalizationResult normalizationResult = app.handleRequest(jsonObject.toMap(), null);

    Assertions.assertEquals("aws", normalizationResult.getCsp());
    Assertions.assertEquals("363265257036", normalizationResult.getResourceContainer());
    Assertions.assertEquals("api-gw-associated-with-waf",
        normalizationResult.getVendorPolicy());
    Assertions.assertEquals("6u20vtvjpk", normalizationResult.getResourceId());
    Assertions
        .assertEquals("arn:aws:apigateway:us-east-1::/restapis/6u20vtvjpk/stages/v1", normalizationResult.getArn());

  }


  @Test
  void testSecurityGroupAlert() throws IOException {

    App app = new App();

    String alertJson = IoUtils
        .toUtf8String(Thread.currentThread().getContextClassLoader().getResourceAsStream("example2"
            + ".json"));
    JSONObject jsonObject = new JSONObject(alertJson);
    NormalizationResult normalizationResult = app.handleRequest(jsonObject.toMap(), null);

    Assertions.assertEquals("aws", normalizationResult.getCsp());
    Assertions.assertEquals("363265257036", normalizationResult.getResourceContainer());
    Assertions.assertEquals("vpc-sg-restricted-common-ports",
        normalizationResult.getVendorPolicy());
    Assertions.assertEquals("sg-061d7bbf4c68da2c7", normalizationResult.getResourceId());
    Assertions
        .assertEquals("arn:aws:ec2:us-east-1:363265257036:security-group/sg-061d7bbf4c68da2c7",
            normalizationResult.getArn());

  }
}