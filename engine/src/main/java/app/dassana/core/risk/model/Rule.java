package app.dassana.core.risk.model;

public class Rule {

  String id;
  String condition;
  String risk;

  public Rule(String id, String condition, String risk) {
    this.id = id;
    this.condition = condition;
    this.risk = risk;
  }

  public String getRisk() {
    return risk;
  }

  public void setRisk(String risk) {
    this.risk = risk;
  }

  public String getName() {
    return id;
  }

  public void setName(String id) {
    this.id = id;
  }

  public String getCondition() {
    return condition;
  }

  public void setCondition(String condition) {
    this.condition = condition;
  }
}
