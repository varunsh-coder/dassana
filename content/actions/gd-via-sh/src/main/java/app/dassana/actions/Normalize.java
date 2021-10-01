package app.dassana.actions;

import org.json.JSONArray;
import org.json.JSONObject;
import software.amazon.awssdk.arns.Arn;
import software.amazon.awssdk.arns.ArnResource;
import software.amazon.awssdk.utils.StringUtils;

public class Normalize {
  private JSONObject finding;
  private String policyId;

  public Normalize(JSONObject finding, String policyId) {
    this.finding = finding;
    this.policyId = policyId;
  }

  public Response normalize() {
    // rt -> resource type; ex. "Types": [ "TTPs/Initial Access/UnauthorizedAccess:EC2-SSHBruteForce" ]
    String rt;
    String resourceFromType = "";
    Response response = new Response();
    JSONObject resourceObject = null;
    ArnResource resource;

    if (policyId.toLowerCase().contains("ec2")) {
      // GuardDuty EC2
      rt = "AwsEc2Instance";
    } else if (policyId.toLowerCase().contains("iam")) {
      // GuardDuty IAM
      rt = "AwsIamAccessKey";
    } else if (policyId.toLowerCase().contains("s3")) {
      // GuardDuty S3
      rt = "AwsS3Bucket";
    } else {
      // No matches - set to empty, this will throw an exception on like 58
      rt = "";
    }

    response.setAlertId(finding.getString("Id"));
    response.setResourceContainer(finding.getString("AwsAccountId"));
    response.setVendorPolicy(policyId);
    response.setCsp("aws");
    response.setVendorId("guardduty");

    JSONArray resources = finding.getJSONArray("Resources");

    // In the case of incidents there are usually multiple resources involved.
    // for-loop through the resource object to find the right resource based on type (rt)
    for (int i = 0; i < resources.length(); i++) {
      if (resources.getJSONObject(i).optString("Type").equals(rt)) {
        resourceFromType = rt;
        // the correct resource matching the resource from Types array
        resourceObject = resources.getJSONObject(i);
        i = resources.length();
      }
    }
    if (resourceObject == null) {
      throw new RuntimeException("ERROR: There were no matching resource with type "+rt);
    }

    response.setRegion(resourceObject.getString("Region"));
    response.setArn(resourceObject.optString("Id"));

    // since IAM arn doesn't start with "arn"; the Arn.fromString() method will error out
    if ( ! resourceFromType.equals("AwsIamAccessKey")) {
      Arn arn = Arn.fromString(response.getArn());
      resource = arn.resource();
    } else {
      // in case of IAM recourse-type; resourceId will be picked up from response.getArn()
      resource = null;
    }

    String resourceId = "";
    if (resource != null && resource.resourceType().isPresent() && StringUtils.isNotBlank(resource.resourceType().get())) {
      resourceId = resource.resource();
    } else {
      String[] arnElements = response.getArn().split(":");
      String resourceInfo = arnElements[5];
      if (resourceInfo.contains("/")) {
        String[] resourceSplit = resourceInfo.split("/");
        resourceId = resourceSplit[2];
      } else {
        resourceId = resourceInfo;
      }
    }
    response.setResourceId(resourceId);

    return response;
  }
}

