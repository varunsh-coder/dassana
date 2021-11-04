//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package app.dassana.core.risk.eval;

import app.dassana.core.contentmanager.ContentManager;
import app.dassana.core.launch.model.Message;
import app.dassana.core.launch.model.Severity;
import app.dassana.core.risk.model.Risk;
import app.dassana.core.risk.model.Rule;
import app.dassana.core.risk.model.SubRule;
import app.dassana.core.workflow.model.Component;
import app.dassana.core.workflow.model.Error;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.inject.Singleton;
import net.thisptr.jackson.jq.BuiltinFunctionLoader;
import net.thisptr.jackson.jq.JsonQuery;
import net.thisptr.jackson.jq.Scope;
import net.thisptr.jackson.jq.Version;
import net.thisptr.jackson.jq.Versions;
import org.json.JSONObject;

@Singleton
public class RiskEvaluator {

  public RiskEvaluator() {
    BuiltinFunctionLoader.getInstance().loadFunctions(Versions.JQ_1_6, rootScope);
  }

  Scope rootScope = Scope.newEmptyScope();
  private static final ObjectMapper MAPPER = new ObjectMapper();


  // a string array to hold the risk severities in descending order (from critical to low)
  private static String[] riskValues = ContentManager.RISKS.getRiskValueArray();

  // Helper function to get the result of applying the condition to the input - this is used for rule evaluation
  public AtomicBoolean getResult(RiskEvalRequest input, String condition, Rule riskRule,
                                 String workflowId, String workflowType, List<Error> errorList ) {
    AtomicBoolean result = new AtomicBoolean(false);
    try {

      JsonQuery jsonQuery = JsonQuery.compile(condition, Version.LATEST);
      Scope childScope = Scope.newChildScope(rootScope);
      JSONObject jsonObject = new JSONObject(input.getJsonData());
      JsonNode in = MAPPER.readTree(jsonObject.toString());
      jsonQuery.apply(childScope, in, jsonNode -> result.set(jsonNode.asBoolean()));

      // changed the catch clause to handle exceptions locally - this allows better error message and continue process
    } catch (Exception e) {
      Error error = new Error(workflowId, workflowType, Component.RISK_CALC, riskRule.getId(),
          new Message(String.format("Unable to match rule %s condition %s", riskRule.getId(),
              riskRule.getCondition()), Severity.WARN));

      // to prevent duplicate error messages being displayed
      if (!errorList.contains(error)) {
        errorList.add(error);
      }
    }
    return result;
  }

  public void setMatchedRules(HashMap<String, List<Rule>> map, Rule rule) {
    // if there has been a same severity match
    if (map.containsKey(rule.getRisk())) {
      map.get(rule.getRisk()).add(rule);
      map.put(rule.getRisk(), map.get(rule.getRisk()));
    } else { // new risk severity
      List<Rule> ruleTemp = new LinkedList<>();
      ruleTemp.add(rule);
      map.put(rule.getRisk(), ruleTemp);
    }
  }

  public Risk evaluate(RiskEvalRequest input, List<Error> errorList, String workflowId, String workflowType) {
    Risk risk = new Risk(); // holds the rule risk
    Risk tempRisk = new Risk(); // holds the sub-rule risk - tempRisk has precedence over risk
    String defaultRisk = input.getDefaultRisk();
    risk.setRiskValue(defaultRisk);
    risk.setId("default");
    List<Rule> riskRules = input.getRiskRules();

    // map to hold the rules. key -> severity | value -> list of rules with same severity
    HashMap<String, List<Rule>> map = new HashMap<>();

    // parses through all the rules in the workflow
    for (Rule riskRule : riskRules) {
      String condition = riskRule.getCondition();
        AtomicBoolean result = getResult(input, condition, riskRule, workflowId, workflowType, errorList);

        // if the condition matches
        if (result.get()) {

          // gets the sub-rules if there are any
          List<SubRule> subRules = riskRule.getSubRules();

          // parses through all the sub-rules in the workflow
          for (SubRule subRule : subRules) {
            String subRiskCondition = subRule.getSubRiskCondition();
            Rule tempRule = new Rule(subRule);
            AtomicBoolean subResult = getResult(input, subRiskCondition, tempRule, workflowId, workflowType, errorList);

              // evaluates the condition
              if (subResult.get()) {
                Rule subRisk = new Rule(subRule.getSubRiskId(), subRule.getSubRiskCondition(), subRule.getSubRisk(), true, null);

                setMatchedRules(map, subRisk);
              }
          }

          setMatchedRules(map, riskRule);
        }
    }

    List<String> matched = new LinkedList<>();
    Boolean isSubRuleFound = false;
    Boolean isMainRuleFound = false;
    Boolean riskFound = false; // whether a higher risk has been found

    // to find the matching risk with the highest severity
    for (String rv : riskValues) { // parse through the severity array
      if (map.containsKey(rv)) { // if any of the matched rules have the same severity

        // need to parse through the array of matched risks with same severity to determine if it is sub-rule
        List<Rule> sameSeverityRules = map.get(rv); // list of matched rules with same severity
        for (Rule ssr : sameSeverityRules) {
          if (!riskFound && ssr.getIsSubRule() && !isSubRuleFound) { // if it's sub-rule; and it hasn't been matched before
            tempRisk.setRiskValue(ssr.getRisk());
            tempRisk.setId(ssr.getId());
            tempRisk.setCondition(ssr.getCondition());

            isSubRuleFound = true; // to prevent overwriting of the already matched sub-rule
          } else if (!riskFound && !ssr.getIsSubRule() && !isMainRuleFound) { // if it's main-rule; and it hasn't been matched before
            risk.setRiskValue(ssr.getRisk());
            risk.setId(ssr.getId());
            risk.setCondition(ssr.getCondition());

            isMainRuleFound = true; // to prevent overwriting of the already matched sub-rule
          } else { // once either sub-rule or rule have matched no need to re-match them
            matched.add(ssr.getId());
          }
        }

        // to prevent the lower severity risks from overwriting the higher severity ones
        if (isMainRuleFound || isSubRuleFound) {
          riskFound = true;
        }
      }
    }

    if (isSubRuleFound) {
      if (isMainRuleFound) { // since sub-rule takes precedence we add the id of the main rule to the matched list
        matched.add(risk.getId());
      }
      risk = tempRisk; // sub-rule takes precedence
    }

    return risk;
  }
}
