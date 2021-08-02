package app.dassana.core.workflow.model.runtime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AwsRuntimeContext {


  AwsCreds awsCreds;
  String resourceRegion;

  public AwsRuntimeContext(AwsCreds awsCreds, String resourceRegion) {
    this.awsCreds = awsCreds;
    this.resourceRegion = resourceRegion;
  }

  public AwsCreds getAwsCreds() {
    return awsCreds;
  }

  public void setAwsCreds(AwsCreds awsCreds) {
    this.awsCreds = awsCreds;
  }

  public String getResourceRegion() {
    return resourceRegion;
  }

  public void setResourceRegion(String resourceRegion) {
    this.resourceRegion = resourceRegion;
  }
}
