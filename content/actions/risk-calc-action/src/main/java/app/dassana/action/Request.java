//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package app.dassana.action;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Request {

  private Map<String, Object> jsonData;
  private String defaultRisk;
  private List<Rule> riskRules;

  public Request() {
  }

  public Map<String, Object> getJsonData() {
    return jsonData;
  }

  public void setJsonData(Map<String, Object> jsonData) {
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
