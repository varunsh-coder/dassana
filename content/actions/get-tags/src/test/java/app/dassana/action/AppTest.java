package app.dassana.action;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class AppTest {

  @Test
  @Disabled
  void handleRequest() {
    App app = new App();
    final Map<String, String> input = new HashMap<>();
    input.put("arn", "arn:aws:iam::363265257036:root");
    input.put("region", "us-east-1");
    app.handleRequest(input, null);
  }
}