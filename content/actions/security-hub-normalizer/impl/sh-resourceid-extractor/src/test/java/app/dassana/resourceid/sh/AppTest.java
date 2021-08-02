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
    Assertions.assertEquals("ec2",normalizationResult.getService());
    Assertions.assertEquals("sg-04101d5687a91bc17",normalizationResult.getResourceId());
    Assertions.assertEquals("arn:aws:ec2:us-east-1:363265257036:security-group/sg-04101d5687a91bc17",normalizationResult.getArn());


  }
}