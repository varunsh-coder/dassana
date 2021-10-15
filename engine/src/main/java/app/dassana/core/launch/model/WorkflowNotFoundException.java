package app.dassana.core.launch.model;

public class WorkflowNotFoundException extends RuntimeException{

  public WorkflowNotFoundException(String message) {
    super(message);
  }
}
