package app.dassana.actions;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;
import software.amazon.awssdk.arns.Arn;
import software.amazon.awssdk.arns.ArnResource;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.utils.StringUtils;

public class App implements RequestHandler<Map<String, Object>, Response> {

  public App() {
  }

  @Override
  public Response handleRequest(final Map<String, Object> input, final Context context) {
    JSONObject inputJson = new JSONObject(input);
    Response response = new Response();

    JSONObject finding = inputJson.getJSONObject("detail").getJSONArray("findings").getJSONObject(0);
    response.setAlertId(finding.getString("Id"));
    response.setArn(finding.getJSONArray("Resources").getJSONObject(0).getString("Id"));
    response.setPolicyId(finding.getJSONObject("FindingProviderFields").getJSONArray("Types").getString(0));
    response.setCsp("aws");
    response.setResourceContainer(finding.getString("AwsAccountId"));
    response.setRegion(finding.getJSONArray("Resources").getJSONObject(0).getString("Region"));
    response.setService(Arn.fromString(response.getArn()).service());
    Arn arn = Arn.fromString(response.getArn());
    ArnResource resource = arn.resource();

    String resourceType = "";
    String resourceId = "";
    if (resource.resourceType().isPresent() && StringUtils.isNotBlank(resource.resourceType().get())) {
      resourceType = resource.resourceType().get();
      resourceId = resource.resource();
    } else {

      String[] arnElements = response.getArn().split(":");
      String resourceInfo = arnElements[5];
      if (resourceInfo.contains("/")) {
        String[] resourceSplit = resourceInfo.split("/");
        resourceType = resourceSplit[1];
        resourceId = resourceSplit[2];
      }

    }
    response.setResourceType(resourceType);
    response.setResourceId(resourceId);

    return response;
  }
}
