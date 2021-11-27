package app.dassana.core.runmanager.contentmanager.validator;

public class ValidationException extends RuntimeException{

  public ValidationException() {
    super();
  }

  public ValidationException(String message) {
    super(message);
  }
}
