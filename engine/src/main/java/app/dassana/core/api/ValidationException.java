package app.dassana.core.api;

public class ValidationException extends RuntimeException{

  public ValidationException() {
    super();
  }

  public ValidationException(String message) {
    super(message);
  }
}
