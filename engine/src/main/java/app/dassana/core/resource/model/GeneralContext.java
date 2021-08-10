package app.dassana.core.resource.model;

import app.dassana.core.risk.model.RiskConfig;
import app.dassana.core.workflow.model.Workflow;

public class GeneralContext extends Workflow {

  private String csp;
  private RiskConfig riskConfig;

  public String getCsp() {
    return csp;
  }

  public void setCsp(String csp) {
    this.csp = csp;
  }

  public RiskConfig getRiskConfig() {
    return riskConfig;
  }

  public void setRiskConfig(RiskConfig riskConfig) {
    this.riskConfig = riskConfig;
  }
}
