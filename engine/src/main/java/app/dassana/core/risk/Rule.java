package app.dassana.core.risk;

public class Rule {

  String name;
  String condition;
  String risk;

  public Rule(String name, String condition, String risk) {
    this.name = name;
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
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getCondition() {
    return condition;
  }

  public void setCondition(String condition) {
    this.condition = condition;
  }
}
