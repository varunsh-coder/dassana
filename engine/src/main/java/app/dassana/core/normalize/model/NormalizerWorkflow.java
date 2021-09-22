package app.dassana.core.normalize.model;

import app.dassana.core.workflow.model.Step;
import app.dassana.core.workflow.model.Workflow;
import java.util.LinkedList;
import java.util.List;

public class NormalizerWorkflow extends Workflow {

  private String vendorId;
  List<Step> postProcessorSteps;
  private boolean outputQueueEnabled;
  protected boolean skipGeneralContext=false;
  private boolean skipResourceContext = false;
  private boolean skipPolicyContext = false;
  private boolean publishToEventBridge=false;

  public boolean isPublishToEventBridge() {
    return publishToEventBridge;
  }

  public void setPublishToEventBridge(boolean publishToEventBridge) {
    this.publishToEventBridge = publishToEventBridge;
  }

  public boolean isSkipGeneralContext() {
    return skipGeneralContext;
  }

  public void setSkipGeneralContext(boolean skipGeneralContext) {
    this.skipGeneralContext = skipGeneralContext;
  }

  public boolean isSkipResourceContext() {
    return skipResourceContext;
  }

  public void setSkipResourceContext(boolean skipResourceContext) {
    this.skipResourceContext = skipResourceContext;
  }

  public boolean isSkipPolicyContext() {
    return skipPolicyContext;
  }

  public void setSkipPolicyContext(boolean skipPolicyContext) {
    this.skipPolicyContext = skipPolicyContext;
  }

  public List<Step> getPostProcessorSteps() {
    if (postProcessorSteps == null) {
      postProcessorSteps = new LinkedList<>();
    }
    return postProcessorSteps;
  }

  public void setPostProcessorSteps(List<Step> postProcessorSteps) {
    this.postProcessorSteps = postProcessorSteps;
  }

  public boolean isOutputQueueEnabled() {
    return outputQueueEnabled;
  }

  public void setOutputQueueEnabled(boolean outputQueueEnabled) {
    this.outputQueueEnabled = outputQueueEnabled;
  }

  public String getVendorId() {
    return vendorId;
  }

  public void setVendorId(String vendorId) {
    this.vendorId = vendorId;
  }

}
