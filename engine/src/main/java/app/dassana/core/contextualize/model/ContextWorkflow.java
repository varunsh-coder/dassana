package app.dassana.core.contextualize.model;

import app.dassana.core.normalize.model.NormalizedWorkFlowOutput;
import app.dassana.core.risk.RiskConfig;
import app.dassana.core.workflow.model.Vendor;
import app.dassana.core.workflow.model.Workflow;
import java.util.Collections;
import java.util.List;

public class ContextWorkflow extends Workflow {

  String category = "";
  String subCategory = "";
  RiskConfig riskConfig;
  NormalizedWorkFlowOutput normalizedWorkFlowOutput;
  List<Vendor> vendors;

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

  public List<Vendor> getVendors() {
    return vendors;
  }

  public void setVendors(List<Vendor> vendors) {
    Collections.reverse(vendors);
    this.vendors = vendors;
  }
}
