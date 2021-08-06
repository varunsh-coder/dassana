package app.dassana.core.normalize.model;

import app.dassana.core.workflow.model.Step;
import app.dassana.core.workflow.model.Workflow;
import java.util.LinkedList;
import java.util.List;

public class NormalizerWorkflow extends Workflow {

  private String vendorName;
  List<Step> postProcessorSteps;
  private boolean outputQueueEnabled;

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

  public String getVendorName() {
    return vendorName;
  }

  public void setVendorName(String vendorName) {
    this.vendorName = vendorName;
  }

}
