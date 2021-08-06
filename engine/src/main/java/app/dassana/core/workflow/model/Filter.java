package app.dassana.core.workflow.model;

import app.dassana.core.rule.MatchType;
import java.util.List;

public class Filter {

  private MatchType matchType;
  List<String> rules;



  public MatchType getMatchType() {
    return matchType;
  }

  public void setMatchType(MatchType matchType) {
    this.matchType = matchType;
  }

  public List<String> getRules() {
    return rules;
  }

  public void setRules(List<String> rules) {
    this.rules = rules;
  }
}
