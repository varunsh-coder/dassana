package app.dassana.core.workflowmanager.model.normalize;

import app.dassana.core.workflowmanager.workflow.model.Step;
import app.dassana.core.workflowmanager.workflow.model.Workflow;
import java.util.LinkedList;
import java.util.List;

public class NormalizerWorkflow extends Workflow {

  private String vendorId;
  List<Step> postProcessorSteps;

  public List<Step> getPostProcessorSteps() {
    if (postProcessorSteps == null) {
      postProcessorSteps = new LinkedList<>();
    }
    return postProcessorSteps;
  }

  public void setPostProcessorSteps(List<Step> postProcessorSteps) {
    this.postProcessorSteps = postProcessorSteps;
  }

  public String getVendorId() {
    return vendorId;
  }

  public void setVendorId(String vendorId) {
    this.vendorId = vendorId;
  }

}
