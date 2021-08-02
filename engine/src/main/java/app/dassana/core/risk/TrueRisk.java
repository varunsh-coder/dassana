package app.dassana.core.risk;

public class TrueRisk {

  Risk contextualRisk;
  Risk resourcePriority;

  public Risk getContextualRisk() {
    return contextualRisk;
  }

  public void setContextualRisk(Risk risk) {
    this.contextualRisk = risk;
  }

  public Risk getResourcePriority() {
    return resourcePriority;
  }

  public void setResourcePriority(Risk resourcePriority) {
    this.resourcePriority = resourcePriority;
  }
}
