package app.dassana.core.policycontext;

import app.dassana.core.risk.Rule;
import java.util.Set;

public class RiskCalcActionRequest {


  Object jsonData;
  String defaultRisk;
  Set<Rule> riskRules;

  public Object getJsonData() {
    return jsonData;
  }

  public void setJsonData(Object jsonData) {
    this.jsonData = jsonData;
  }

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
