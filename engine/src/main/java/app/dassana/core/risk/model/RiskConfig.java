package app.dassana.core.risk.model;

import java.util.List;

public class RiskConfig {

  String defaultRisk;
  List<Rule> riskRules;


  public String getDefaultRisk() {
    return defaultRisk;
  }

  public void setDefaultRisk(String defaultRisk) {
    this.defaultRisk = defaultRisk;
  }

  public List<Rule> getRiskRules() {
    return riskRules;
  }

  public void setRiskRules(List<Rule> riskRules) {
    this.riskRules = riskRules;
  }
}
