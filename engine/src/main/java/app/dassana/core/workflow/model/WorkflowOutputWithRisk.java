package app.dassana.core.workflow.model;

import app.dassana.core.risk.model.Risk;
import java.util.LinkedList;
import java.util.List;

public class WorkflowOutputWithRisk extends WorkflowOutput {

  Risk risk;
  List<Error> errorList;

  public List<Error> getErrorList() {
    if (errorList == null) {
      errorList = new LinkedList<>();
    }
    return errorList;
  }

  public void setErrorList(List<Error> errorList) {
    this.errorList = errorList;
  }

  public WorkflowOutputWithRisk() {
    risk = new Risk();
  }

  public Risk getRisk() {
    return risk;
  }

  public void setRisk(Risk risk) {
    this.risk = risk;
  }

  @Override
  public String toString() {
    return "WorkflowOutputWithRisk{" +
        "risk=" + risk +
        '}';
  }
}
