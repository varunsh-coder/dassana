package app.dassana.core.launch.model;

import app.dassana.core.normalize.model.NormalizerWorkflow;

public class ProcessingResponse {

  String decoratedJson;
  NormalizerWorkflow normalizerWorkflow;

  public NormalizerWorkflow getNormalizerWorkflow() {
    return normalizerWorkflow;
  }

  public void setNormalizerWorkflow(NormalizerWorkflow normalizerWorkflow) {
    this.normalizerWorkflow = normalizerWorkflow;
  }

  public String getDecoratedJson() {
    return decoratedJson;
  }

  public void setDecoratedJson(String decoratedJson) {
    this.decoratedJson = decoratedJson;
  }
}
