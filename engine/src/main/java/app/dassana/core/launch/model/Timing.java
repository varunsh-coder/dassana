package app.dassana.core.launch.model;

public class Timing {

  long totalTimeTaken;
  long normalize;
  long resourcePrioritization;
  long contextualization;

  public long getTotalTimeTaken() {
    return totalTimeTaken;
  }

  public void setTotalTimeTaken(long totalTimeTaken) {
    this.totalTimeTaken = totalTimeTaken;
  }

  public long getNormalize() {
    return normalize;
  }

  public void setNormalize(long normalize) {
    this.normalize = normalize;
  }

  public long getResourcePrioritization() {
    return resourcePrioritization;
  }

  public void setResourcePrioritization(long resourcePrioritization) {
    this.resourcePrioritization = resourcePrioritization;
  }

  public long getContextualization() {
    return contextualization;
  }

  public void setContextualization(long contextualization) {
    this.contextualization = contextualization;
  }
}
