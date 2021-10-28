package app.dassana.core.workflow;


import app.dassana.core.api.DassanaWorkflowValidationException;
import app.dassana.core.policycontext.model.PolicyContext;
import app.dassana.core.rule.RuleMatch;
import app.dassana.core.workflow.model.Error;
import app.dassana.core.workflow.model.Filter;
import app.dassana.core.workflow.model.VendorFilter;
import app.dassana.core.workflow.model.Workflow;
import app.dassana.core.launch.model.Message;
import app.dassana.core.launch.model.Severity;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import app.dassana.core.workflow.model.Component;

@Singleton
public class FilterMatch {

  @Inject RuleMatch ruleMatch;
  private static final Logger logger = LoggerFactory.getLogger(RuleMatch.class);

  public Optional<Workflow> getMatchingWorkflow(Set<Workflow> workflowSet, String jsonData, List<Error> errorList ) throws Exception {
    boolean b;
    for (Workflow workflow : workflowSet) {
      List<Filter> filters = new LinkedList<>();
      if (workflow instanceof PolicyContext) {
        List<VendorFilter> vendorFilters = ((PolicyContext) workflow).getVendorFilters();
        filters.addAll(vendorFilters);
      } else {
        filters = workflow.getFilters();
      }
      for (Filter filter : filters) {
//        try {
          b = ruleMatch.ruleMatch(filter.getRules(), jsonData, filter.getMatchType(), workflow.getId(), workflow.getType(), errorList);
          if (b) {
            return Optional.of(workflow);
          }

//        }
//        catch (DassanaWorkflowValidationException dassanaWorkflowValidationException) {
////          List<String> messages = new LinkedList<>();
//          String tempMessage = dassanaWorkflowValidationException.getMessage();
////          messages.addAll(dassanaWorkflowValidationException.getMessage());
//
//
//          Error error = new Error(workflow.getId(), workflow.getType(), Component.RULES_CALC, "filters - rules",
//              new Message(String.format("Unable to match %s condition %s", "filters",
//                  tempMessage), Severity.WARN));
//          if (!errorList.contains(error)) {
//            errorList.add(error);
//          }
//        }


      }
    }
    return Optional.empty();

  }


}
