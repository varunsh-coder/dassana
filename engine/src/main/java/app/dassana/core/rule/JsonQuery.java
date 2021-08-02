package app.dassana.core.rule;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.concurrent.atomic.AtomicReference;
import javax.inject.Singleton;
import net.thisptr.jackson.jq.BuiltinFunctionLoader;
import net.thisptr.jackson.jq.Scope;
import net.thisptr.jackson.jq.Version;
import net.thisptr.jackson.jq.Versions;

@Singleton
public class JsonQuery {

  Scope rootScope = Scope.newEmptyScope();
  private static final ObjectMapper MAPPER = new ObjectMapper();

  public JsonQuery() {
    BuiltinFunctionLoader.getInstance().loadFunctions(Versions.JQ_1_6, rootScope);
  }

  public JsonNode query(String path, String jsonData) throws Exception {
    AtomicReference<JsonNode> jsonNodeToReturn = new AtomicReference<>();

    net.thisptr.jackson.jq.JsonQuery jsonQuery = net.thisptr.jackson.jq.JsonQuery.compile(path, Version.LATEST);
    JsonNode in = MAPPER.readTree(jsonData);
    Scope childScope = Scope.newChildScope(rootScope);
    jsonQuery.apply(childScope, in, jsonNodeToReturn::set);
    return jsonNodeToReturn.get();

  }

}
