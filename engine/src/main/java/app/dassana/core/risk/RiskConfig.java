package app.dassana.core.risk;

import java.util.Set;

public class RiskConfig {

  String defaultRisk;
  Set<Rule> riskRules;


  public String getDefaultRisk() {
    return defaultRisk;
  }

  public void setDefaultRisk(String defaultRisk) {
    this.defaultRisk = defaultRisk;
  }

  public Set<Rule> getRiskRules() {
    return riskRules;
  }

  public void setRiskRules(Set<Rule> riskRules) {
    this.riskRules = riskRules;
  }
}
