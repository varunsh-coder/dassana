package app.dassana.core.api;

import java.util.LinkedList;
import java.util.List;

public class DassanaWorkflowValidationException extends RuntimeException {

  List<String> issues;

  public List<String> getIssues() {
    if (issues == null) {
      issues = new LinkedList<>();
    }
    return issues;
  }

  public void setIssues(List<String> issues) {
    this.issues = issues;
  }

  public DassanaWorkflowValidationException() {
    super();
  }

  public DassanaWorkflowValidationException(String message) {
    super(message);
  }
}
