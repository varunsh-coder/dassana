package app.dassana.core.launch.model;

public class Request {

  private final String inputJson;

  //content management
  private boolean includeOriginalJson;
  private boolean skipGeneralContext;
  private boolean skipPolicyContext;
  private boolean skipS3Upload;
  private boolean refreshFromS3;

  //engine specific
  private boolean queueProcessing;
  private boolean skipPostProcessor;

  public Request(String inputJson) {
    this.inputJson = inputJson;
  }

  public boolean isIncludeOriginalJson() {
    return includeOriginalJson;
  }

  public void setIncludeOriginalJson(boolean includeOriginalJson) {
    this.includeOriginalJson = includeOriginalJson;
  }

  public boolean isRefreshFromS3() {
    return refreshFromS3;
  }

  public void setRefreshFromS3(boolean refreshFromS3) {
    this.refreshFromS3 = refreshFromS3;
  }

  public boolean isSkipS3Upload() {
    return skipS3Upload;
  }

  public void setSkipS3Upload(boolean skipS3Upload) {
    this.skipS3Upload = skipS3Upload;
  }

  public boolean isSkipPolicyContext() {
    return skipPolicyContext;
  }

  public void setSkipPolicyContext(boolean skipPolicyContext) {
    this.skipPolicyContext = skipPolicyContext;
  }

  public boolean isSkipGeneralContext() {
    return skipGeneralContext;
  }

  public void setSkipGeneralContext(boolean skipGeneralContext) {
    this.skipGeneralContext = skipGeneralContext;
  }

  public String getInputJson() {
    return inputJson;
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
