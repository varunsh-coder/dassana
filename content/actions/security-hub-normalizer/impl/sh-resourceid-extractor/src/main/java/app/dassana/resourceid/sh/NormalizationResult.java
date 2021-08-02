package app.dassana.resourceid.sh;

import java.util.List;

public class NormalizationResult {

  private String csp;
  private String resourceContainer;
  private String region;
  private String service;
  private String resourceType;
  private String resourceId;
  private String alertId;
  private String arn;
  private String policyId;

  public NormalizationResult(String csp, String resourceType, String resourceId, String alertId) {
    this.csp = csp;
    this.resourceType = resourceType;
    this.resourceId = resourceId;
    this.alertId = alertId;
  }

  public String getPolicyId() {
    return policyId;
  }

  public void setPolicyId(String policyId) {
    this.policyId = policyId;
  }

  public String getArn() {
    return arn;
  }

  public void setArn(String arn) {
    this.arn = arn;
  }

  @Override
  public String toString() {
    return "ResourceIdAndAlert{" +
        "csp='" + csp + '\'' +
        ", resourceContainer='" + resourceContainer + '\'' +
        ", region='" + region + '\'' +
        ", service='" + service + '\'' +
        ", resourceType='" + resourceType + '\'' +
        ", resourceId='" + resourceId + '\'' +
        ", alertId='" + alertId + '\'' +
        '}';
  }

  public String getService() {
    return service;
  }

  public void setService(String service) {
    this.service = service;
  }

  public String getAlertId() {
    return alertId;
  }

  public void setAlertId(String alertId) {
    this.alertId = alertId;
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

  public String getResourceType() {
    return resourceType;
  }

  public void setResourceType(String resourceType) {
    this.resourceType = resourceType;
  }

  public String getRegion() {
    return region;
  }

  public void setRegion(String region) {
    this.region = region;
  }

  public String getResourceId() {
    return resourceId;
  }

  public void setResourceId(String resourceId) {
    this.resourceId = resourceId;
  }
}
