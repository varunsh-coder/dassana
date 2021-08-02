package app.dassana.core.normalize.model;

public class NormalizationResult {

  private NormalizedWorkFlowOutputWithId normalizedWorkFlowOutputWithId;
  private NormalizerWorkflow normalizerWorkflowUsed;
  private Long timeTaken;

  public Long getTimeTaken() {
    return timeTaken;
  }

  public void setTimeTaken(Long timeTaken) {
    this.timeTaken = timeTaken;
  }


  public NormalizedWorkFlowOutputWithId getNormalizedWorkFlowOutputWithId() {
    return normalizedWorkFlowOutputWithId;
  }

  public void setNormalizedWorkFlowOutputWithId(
      NormalizedWorkFlowOutputWithId normalizedWorkFlowOutputWithId) {
    this.normalizedWorkFlowOutputWithId = normalizedWorkFlowOutputWithId;
  }

  public NormalizerWorkflow getNormalizerWorkflowUsed() {
    return normalizerWorkflowUsed;
  }

  public void setNormalizerWorkflowUsed(NormalizerWorkflow normalizerWorkflowUsed) {
    this.normalizerWorkflowUsed = normalizerWorkflowUsed;
  }
}