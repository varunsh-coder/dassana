package jar;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import app.dassana.action.App;
import org.json.JSONObject;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.utils.IoUtils;

public class AppTest {

  @Test
  @Disabled//enable it for local testing, it makes real aws api calls
  public void handleRequest_shouldReturnConstantValue() throws IOException {
    App function = new App();

    String event =
        IoUtils.toUtf8String(Thread.currentThread().getContextClassLoader().getResourceAsStream("sampleFinding.json"));

    JSONObject jsonObject = new JSONObject(event);
    Object result = function.handleRequest(jsonObject.toMap(), null);
  }
}
