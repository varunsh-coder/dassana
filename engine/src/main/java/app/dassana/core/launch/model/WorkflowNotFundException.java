package app.dassana.core.launch.model;

public class WorkflowNotFundException extends RuntimeException{

  public WorkflowNotFundException() {
  }

  public WorkflowNotFundException(String message) {
    super(message);
  }
}
