package app.dassana.core.risk.model;

public class CombinedRisk {

  Risk policyContextRisk;
  Risk generalContextRisk;

  public Risk getPolicyContextRisk() {
    return policyContextRisk;
  }

  public void setPolicyContextRisk(Risk risk) {
    this.policyContextRisk = risk;
  }

  public Risk getGeneralContextRisk() {
    return generalContextRisk;
  }

  public void setGeneralContextRisk(Risk generalContextRisk) {
    this.generalContextRisk = generalContextRisk;
  }
}
