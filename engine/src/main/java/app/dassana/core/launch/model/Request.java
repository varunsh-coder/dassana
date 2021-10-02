package app.dassana.core.launch.model;

import java.util.List;

public class Request {

  private final String inputJson;


  boolean includeStepOutput;
  boolean includeAlertInOutput;
  boolean isDefault;

  private boolean refreshFromS3;
  private List<String> additionalWorkflowYamls;

  private String workflowId; //if specified, only this workflow will run

  //engine specific
  private boolean skipPostProcessor;

  public Request(String inputJson) {
    this.inputJson = inputJson;
  }

  public boolean isIncludeStepOutput() {
    return includeStepOutput;
  }

  public boolean isIncludeAlertInOutput() {
    return includeAlertInOutput;
  }

  public void setIncludeAlertInOutput(boolean includeAlertInOutput) {
    this.includeAlertInOutput = includeAlertInOutput;
  }

  public void setIncludeStepOutput(boolean includeStepOutput) {
    this.includeStepOutput = includeStepOutput;
  }

  public String getWorkflowId() {
    return workflowId;
  }

  public void setWorkflowId(String workflowId) {
    this.workflowId = workflowId;
  }

  public boolean isRefreshFromS3() {
    return refreshFromS3;
  }

  public List<String> getAdditionalWorkflowYamls() {
    return additionalWorkflowYamls;
  }

  public void setAdditionalWorkflowYamls(List<String> additionalWorkflowYamls) {
    this.additionalWorkflowYamls = additionalWorkflowYamls;
  }

  public void setRefreshFromS3(boolean refreshFromS3) {
    this.refreshFromS3 = refreshFromS3;
  }


  public String getInputJson() {
    return inputJson;
  }

  public boolean isSkipPostProcessor() {
    return skipPostProcessor;
  }

  public void setSkipPostProcessor(boolean skipPostProcessor) {
    this.skipPostProcessor = skipPostProcessor;
  }

  public boolean isDefault() {
    return isDefault;
  }

  public void setDefault(boolean isDefault) {
    this.isDefault = isDefault;
  }
}
