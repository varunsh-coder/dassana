package app.dassana.core.workflow.model;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class WorkflowOutput {

  Map<String, Object> output;
  List<Map<String, Object>> stepOutput;
  String workflowId;

  public Map<String, Object> getOutput() {
    return output;
  }

  public void setOutput(Map<String, Object> output) {
    this.output = output;
  }

  public String getWorkflowId() {
    return workflowId;
  }

  public void setWorkflowId(String workflowId) {
    this.workflowId = workflowId;
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
