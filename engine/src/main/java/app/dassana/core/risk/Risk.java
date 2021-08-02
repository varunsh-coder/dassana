package app.dassana.core.risk;

public class Risk {

  String name = "";
  String riskValue = "";
  String condition = "";

  public String getCondition() {
    return condition;
  }

  public void setCondition(String condition) {
    this.condition = condition;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getRiskValue() {
    return riskValue;
  }

  public void setRiskValue(String riskValue) {
    this.riskValue = riskValue;
  }
}
