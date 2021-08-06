package app.dassana.core.policycontext.model;

import app.dassana.core.risk.RiskConfig;
import app.dassana.core.workflow.model.Workflow;
import app.dassana.core.workflow.model.VendorFilter;
import java.util.Collections;
import java.util.List;

public class PolicyContext extends Workflow {

  String category = "";
  String subCategory = "";

  RiskConfig riskConfig;
  List<VendorFilter> vendorFilters;

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public String getSubCategory() {
    return subCategory;
  }

  public void setSubCategory(String subCategory) {
    this.subCategory = subCategory;
  }

  public RiskConfig getRiskConfig() {
    return riskConfig;
  }

  public void setRiskConfig(RiskConfig riskConfig) {
    this.riskConfig = riskConfig;
  }

  public List<VendorFilter> getVendorFilters() {
    return vendorFilters;
  }

  public void setVendorFilters(List<VendorFilter> vendorFilters) {
    Collections.reverse(vendorFilters);
    this.vendorFilters = vendorFilters;
  }
}
