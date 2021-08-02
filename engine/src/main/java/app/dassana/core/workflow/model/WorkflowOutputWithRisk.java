package app.dassana.core.workflow.model;

import app.dassana.core.risk.Risk;

public class WorkflowOutputWithRisk extends WorkflowOutput {

  Workflow workflowUsed;
  Risk risk;
  Long timeTakenMs;

  public Long getTimeTakenMs() {
    return timeTakenMs;
  }

  public Workflow getWorkflowUsed() {
    return workflowUsed;
  }

  public void setWorkflowUsed(Workflow workflowUsed) {
    this.workflowUsed = workflowUsed;
  }

  public void setTimeTakenMs(Long timeTakenMs) {
    this.timeTakenMs = timeTakenMs;
  }

  public WorkflowOutputWithRisk() {
    risk = new Risk();
  }

  public Risk getRisk() {
    return risk;
  }

  public void setRisk(Risk risk) {
    this.risk = risk;
  }
}
