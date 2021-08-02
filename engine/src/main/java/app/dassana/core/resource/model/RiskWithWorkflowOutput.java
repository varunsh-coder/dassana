package app.dassana.core.resource.model;

import app.dassana.core.risk.Risk;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class RiskWithWorkflowOutput {

  Risk risk;
  List<Map<String, Object>> riskFlowOutput;

  public RiskWithWorkflowOutput(Risk risk,
      List<Map<String, Object>> riskFlowOutput) {
    this.risk = risk;
    this.riskFlowOutput = riskFlowOutput;
  }

  public Risk getRisk() {
    return risk;
  }

  public void setRisk(Risk risk) {
    this.risk = risk;
  }

  public List<Map<String, Object>> getRiskFlowOutput() {
    if (riskFlowOutput == null) {
      riskFlowOutput = new LinkedList<>();
    }
    return riskFlowOutput;
  }

  public void setRiskFlowOutput(List<Map<String, Object>> riskFlowOutput) {
    this.riskFlowOutput = riskFlowOutput;
  }
}
