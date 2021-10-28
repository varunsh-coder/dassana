package app.dassana.core.rule;

import java.util.LinkedList;
import java.util.List;

import app.dassana.core.launch.model.Message;
import app.dassana.core.workflow.model.Error;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class RuleMatchTest {

  @Test
  void ruleMatch() throws Exception {

    RuleMatch ruleMatch=new RuleMatch();
    List<String> rules=new LinkedList<>();
    List<Error> errorList = new LinkedList<>();
    rules.add("(.resourceId | contains (\"public\")) and .\"website-info\"?");
    String jsonData="{\n"
        + "  \"resourceId\": \"public-foo\",\n"
        + "  \"website-info\": {\n"
        + "      \"bucketUrl\": \"dassana-public-content.s3-website-us-east-1.amazonaws.com\"\n"
        + "  }\n"
        + "}";
    boolean match = ruleMatch.ruleMatch(rules, jsonData, MatchType.ANY, "", "", errorList);
    Assertions.assertTrue(match);

  }
}