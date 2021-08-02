package app.dassana.core.normalize.model;

public class NormalizedWorkFlowOutput {

  private String csp;
  private String resourceContainer;
  private String region;
  private String service;
  private String resourceType;
  private String resourceId;
  private String alertId;
  private String canonicalId;
  private String vendorPolicy;

  public String getVendorPolicy() {
    return vendorPolicy;
  }

  public void setVendorPolicy(String vendorPolicy) {
    this.vendorPolicy = vendorPolicy;
  }

  public String getCanonicalId() {
    return canonicalId;
  }

  public void setCanonicalId(String canonicalId) {
    this.canonicalId = canonicalId;
  }

  public String getResourceContainer() {
    return resourceContainer;
  }

  public void setResourceContainer(String resourceContainer) {
    this.resourceContainer = resourceContainer;
  }

  public String getCsp() {
    return csp;
  }

  public void setCsp(String csp) {
    this.csp = csp;
  }

  public String getRegion() {
    return region;
  }

  public void setRegion(String region) {
    this.region = region;
  }

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

  public String getResourceId() {
    return resourceId;
  }

  public void setResourceId(String resourceId) {
    this.resourceId = resourceId;
  }

  public String getAlertId() {
    return alertId;
  }

  public void setAlertId(String alertId) {
    this.alertId = alertId;
  }
}
