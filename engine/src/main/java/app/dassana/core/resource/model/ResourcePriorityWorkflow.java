package app.dassana.core.resource.model;

import app.dassana.core.risk.RiskConfig;
import app.dassana.core.rule.MatchType;
import app.dassana.core.workflow.model.Workflow;
import java.util.Collections;
import java.util.List;

public class ResourcePriorityWorkflow extends Workflow {

  RiskConfig riskConfig;

  private List<String> filterRules;
  private MatchType matchType;

  public RiskConfig getRiskConfig() {
    return riskConfig;
  }

  public void setRiskConfig(RiskConfig riskConfig) {
    this.riskConfig = riskConfig;
  }

  public List<String> getFilterRules() {
    return filterRules;
  }

  public void setFilterRules(List<String> filterRules) {
    Collections.reverse(filterRules);
    this.filterRules = filterRules;
  }

  public MatchType getMatchType() {
    return matchType;
  }

  public void setMatchType(MatchType matchType) {
    this.matchType = matchType;
  }
}
