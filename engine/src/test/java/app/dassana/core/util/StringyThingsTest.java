package app.dassana.core.util;

import java.io.IOException;
import java.nio.charset.Charset;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class StringyThingsTest {

  @Test
  void getJsonFromYaml() throws IOException {

    String validateWorkflowRequestJsonInput = IOUtils
        .toString(Thread.currentThread().getContextClassLoader().getResourceAsStream(
            "validationRequests/validWorkflow.json"),
            Charset.defaultCharset());
    JSONObject jsonObject = new JSONObject(validateWorkflowRequestJsonInput);
    String workflow = StringyThings.getJsonFromYaml(jsonObject.getString("workflow"));

    JSONObject workflowAsJsonObj = new JSONObject(workflow);
    Assertions.assertTrue(workflowAsJsonObj.getString("id").contentEquals("foo-cloud-normalize"));

  }
}