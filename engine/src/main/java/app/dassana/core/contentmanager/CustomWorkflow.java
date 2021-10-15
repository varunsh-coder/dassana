package app.dassana.core.contentmanager;

import app.dassana.core.workflow.model.Workflow;

public class CustomWorkflow {

  Workflow workflow;
  String workflowFileContent;

  public Workflow getWorkflow() {
    return workflow;
  }

  public void setWorkflow(Workflow workflow) {
    this.workflow = workflow;
  }

  public CustomWorkflow(Workflow workflow, String workflowFileContent) {
    this.workflow = workflow;
    this.workflowFileContent = workflowFileContent;
  }
}
