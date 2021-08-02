package app.dassana.core.launch.model;

public class RequestConfig {

  private String inputJson;
  private boolean queueProcessing;
  private boolean skipPostProcessor;
  boolean skipResourcePrioritization;
  boolean skipResourceContextualization;
  boolean skipS3Upload;
  long requestArrivalTimeMs;
  boolean includeLoadedWorkflows;
  boolean refreshFromS3;

  public boolean isRefreshFromS3() {
    return refreshFromS3;
  }

  public void setRefreshFromS3(boolean refreshFromS3) {
    this.refreshFromS3 = refreshFromS3;
  }

  public boolean isIncludeLoadedWorkflows() {
    return includeLoadedWorkflows;
  }

  public void setIncludeLoadedWorkflows(boolean includeLoadedWorkflows) {
    this.includeLoadedWorkflows = includeLoadedWorkflows;
  }


  public long getRequestArrivalTimeMs() {
    return requestArrivalTimeMs;
  }

  public void setRequestArrivalTimeMs(long requestArrivalTimeMs) {
    this.requestArrivalTimeMs = requestArrivalTimeMs;
  }

  public boolean isSkipS3Upload() {
    return skipS3Upload;
  }

  public void setSkipS3Upload(boolean skipS3Upload) {
    this.skipS3Upload = skipS3Upload;
  }

  public boolean isSkipResourceContextualization() {
    return skipResourceContextualization;
  }

  public void setSkipResourceContextualization(boolean skipResourceContextualization) {
    this.skipResourceContextualization = skipResourceContextualization;
  }

  public boolean isSkipResourcePrioritization() {
    return skipResourcePrioritization;
  }

  public void setSkipResourcePrioritization(boolean skipResourcePrioritization) {
    this.skipResourcePrioritization = skipResourcePrioritization;
  }

  public String getInputJson() {
    return inputJson;
  }

  public void setInputJson(String inputJson) {
    this.inputJson = inputJson;
  }

  public boolean isQueueProcessing() {
    return queueProcessing;
  }

  public void setQueueProcessing(boolean queueProcessing) {
    this.queueProcessing = queueProcessing;
  }

  public boolean isSkipPostProcessor() {
    return skipPostProcessor;
  }

  public void setSkipPostProcessor(boolean skipPostProcessor) {
    this.skipPostProcessor = skipPostProcessor;
  }

}
