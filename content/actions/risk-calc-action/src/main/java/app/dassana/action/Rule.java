//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package app.dassana.action;

public class Rule {
  String name;
  String condition;
  String risk;

  public Rule() {
  }

  public Rule(String name, String condition, String risk) {
    this.name = name;
    this.condition = condition;
    this.risk = risk;
  }

  public String getName() {
    return this.name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getCondition() {
    return this.condition;
  }

  public void setCondition(String condition) {
    this.condition = condition;
  }

  public String getRisk() {
    return this.risk;
  }

  public void setRisk(String risk) {
    this.risk = risk;
  }
}
