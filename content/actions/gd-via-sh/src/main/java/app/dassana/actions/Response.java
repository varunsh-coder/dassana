package app.dassana.actions;

public class Response {

  private String csp;
  private String resourceContainer;
  private String region;
  private String resourceId;
  private String alertId;
  private String arn;
  private String vendorPolicy;
  private String vendorId;

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

  public String getVendorPolicy() {
    return vendorPolicy;
  }

  public void setVendorPolicy(String vendorPolicy) {
    this.vendorPolicy = vendorPolicy;
  }

  public String getVendorId() {
    return vendorId;
  }

  public void setVendorId(String vendorId) {
    this.vendorId = vendorId;
  }

  public String getResourceId() {
    return resourceId;
  }

  public void setResourceId(String resourceId) {
    this.resourceId = resourceId;
  }
}
