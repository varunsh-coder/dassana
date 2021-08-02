package app.dassana.core.workflow.model;

import java.util.List;
import java.util.Map;

public class Step {

  private String id;
  private String uses;
  private List<Map<String, String>> fields;

  public List<Map<String, String>> getFields() {
    return fields;
  }

  public void setFields(List<Map<String, String>> fields) {
    this.fields = fields;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getUses() {
    return uses;
  }

  public void setUses(String uses) {
    this.uses = uses;
  }


}
