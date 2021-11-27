package app.dassana.core.workflowmanager.model.resource;

import app.dassana.core.workflowmanager.risk.model.RiskConfig;
import app.dassana.core.workflowmanager.workflow.model.Workflow;

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
