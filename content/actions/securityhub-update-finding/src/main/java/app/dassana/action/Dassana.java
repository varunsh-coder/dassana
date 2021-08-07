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
  private String policyContextCat;
  private String policyContextSubCat;

  public String getPolicyContextCat() {
    return policyContextCat;
  }

  public void setPolicyContextCat(String policyContextCat) {
    this.policyContextCat = policyContextCat;
  }

  public String getPolicyContextSubCat() {
    return policyContextSubCat;
  }

  public void setPolicyContextSubCat(String policyContextSubCat) {
    this.policyContextSubCat = policyContextSubCat;
  }

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
