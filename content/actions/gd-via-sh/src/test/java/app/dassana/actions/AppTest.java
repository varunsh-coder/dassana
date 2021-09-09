package app.dassana.actions;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.utils.IoUtils;

public class AppTest {

  @Test
  public void test() throws IOException {
    App function = new App();
    String inputJson = IoUtils
        .toUtf8String(Thread.currentThread().getContextClassLoader().getResourceAsStream("sample-gd.json"));
    Response result = function.handleRequest(new JSONObject(inputJson).toMap(), null);


    Assertions.assertTrue(result.getRegion().contentEquals("us-east-1"));
    Assertions.assertTrue(result.getResourceContainer().contentEquals("363265257036"));
    Assertions.assertTrue(result.getCsp().contentEquals("aws"));
    Assertions
        .assertTrue(result.getVendorPolicy().contentEquals("TTPs/Initial Access/UnauthorizedAccess:EC2-SSHBruteForce"));
    Assertions
        .assertTrue(result.getArn().contentEquals("arn:aws:ec2:us-east-1:363265257036:instance/i-054474987a390f341"));
    Assertions.assertTrue(result.getAlertId().contentEquals(
        "arn:aws:guardduty:us-east-1:363265257036:detector/96bad234c3d0033d695e70c93b8741fb/finding/36bc3df32928fe1c98ac2a6d9a48fb56"));

  }
}
