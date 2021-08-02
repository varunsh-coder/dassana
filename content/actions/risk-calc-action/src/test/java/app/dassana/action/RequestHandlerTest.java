package app.dassana.action;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;

class RequestHandlerTest {

  @org.junit.jupiter.api.Test
  void execute() throws IOException {
    RequestHandler requestHandler = new RequestHandler();

    InputStream resourceAsStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("input.json");

    String inputJson = IOUtils.toString(resourceAsStream, Charset.defaultCharset());

    Request request = new Request();
    request.setDefaultRisk("high");

    request.setJsonData(new JSONObject(inputJson).toMap());

    List<Rule> ruleList = new LinkedList<>();
    String condition = ".csp == \"aws\" ";
    Rule rule = new Rule("foo", condition, "critical");
    ruleList.add(rule);
    request.setRiskRules(ruleList);

    Risk risk = requestHandler.execute(request);

    Assertions.assertEquals("foo", risk.getName());
    Assertions.assertEquals("critical", risk.getRisk());
    Assertions.assertEquals(condition, risk.getCondition());

  }
}