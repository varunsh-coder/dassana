package app.dassana.core.resource.model;

import app.dassana.core.risk.model.RiskConfig;
import app.dassana.core.workflow.model.Workflow;

public class ResourceContext extends Workflow {

  RiskConfig riskConfig;

  public RiskConfig getRiskConfig() {
    return riskConfig;
  }

  public void setRiskConfig(RiskConfig riskConfig) {
    this.riskConfig = riskConfig;
  }
}
