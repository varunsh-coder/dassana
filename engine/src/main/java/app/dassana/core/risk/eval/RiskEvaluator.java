//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package app.dassana.core.risk.eval;

import app.dassana.core.risk.model.Risk;
import app.dassana.core.risk.model.Rule;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
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

  public Risk evaluate(RiskEvalRequest input) {
    Risk risk = new Risk();
    String defaultRisk = input.getDefaultRisk();
    risk.setRiskValue(defaultRisk);
    risk.setName("default");
    List<Rule> riskRules = input.getRiskRules();

    for (app.dassana.core.risk.model.Rule riskRule : riskRules) {
      String condition = riskRule.getCondition();
      try {

        JsonQuery jsonQuery = JsonQuery.compile(condition, Version.LATEST);
        Scope childScope = Scope.newChildScope(rootScope);
        JSONObject jsonObject = new JSONObject(input.getJsonData());
        JsonNode in = MAPPER.readTree(jsonObject.toString());
        AtomicBoolean result = new AtomicBoolean(false);
        jsonQuery.apply(childScope, in, jsonNode -> result.set(jsonNode.asBoolean()));

        if (result.get()) {
          risk.setRiskValue(riskRule.getRisk());
          risk.setName(riskRule.getName());
          risk.setCondition(condition);
          break;
        }

      } catch (Exception e) {
        throw new RuntimeException(String.format("Unable to match rule %s condition %s", riskRule.getName(),
            riskRule.getCondition()),e);
      }
    }

    return risk;
  }
}
