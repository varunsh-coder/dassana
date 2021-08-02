package app.dassana.resourceid.sh;

import org.json.JSONArray;
import org.json.JSONObject;
import software.amazon.awssdk.arns.Arn;
import software.amazon.awssdk.arns.ArnResource;

public class Normalize {

  private final String jsonAlertData;

  public Normalize(String jsonAlertData) {
    this.jsonAlertData = jsonAlertData;
  }

  public NormalizationResult normalize() {

    JSONObject jsonObject = new JSONObject(jsonAlertData);
    JSONObject detailJsonObject = jsonObject.getJSONObject("detail");
    JSONArray jsonArray = detailJsonObject.getJSONArray("findings");

    String policyId = jsonArray.getJSONObject(0).getJSONObject("ProductFields").getString("RelatedAWSResources:0/name");

    String resourceArn = jsonArray.getJSONObject(0).getJSONObject("ProductFields").getString("Resources:0/Id");

    String findingId = jsonArray.getJSONObject(0).getString("Id");

    Arn arn = Arn.fromString(resourceArn);

    ArnResource resource = arn.resource();

    String resourceType = "";
    if (resource.resourceType().isPresent()) {
      resourceType = resource.resourceType().get();
    }

    String resourceId;

    String[] arnElements = resourceArn.split(":");
    resourceId = arnElements[arnElements.length - 1];

    if (resourceId.contains("/")) {
      String[] split = resourceId.split("/");
      resourceId = split[1];

    }
    NormalizationResult normalizationResult = new NormalizationResult("aws", resourceType, resourceId, findingId);
    normalizationResult.setArn(resourceArn);

    normalizationResult.setPolicyId(policyId);

    if (arn.region().isPresent()) {
      normalizationResult.setRegion(arn.region().get());
    } else {
      normalizationResult.setRegion("");
    }

    normalizationResult.setService(arn.service());

    if (arn.accountId().isPresent()) {
      normalizationResult.setResourceContainer(arn.accountId().get());
    }//set the tags

    normalizationResult.setArn(resourceArn);
    return normalizationResult;

  }


}
