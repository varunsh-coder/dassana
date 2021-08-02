package app.dassana.core.workflow.model;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class WorkflowOutput {

  List<Map<String, Object>> workflowOutput;
  List<Map<String, Object>> stepOutput;

  public List<Map<String, Object>> getWorkflowOutput() {
    if (workflowOutput == null) {
      workflowOutput = new LinkedList<>();
    }
    return workflowOutput;
  }

  public void setWorkflowOutput(List<Map<String, Object>> workflowOutput) {
    this.workflowOutput = workflowOutput;
  }

  public List<Map<String, Object>> getStepOutput() {
    if (stepOutput == null) {
      stepOutput = new LinkedList<>();
    }
    return stepOutput;
  }

  public void setStepOutput(List<Map<String, Object>> stepOutput) {
    this.stepOutput = stepOutput;
  }
}
