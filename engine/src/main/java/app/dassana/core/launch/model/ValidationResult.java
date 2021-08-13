package app.dassana.core.launch.model;

import java.util.LinkedList;
import java.util.List;

public class ValidationResult {

  List<String> invalidRules = new LinkedList<>();
  ValidYaml validYaml;
  WorkFlowType validWorkflowType;

  public ValidYaml getValidYaml() {
    return validYaml;
  }

  public void setValidYaml(ValidYaml validYaml) {
    this.validYaml = validYaml;
  }

  public WorkFlowType getValidWorkflowType() {
    return validWorkflowType;
  }

  public void setValidWorkflowType(WorkFlowType validWorkflowType) {
    this.validWorkflowType = validWorkflowType;
  }

  public static class ValidYaml {

    boolean isValidYaml;
    String message;

    public boolean isValidYaml() {
      return isValidYaml;
    }

    public void setValidYaml(boolean validYaml) {
      isValidYaml = validYaml;
    }

    public String getMessage() {
      return message;
    }

    public void setMessage(String message) {
      this.message = message;
    }
  }

  public static class WorkFlowType {

    boolean isValidType;
    String message;

    public boolean isValidType() {
      return isValidType;
    }

    public void setValidType(boolean validType) {
      isValidType = validType;
    }

    public String getMessage() {
      return message;
    }

    public void setMessage(String message) {
      this.message = message;
    }
  }


  public List<String> getInvalidRules() {
    return invalidRules;
  }

  public void setInvalidRules(List<String> invalidRules) {
    this.invalidRules = invalidRules;
  }
}
