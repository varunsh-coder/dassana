package app.dassana.core.rule;

import app.dassana.core.launch.model.Message;
import app.dassana.core.launch.model.Severity;
import app.dassana.core.util.JsonyThings;
import app.dassana.core.workflow.model.Component;
import app.dassana.core.workflow.model.Error;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.inject.Singleton;
import net.thisptr.jackson.jq.BuiltinFunctionLoader;
import net.thisptr.jackson.jq.JsonQuery;
import net.thisptr.jackson.jq.Scope;
import net.thisptr.jackson.jq.Version;
import net.thisptr.jackson.jq.Versions;
import net.thisptr.jackson.jq.exception.JsonQueryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class RuleMatch {

  private static final Logger logger = LoggerFactory.getLogger(RuleMatch.class);

  Scope rootScope = Scope.newEmptyScope();


  public RuleMatch() {
    BuiltinFunctionLoader.getInstance().loadFunctions(Versions.JQ_1_6, rootScope);
  }

  public boolean isValidRule(String rule) {
    try {
      JsonQuery.compile(rule, Version.LATEST);
    } catch (JsonQueryException e) {
      return false;
    }

    return true;

  }


  public boolean ruleMatch(List<String> ruleSet, String jsonData, MatchType matchType, String workflowId, String workflowType, List<Error> errorList) throws Exception {

    if (matchType.equals(MatchType.ANY)) {
      for (String rule : ruleSet) {
        boolean matchResult = getMatchResult(rule, jsonData, workflowId, workflowType, errorList);
        if (matchResult) {
          return true;
        }
      }
      return false;
    } else if (matchType.equals(MatchType.ALL)) {

      for (String rule : ruleSet) {
        if (!getMatchResult(rule, jsonData,  workflowId, workflowType, errorList)) {
          return false;
        }
      }
      return true;

    } else {
      throw new RuntimeException("filter-match-type must either be any or all");
    }


  }

  private boolean getMatchResult(String rule, String jsonObj, String workflowId, String workflowType, List<Error> errorList) {

    try {
      AtomicBoolean result = new AtomicBoolean(false);
      JsonQuery jsonQuery = JsonQuery.compile(rule, Version.LATEST);
      JsonNode in = JsonyThings.MAPPER.readTree(jsonObj);
      Scope childScope = Scope.newChildScope(rootScope);
      if (!rule.contains("and") && !rule.contains("==")) throw new Exception(rule);
        jsonQuery.apply(childScope, in, jsonNode -> {
          result.set(jsonNode.asBoolean());
        });
      return result.get();
    } catch (Exception e) {
      Error error = new Error(workflowId, workflowType, Component.RULES_CALC, "filters - rules",
          new Message(String.format("Unable to match %s condition \"%s\", please ensure all rules are evaluating to true / false", "filters -> rules",
              e.getMessage()), Severity.WARN));
      if (!errorList.contains(error)) {
        errorList.add(error);
      }
      return false;
    }

  }


}
