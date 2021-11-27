package app.dassana.core.workflowmanager.model.policycontext;

import app.dassana.core.workflowmanager.model.resource.ResourceContext;
import app.dassana.core.workflowmanager.workflow.model.VendorFilter;
import java.util.Collections;
import java.util.List;

public class PolicyContext extends ResourceContext {

  String alertClass = "";
  String subClass = "";
  String category = "";
  String subCategory = "";

  List<VendorFilter> vendorFilters;

  public String getAlertClass() {
    return alertClass;
  }

  public void setAlertClass(String alertClass) {
    this.alertClass = alertClass;
  }

  public String getSubClass() {
    return subClass;
  }

  public void setSubClass(String subClass) {
    this.subClass = subClass;
  }

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


  public List<VendorFilter> getVendorFilters() {
    return vendorFilters;
  }

  public void setVendorFilters(List<VendorFilter> vendorFilters) {
    Collections.reverse(vendorFilters);
    this.vendorFilters = vendorFilters;
  }
}
