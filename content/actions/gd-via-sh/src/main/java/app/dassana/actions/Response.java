package app.dassana.actions;

public class Response {

  String alertId;
  String arn;
  String policyId;
  String csp;
  String resourceContainer;
  String region;
  String service;
  String resourceType;
  String resourceId;

  public String getAlertId() {
    return alertId;
  }

  public void setAlertId(String alertId) {
    this.alertId = alertId;
  }

  public String getArn() {
    return arn;
  }

  public void setArn(String arn) {
    this.arn = arn;
  }

  public String getPolicyId() {
    return policyId;
  }

  public void setPolicyId(String policyId) {
    this.policyId = policyId;
  }

  public String getCsp() {
    return csp;
  }

  public void setCsp(String csp) {
    this.csp = csp;
  }

  public String getResourceContainer() {
    return resourceContainer;
  }

  public void setResourceContainer(String resourceContainer) {
    this.resourceContainer = resourceContainer;
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
}
