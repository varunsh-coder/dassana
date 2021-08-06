package app.dassana.core.workflow;


import app.dassana.core.policycontext.model.PolicyContext;
import app.dassana.core.rule.RuleMatch;
import app.dassana.core.workflow.model.Filter;
import app.dassana.core.workflow.model.VendorFilter;
import app.dassana.core.workflow.model.Workflow;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;


@Singleton
public class FilterMatch {

  @Inject RuleMatch ruleMatch;

  public Optional<Workflow> getMatchingWorkflow(Set<Workflow> workflowSet, String jsonData) throws Exception {
    for (Workflow workflow : workflowSet) {
      List<Filter> filters = new LinkedList<>();
      if (workflow instanceof PolicyContext) {
        List<VendorFilter> vendorFilters = ((PolicyContext) workflow).getVendorFilters();
        filters.addAll(vendorFilters);
      } else {
        filters = workflow.getFilters();
      }
      for (Filter filter : filters) {
        if (ruleMatch.ruleMatch(filter.getRules(), jsonData, filter.getMatchType())) {
          return Optional.of(workflow);
        }

      }
    }
    return Optional.empty();

  }


}
