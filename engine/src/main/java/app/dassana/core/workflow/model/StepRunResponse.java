package app.dassana.core.workflow.model;

import app.dassana.core.workflow.infra.LambdaStepRunner.RuntimeContext;

public class StepRunResponse {

  String response;
  RuntimeContext runtimeContext;

  public String getResponse() {
    return response;
  }

  public void setResponse(String response) {
    this.response = response;
  }

  public RuntimeContext getRuntimeContext() {
    return runtimeContext;
  }

  public void setRuntimeContext(
      RuntimeContext runtimeContext) {
    this.runtimeContext = runtimeContext;
  }
}
