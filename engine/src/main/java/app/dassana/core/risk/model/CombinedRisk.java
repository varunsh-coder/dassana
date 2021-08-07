package app.dassana.core.risk.model;

public class CombinedRisk {

  Risk policyContextRisk;
  Risk resourceContextRisk;
  Risk generalContextRisk;

  public Risk getResourceContextRisk() {
    return resourceContextRisk;
  }

  public void setResourceContextRisk(Risk resourceContextRisk) {
    this.resourceContextRisk = resourceContextRisk;
  }

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
