//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package app.dassana.core.workflowmanager.risk.eval;

import app.dassana.core.workflowmanager.risk.model.Rule;
import java.util.LinkedList;
import java.util.List;

public class RiskEvalRequest {

  private String jsonData;
  private String defaultRisk;
  private List<Rule> riskRules;

  public RiskEvalRequest() {
  }

  public String getJsonData() {
    return jsonData;
  }

  public void setJsonData(String jsonData) {
    this.jsonData = jsonData;
  }

  public String getDefaultRisk() {
    return this.defaultRisk;
  }

  public void setDefaultRisk(String defaultRisk) {
    this.defaultRisk = defaultRisk;
  }

  public List<Rule> getRiskRules() {
    if (riskRules == null) {
      return new LinkedList<>();
    }
    return this.riskRules;
  }

  public void setRiskRules(List<Rule> riskRules) {
    this.riskRules = riskRules;
  }
}
