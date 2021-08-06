package app.dassana.action;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;
import software.amazon.awssdk.services.securityhub.SecurityHubAsyncClient;
import software.amazon.awssdk.services.securityhub.model.AwsSecurityFindingIdentifier;
import software.amazon.awssdk.services.securityhub.model.BatchUpdateFindingsRequest;
import software.amazon.awssdk.services.securityhub.model.BatchUpdateFindingsResponse;
import software.amazon.awssdk.utils.StringUtils;

public class App implements RequestHandler<Map<String, Object>, BatchUpdateFindingsResponse> {

  private final SecurityHubAsyncClient securityHubClient;

  public App() {
    securityHubClient = DependencyFactory.securityHubClient();
  }

  @Override
  public BatchUpdateFindingsResponse handleRequest(final Map<String, Object> input, final Context context) {

    JSONObject jsonObject = new JSONObject(input);
    JSONObject dassanaObject = jsonObject.getJSONObject("dassana");

    String findingId = dassanaObject.getJSONObject("normalize").getJSONObject("output").getString("alertId");

    String productArn = jsonObject.getJSONObject("detail").getJSONArray("findings").getJSONObject(0)
        .getString("ProductArn");

    AwsSecurityFindingIdentifier awsSecurityFindingIdentifier =
        AwsSecurityFindingIdentifier.builder().id(findingId).productArn(productArn)
            .build();

    Map<String, String> userDefinedFields = new HashMap<>();

    String alertKey = dassanaObject.getString("alertKey");

    userDefinedFields.put("DASSANA_ALERT_PATH", alertKey);

    JSONObject resPriorityJsonObj = dassanaObject.optJSONObject("resourcePriority");
    String resourceRisk = "";
    String resourceRiskRuleName = "";
    if (resPriorityJsonObj != null) {
      JSONObject risk = resPriorityJsonObj.optJSONObject("risk");
      resourceRisk = risk.optString("riskValue");
      resourceRiskRuleName = risk.optString("name");
    }

    JSONObject contextObj = dassanaObject.optJSONObject("policy-context");
    String contextualRisk = "";
    String contextualRiskRuleName = "";

    if (contextObj != null) {
      JSONObject risk = contextObj.optJSONObject("risk");
      contextualRisk = risk.optString("riskValue");
      contextualRiskRuleName = risk.optString("name");
    }

    userDefinedFields
        .put("DASSANA_NORMALIZE_WORKFLOW_ID",
            dassanaObject.getJSONObject("normalize").getString("workflowId"));

    JSONObject optContext = dassanaObject.optJSONObject("context");
    if (optContext != null) {
      String contextWorkFlowId = optContext.optString("workflowId");
      if (StringUtils.isNotBlank(contextWorkFlowId)) {
        userDefinedFields
            .put("DASSANA_CONTEXT_WORKFLOW_ID", dassanaObject.getJSONObject("context").getString("workflowId"));

        userDefinedFields.put("DASSANA_CONTEXT_POLICY_CATEGORY", optContext.optString("category"));
        userDefinedFields.put("DASSANA_CONTEXT_POLICY_SUB_CATEGORY", optContext.optString("subCategory"));

      }
    }
    userDefinedFields.put("DASSANA_CONTEXT_RISK", contextualRisk);
    userDefinedFields.put("DASSANA_CONTEXT_RISK_RULE_NAME", contextualRiskRuleName);
    userDefinedFields.put("DASSANA_RESOURCE_PRIORITY", resourceRisk);
    userDefinedFields.put("DASSANA_RESOURCE_RISK_RULE_NAME", resourceRiskRuleName);

    //handle resource priority aka environment name

    JSONObject resourcePriority = dassanaObject.optJSONObject("resourcePriority");

    if (resourcePriority != null) {
      JSONArray output = resourcePriority.optJSONArray("output");

      if (output != null) {
        for (int i = 0; i < output.length(); i++) {
          JSONObject outputJSONObject = output.getJSONObject(i);

          String environment = outputJSONObject.optString("environment");

          if (StringUtils.isNotBlank(environment)) {
            userDefinedFields.put("DASSANA_RESOURCE_ENVIRONMENT", environment);
          }


        }
      }


    }

    try {
      return securityHubClient
          .batchUpdateFindings(BatchUpdateFindingsRequest.builder().
              findingIdentifiers(awsSecurityFindingIdentifier).
              userDefinedFields(userDefinedFields)
              .build()).get();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

  }
}
