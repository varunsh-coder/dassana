package app.dassana.core.workflow.model.runtime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AwsCreds {

  String accessKey;
  String secretKey;
  String sessionToken;

  public AwsCreds(String accessKey, String secretKey, String sessionToken) {
    this.accessKey = accessKey;
    this.secretKey = secretKey;
    this.sessionToken = sessionToken;
  }

  public String getAccessKey() {
    return accessKey;
  }

  public void setAccessKey(String accessKey) {
    this.accessKey = accessKey;
  }

  public String getSecretKey() {
    return secretKey;
  }

  public void setSecretKey(String secretKey) {
    this.secretKey = secretKey;
  }

  public String getSessionToken() {
    return sessionToken;
  }

  public void setSessionToken(String sessionToken) {
    this.sessionToken = sessionToken;
  }
}
