package app.dassana.core.workflow.model;

import app.dassana.core.launch.model.Message;

public class Error {

  private String workflowId;
  private Component component;
  private String componentId;
  private Message message;

  public String getWorkflowId() {
    return workflowId;
  }

  public void setWorkflowId(final String workflowId) {
    this.workflowId = workflowId;
  }

  public Component getComponent() {
    return component;
  }

  public void setComponent(Component component) {
    this.component = component;
  }

  public String getComponentId() {
    return componentId;
  }

  public void setComponentId(String componentId) {
    this.componentId = componentId;
  }

  public Message getMessage() {
    return message;
  }

  public void setMessage(Message message) {
    this.message = message;
  }
}
