package app.dassana.core.rule;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class RuleMatch {

  private static final Logger logger = LoggerFactory.getLogger(RuleMatch.class);

  Scope rootScope = Scope.newEmptyScope();
  private static final ObjectMapper MAPPER = new ObjectMapper();


  public RuleMatch() {
    BuiltinFunctionLoader.getInstance().loadFunctions(Versions.JQ_1_6, rootScope);
  }


  public boolean ruleMatch(List<String> ruleSet, String jsonData, MatchType matchType) throws Exception {

    if (matchType.equals(MatchType.ANY)) {
      for (String rule : ruleSet) {
        return getMatchResult(rule, jsonData);
      }
      return false;
    } else if (matchType.equals(MatchType.ALL)) {

      for (String rule : ruleSet) {
        if (!getMatchResult(rule, jsonData)) {
          return false;
        }
      }
      return true;

    } else {
      throw new RuntimeException("filter-match-type must either be any or all");
    }


  }

  private boolean getMatchResult(String rule, String jsonObj) throws Exception {

    try {
      AtomicBoolean result = new AtomicBoolean(false);
      JsonQuery jsonQuery = JsonQuery.compile(rule, Version.LATEST);
      JsonNode in = MAPPER.readTree(jsonObj);
      Scope childScope = Scope.newChildScope(rootScope);
      jsonQuery.apply(childScope, in, jsonNode -> result.set(jsonNode.asBoolean()));
      return result.get();
    } catch (Exception e) {
      logger.error("Assuming the rule \"{}\" to not match due to error  against {}",rule,jsonObj,e);
      return false;
    }

  }


}
