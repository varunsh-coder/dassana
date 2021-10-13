package app.dassana.core.rule;

import java.util.LinkedList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class RuleMatchTest {

  @Test
  void ruleMatch() throws Exception {

    RuleMatch ruleMatch=new RuleMatch();
    List<String> rules=new LinkedList<>();
    rules.add("(.resourceId | contains (\"public\")) and .\"website-info\"?");
    String jsonData="{\n"
        + "  \"resourceId\": \"public-foo\",\n"
        + "  \"website-info\": {\n"
        + "      \"bucketUrl\": \"dassana-public-content.s3-website-us-east-1.amazonaws.com\"\n"
        + "  }\n"
        + "}";
    boolean match = ruleMatch.ruleMatch(rules, jsonData, MatchType.ANY);
    Assertions.assertTrue(match);

  }
}