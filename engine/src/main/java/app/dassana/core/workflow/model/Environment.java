package app.dassana.core.workflow.model;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Environment {

  Map<String,String> envMap;

  public Map<String, String> getEnvMap() {
    return envMap;
  }

  public void setEnvMap(Map<String, String> envMap) {
    this.envMap = envMap;
  }
}
