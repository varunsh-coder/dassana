package app.dassana.core.risk.model;

public class Risk {
  String id = "";
  String riskValue = "";
  String condition = "";

  public String getCondition() {
    return condition;
  }

  public void setCondition(String condition) {
    this.condition = condition;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getRiskValue() {
    return riskValue;
  }

  public void setRiskValue(String riskValue) {
    this.riskValue = riskValue;
  }
}
