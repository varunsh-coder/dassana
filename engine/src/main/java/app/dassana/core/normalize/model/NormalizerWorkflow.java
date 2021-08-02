package app.dassana.core.normalize.model;

import app.dassana.core.rule.MatchType;
import app.dassana.core.workflow.model.Step;
import app.dassana.core.workflow.model.Workflow;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class NormalizerWorkflow extends Workflow {

  private List<String> filterRules;
  private MatchType matchType;
  private boolean outputQueueEnabled;
  private String alertId;
  private String alertIdJsonPath;
  private String vendorName;
  private String stepId;
  private String canonicalIdJqPath;
  private String vendorPolicyJqPath;
  private String cspJqPath;
  private String resourceContainerJqPath;
  private String regionJqPath;
  private String serviceJqPath;
  private String resourceTypeJqPath;
  private String resourceIdJqPath;
  List<Step> postProcessorSteps;



  public String getAlertId() {
    return alertId;
  }

  public void setAlertId(String alertId) {
    this.alertId = alertId;
  }

  public String getAlertIdJsonPath() {
    return alertIdJsonPath;
  }

  public void setAlertIdJsonPath(String alertIdJsonPath) {
    this.alertIdJsonPath = alertIdJsonPath;
  }

  public List<Step> getPostProcessorSteps() {
    if (postProcessorSteps == null) {
      postProcessorSteps = new LinkedList<>();
    }
    return postProcessorSteps;
  }

  public void setPostProcessorSteps(List<Step> postProcessorSteps) {
    this.postProcessorSteps = postProcessorSteps;
  }

  public String getCanonicalIdJqPath() {
    return canonicalIdJqPath;
  }

  public void setCanonicalIdJqPath(String canonicalIdJqPath) {
    this.canonicalIdJqPath = canonicalIdJqPath;
  }

  public String getVendorPolicyJqPath() {
    return vendorPolicyJqPath;
  }

  public void setVendorPolicyJqPath(String vendorPolicyJqPath) {
    this.vendorPolicyJqPath = vendorPolicyJqPath;
  }

  public String getCspJqPath() {
    return cspJqPath;
  }

  public void setCspJqPath(String cspJqPath) {
    this.cspJqPath = cspJqPath;
  }

  public String getResourceContainerJqPath() {
    return resourceContainerJqPath;
  }

  public void setResourceContainerJqPath(String resourceContainerJqPath) {
    this.resourceContainerJqPath = resourceContainerJqPath;
  }

  public String getRegionJqPath() {
    return regionJqPath;
  }

  public void setRegionJqPath(String regionJqPath) {
    this.regionJqPath = regionJqPath;
  }

  public String getServiceJqPath() {
    return serviceJqPath;
  }

  public void setServiceJqPath(String serviceJqPath) {
    this.serviceJqPath = serviceJqPath;
  }

  public String getResourceTypeJqPath() {
    return resourceTypeJqPath;
  }

  public void setResourceTypeJqPath(String resourceTypeJqPath) {
    this.resourceTypeJqPath = resourceTypeJqPath;
  }

  public String getResourceIdJqPath() {
    return resourceIdJqPath;
  }

  public void setResourceIdJqPath(String resourceIdJqPath) {
    this.resourceIdJqPath = resourceIdJqPath;
  }

  public String getStepId() {
    return stepId;
  }

  public void setStepId(String stepId) {
    this.stepId = stepId;
  }

  public MatchType getMatchType() {
    return matchType;
  }

  public void setMatchType(MatchType matchType) {
    this.matchType = matchType;
  }

  public List<String> getFilterRules() {
    return filterRules;
  }

  public void setFilterRules(List<String> filterRules) {
    Collections.reverse(filterRules);
    this.filterRules = filterRules;
  }

  public boolean isOutputQueueEnabled() {
    return outputQueueEnabled;
  }

  public void setOutputQueueEnabled(boolean outputQueueEnabled) {
    this.outputQueueEnabled = outputQueueEnabled;
  }

  public String getVendorName() {
    return vendorName;
  }

  public void setVendorName(String vendorName) {
    this.vendorName = vendorName;
  }

}
