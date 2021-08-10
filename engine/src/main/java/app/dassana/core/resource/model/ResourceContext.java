package app.dassana.core.resource.model;

import app.dassana.core.risk.model.RiskConfig;

public class ResourceContext extends GeneralContext {

  String service;
  String resourceType;

  public String getService() {
    return service;
  }

  public void setService(String service) {
    this.service = service;
  }

  public String getResourceType() {
    return resourceType;
  }

  public void setResourceType(String resourceType) {
    this.resourceType = resourceType;
  }

  RiskConfig riskConfig;

  public RiskConfig getRiskConfig() {
    return riskConfig;
  }

  public void setRiskConfig(RiskConfig riskConfig) {
    this.riskConfig = riskConfig;
  }
}
