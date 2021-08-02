package app.dassana.core.workflow.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Macro {

  String name;
  String jsonPayload;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getJsonPayload() {
    return jsonPayload;
  }

  public void setJsonPayload(String jsonPayload) {
    this.jsonPayload = jsonPayload;
  }
}
