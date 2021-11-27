package app.dassana.core.workflowmanager.workflow.model;

import java.util.Map;

public class Environment {

  Map<String,String> envMap;

  public Map<String, String> getEnvMap() {
    return envMap;
  }

  public void setEnvMap(Map<String, String> envMap) {
    this.envMap = envMap;
  }
}
