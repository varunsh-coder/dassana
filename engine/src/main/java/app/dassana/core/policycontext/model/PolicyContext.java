package app.dassana.core.policycontext.model;

import app.dassana.core.resource.model.GeneralContext;
import app.dassana.core.workflow.model.VendorFilter;
import java.util.Collections;
import java.util.List;

public class PolicyContext extends GeneralContext {

  String category = "";
  String subCategory = "";

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


  public List<VendorFilter> getVendorFilters() {
    return vendorFilters;
  }

  public void setVendorFilters(List<VendorFilter> vendorFilters) {
    Collections.reverse(vendorFilters);
    this.vendorFilters = vendorFilters;
  }
}
