package app.dassana.action;

import java.util.Map;
import org.json.JSONObject;

public class ProcessAlert {


  Dassana getDassana(Map<String, Object> input) {
    Dassana dassana = new Dassana();

    JSONObject inputJsonObj = new JSONObject(input);

    if (inputJsonObj.has("dassana")) {

      JSONObject dassanaJsonObj = inputJsonObj.getJSONObject("dassana");

      //notice that we set a default value here because if this key is not available, Dassana Engine will get into
      // infinite loop. The reason is when SecurityHub updates a finding, it generates another event which matches
      // BatchImportFinding and since Dassana itself ran the update finding api, it will get its own alert json back
      dassana.setAlertS3Key(dassanaJsonObj.optString("alertKey", "UNAVAILABLE"));

      JSONObject generalContextJsonObj = dassanaJsonObj.optJSONObject("general-context");
      if (generalContextJsonObj != null) {
        JSONObject generalContextRiskJsonObj = generalContextJsonObj.optJSONObject("risk");
        dassana.setGeneralContextRiskValue(generalContextRiskJsonObj.optString("riskValue"));
        dassana.setGeneralContextRiskConditionName(generalContextRiskJsonObj.optString("name"));
      }

      JSONObject resourceContextJsonObj = dassanaJsonObj.optJSONObject("resource-context");
      if (resourceContextJsonObj != null) {
        JSONObject resourceContextRiskJsonObj = resourceContextJsonObj.optJSONObject("risk");
        dassana.setResourceContextRiskValue(resourceContextRiskJsonObj.optString("riskValue"));
        dassana.setResourceContextRiskConditionName(resourceContextRiskJsonObj.optString("name"));
      }

      JSONObject policyContextJsonObj = dassanaJsonObj.optJSONObject("policy-context");
      if (policyContextJsonObj != null) {
        JSONObject policyContextRiskJsonObj = policyContextJsonObj.optJSONObject("risk");
        dassana.setPolicyContextRiskValue(policyContextRiskJsonObj.optString("riskValue"));
        dassana.setPolicyContextRiskConditionName(policyContextRiskJsonObj.optString("name"));
      }

      // picking up values from dassana.normalize.output.alertClassification instead of dassana.policy-context
      JSONObject dassanaNormalizeJsonObj = dassanaJsonObj.optJSONObject("normalize");
      if (dassanaNormalizeJsonObj != null) {
        JSONObject dassanaNormalizeOutputJsonObj = dassanaNormalizeJsonObj.optJSONObject("output");
        JSONObject outputAlertClassificationJsonObj = dassanaNormalizeOutputJsonObj.optJSONObject(("alertClassification"));

        if (outputAlertClassificationJsonObj != null) {
          dassana.setAlertClass(outputAlertClassificationJsonObj.optString("class"));
          dassana.setAlertSubClass(outputAlertClassificationJsonObj.optString("subclass"));
          dassana.setAlertCategory(outputAlertClassificationJsonObj.optString("category"));
          dassana.setAlertSubCategory(outputAlertClassificationJsonObj.optString("subcategory"));
        }
      }


    } else {
      throw new RuntimeException("I can only process Dassana decorated events");
    }
    return dassana;

  }

}
