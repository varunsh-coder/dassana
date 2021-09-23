package app.dassana.action;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import software.amazon.awssdk.services.securityhub.SecurityHubAsyncClient;
import software.amazon.awssdk.services.securityhub.model.AwsSecurityFindingIdentifier;
import software.amazon.awssdk.services.securityhub.model.BatchUpdateFindingsRequest;
import software.amazon.awssdk.services.securityhub.model.BatchUpdateFindingsResponse;

public class App implements RequestHandler<Map<String, Object>, List<AwsSecurityFindingIdentifier>> {

  private final SecurityHubAsyncClient securityHubClient;

  public App() {
    securityHubClient = DependencyFactory.securityHubClient();
  }

  @Override
  public List<AwsSecurityFindingIdentifier> handleRequest(final Map<String, Object> input, final Context context) {

    JSONObject jsonObject = new JSONObject(input);
    JSONObject dassanaObject = jsonObject.getJSONObject("dassana");
    String productArn;

    String findingId = dassanaObject.getJSONObject("normalize").getJSONObject("output").getString("alertId");

    // only the EventBridge alert has detail and findings fields
    if (jsonObject.has("detail")) {
      productArn = jsonObject.getJSONObject("detail").getJSONArray("findings").getJSONObject(0)
          .getString("ProductArn");
    } else {
      productArn = jsonObject.getString("ProductArn");
    }

    AwsSecurityFindingIdentifier awsSecurityFindingIdentifier =
        AwsSecurityFindingIdentifier.builder().id(findingId).productArn(productArn)
            .build();

    ProcessAlert processAlert = new ProcessAlert();
    Dassana dassana = processAlert.getDassana(input);

    Map<String, String> userDefinedFields = new HashMap<>();

    userDefinedFields.put("DASSANA_ALERT_PATH", dassana.getAlertS3Key());
    userDefinedFields.put("DASSANA_GENERAL_CONTEXT_RISK", dassana.getGeneralContextRiskValue());
    userDefinedFields.put("DASSANA_GENERAL_CONTEXT_RISK_CONDITION_NAME", dassana.getGeneralContextRiskConditionName());
    userDefinedFields.put("DASSANA_RESOURCE_CONTEXT_RISK", dassana.getResourceContextRiskValue());
    userDefinedFields
        .put("DASSANA_RESOURCE_CONTEXT_RISK_CONDITION_NAME", dassana.getResourceContextRiskConditionName());
    userDefinedFields.put("DASSANA_POLICY_CONTEXT_RISK", dassana.getPolicyContextRiskValue());
    userDefinedFields.put("DASSANA_POLICY_CONTEXT_RISK_CONDITION_NAME", dassana.getPolicyContextRiskConditionName());
    userDefinedFields.put("DASSANA_POLICY_CONTEXT_CATEGORY", dassana.getPolicyContextCat());
    userDefinedFields.put("DASSANA_POLICY_CONTEXT_SUB_CATEGORY", dassana.getPolicyContextSubCat());

    try {
      BatchUpdateFindingsResponse batchUpdateFindingsResponse = securityHubClient
          .batchUpdateFindings(BatchUpdateFindingsRequest.builder().
              findingIdentifiers(awsSecurityFindingIdentifier).
              userDefinedFields(userDefinedFields)
              .build()).get();
      return batchUpdateFindingsResponse.processedFindings();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

  }
}
