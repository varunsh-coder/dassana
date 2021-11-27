package app.dassana.core.runmanager.launch.handler;

public class WorkflowNotFoundException extends RuntimeException{

  public WorkflowNotFoundException(String message) {
    super(message);
  }
}
