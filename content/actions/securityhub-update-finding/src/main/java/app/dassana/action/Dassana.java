package app.dassana.action;

public class Dassana {


  private boolean hasErrors;
  private String alertS3Key;
  private String generalContextRiskValue;
  private String generalContextRiskConditionName;
  private String resourceContextRiskValue;
  private String resourceContextRiskConditionName;
  private String policyContextRiskValue;
  private String policyContextRiskConditionName;
  private String alertCategory;
  private String alertSubCategory;
  private String alertClass;
  private String alertSubClass;

  public String getAlertCategory() {
    return alertCategory;
  }

  public void setAlertCategory(String alertCategory) {
    this.alertCategory = alertCategory;
  }

  public String getAlertSubCategory() { return alertSubCategory; }

  public void setAlertSubCategory(String alertSubCategory) {
    this.alertSubCategory = alertSubCategory;
  }

  public String getAlertClass() { return alertClass; }

  public void setAlertClass(String alertClass) { this.alertClass = alertClass; }

  public String getAlertSubClass() { return alertSubClass; }

  public void setAlertSubClass(String alertSubClass) { this.alertSubClass = alertSubClass; }

  public boolean isHasErrors() {
    return hasErrors;
  }

  public void setHasErrors(boolean hasErrors) {
    this.hasErrors = hasErrors;
  }

  public String getAlertS3Key() {
    return alertS3Key;
  }

  public void setAlertS3Key(String alertS3Key) {
    this.alertS3Key = alertS3Key;
  }

  public String getGeneralContextRiskValue() {
    return generalContextRiskValue;
  }

  public void setGeneralContextRiskValue(String generalContextRiskValue) {
    this.generalContextRiskValue = generalContextRiskValue;
  }

  public String getGeneralContextRiskConditionName() {
    return generalContextRiskConditionName;
  }

  public void setGeneralContextRiskConditionName(String generalContextRiskConditionName) {
    this.generalContextRiskConditionName = generalContextRiskConditionName;
  }

  public String getResourceContextRiskValue() {
    return resourceContextRiskValue;
  }

  public void setResourceContextRiskValue(String resourceContextRiskValue) {
    this.resourceContextRiskValue = resourceContextRiskValue;
  }

  public String getResourceContextRiskConditionName() {
    return resourceContextRiskConditionName;
  }

  public void setResourceContextRiskConditionName(String resourceContextRiskConditionName) {
    this.resourceContextRiskConditionName = resourceContextRiskConditionName;
  }

  public String getPolicyContextRiskValue() {
    return policyContextRiskValue;
  }

  public void setPolicyContextRiskValue(String policyContextRiskValue) {
    this.policyContextRiskValue = policyContextRiskValue;
  }

  public String getPolicyContextRiskConditionName() {
    return policyContextRiskConditionName;
  }

  public void setPolicyContextRiskConditionName(String policyContextRiskConditionName) {
    this.policyContextRiskConditionName = policyContextRiskConditionName;
  }
}
