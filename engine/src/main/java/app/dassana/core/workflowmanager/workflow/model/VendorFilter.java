package app.dassana.core.workflowmanager.workflow.model;

import java.util.List;

public class VendorFilter extends Filter{
  String name;
  List<String> policies;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<String> getPolicies() {
    return policies;
  }

  public void setPolicies(List<String> policies) {
    this.policies = policies;
  }
}
