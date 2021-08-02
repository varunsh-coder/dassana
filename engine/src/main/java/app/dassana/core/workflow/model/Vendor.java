package app.dassana.core.workflow.model;

import app.dassana.core.rule.MatchType;
import java.util.List;

public class Vendor {

  String name;
  List<String> rules;
  MatchType matchType;

  public MatchType getMatchType() {
    return matchType;
  }

  public void setMatchType(MatchType matchType) {
    this.matchType = matchType;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<String> getRules() {
    return rules;
  }

  public void setRules(List<String> rules) {
    this.rules = rules;
  }
}
