//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package app.dassana.action;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.function.aws.MicronautRequestHandler;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import net.thisptr.jackson.jq.BuiltinFunctionLoader;
import net.thisptr.jackson.jq.JsonQuery;
import net.thisptr.jackson.jq.Scope;
import net.thisptr.jackson.jq.Version;
import net.thisptr.jackson.jq.Versions;
import org.json.JSONObject;

@Introspected
public class RequestHandler extends MicronautRequestHandler<Request, Risk> {

  Scope rootScope = Scope.newEmptyScope();
  private static final ObjectMapper MAPPER = new ObjectMapper();


  public RequestHandler() {
    BuiltinFunctionLoader.getInstance().loadFunctions(Versions.JQ_1_6, rootScope);
  }

  public Risk execute(Request input) {
    Risk risk = new Risk();
    String defaultRisk = input.getDefaultRisk();
    risk.setRisk(defaultRisk);
    risk.setName("default");
    List<Rule> riskRules = input.getRiskRules();

    for (Rule riskRule : riskRules) {
      String condition = riskRule.getCondition();
      try {

        JsonQuery jsonQuery = JsonQuery.compile(condition, Version.LATEST);
        Scope childScope = Scope.newChildScope(rootScope);
        JsonNode in = MAPPER.readTree(new JSONObject(input.getJsonData()).toString());
        AtomicBoolean result = new AtomicBoolean(false);
        jsonQuery.apply(childScope, in, jsonNode -> result.set(jsonNode.asBoolean()));

        if (result.get()) {
          risk.setRisk(riskRule.getRisk());
          risk.setName(riskRule.getName());
          risk.setCondition(condition);
          break;
        }

      } catch (JsonProcessingException e) {
        throw new RuntimeException(e);
      }
    }

    return risk;
  }
}
