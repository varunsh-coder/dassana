package app.dassana.core.resource.model;

import app.dassana.core.risk.RiskConfig;
import app.dassana.core.workflow.model.Workflow;

public class GeneralContext extends Workflow {
  RiskConfig riskConfig;

  public RiskConfig getRiskConfig() {
    return riskConfig;
  }

  public void setRiskConfig(RiskConfig riskConfig) {
    this.riskConfig = riskConfig;
  }
}
