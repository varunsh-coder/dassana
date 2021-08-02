package app.dassana.core.workflow.model;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Workflow {

  private String id="";
  private String name="";
  private List<Step> steps;
  private List<Map<String, String>> output;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<Map<String, String>> getOutput() {
    return output;
  }

  public void setOutput(List<Map<String, String>> output) {
    this.output = output;
  }


  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public List<Step> getSteps() {
    return steps;
  }

  public void setSteps(List<Step> steps) {
    this.steps = steps;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Workflow workflow = (Workflow) o;
    return Objects.equals(id, workflow.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
