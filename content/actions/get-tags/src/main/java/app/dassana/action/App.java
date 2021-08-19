package app.dassana.action;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import software.amazon.awssdk.arns.Arn;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.resourcegroupstaggingapi.ResourceGroupsTaggingApiClient;
import software.amazon.awssdk.services.resourcegroupstaggingapi.model.GetResourcesRequest;
import software.amazon.awssdk.services.resourcegroupstaggingapi.model.ResourceTagMapping;
import software.amazon.awssdk.services.resourcegroupstaggingapi.model.Tag;
import software.amazon.awssdk.utils.StringUtils;

/**
 * Lambda function entry point. You can change to use other pojo type or implement a different RequestHandler.
 *
 * @see <a href=https://docs.aws.amazon.com/lambda/latest/dg/java-handler.html>Lambda Java
 * Handler</a> for more information
 */
public class App implements RequestHandler<Map<String, String>, Object> {

  private boolean credsAvailableViaContext(Context context) {

    if (context != null && context.getClientContext() != null && context.getClientContext().getCustom() != null) {
      String aws_access_key_id = context.getClientContext().getCustom().get("AWS_ACCESS_KEY_ID");
      String aws_secret_access_key = context.getClientContext().getCustom()
          .get("AWS_SECRET_ACCESS_KEY");
      String session_token = context.getClientContext().getCustom().get("AWS_SESSION_TOKEN");

      return aws_access_key_id != null && aws_secret_access_key != null && session_token != null;

    }

    return false;
  }

  @Override
  public List<app.dassana.action.Tag> handleRequest(final Map<String, String> input,
      final Context context) {

    String region = input.get("region");

    if (region.toLowerCase().contentEquals("global")) {
      region = "us-east-1";//some of the CSPM vendors provide region as "global" which is not a "valid" region
      // technically speaking. So we default to us-east-1  where ,anecdotally, where the global services run like IAM
      // etc
    }

    ResourceGroupsTaggingApiClient client;

    boolean credsAvailableViaContext = credsAvailableViaContext(context);

    if (!credsAvailableViaContext) {//this means that we are running in the same account as Dassana Engine
      client = ResourceGroupsTaggingApiClient.builder().
          region(Region.of(region)).
          build();
    } else {//cross account role creds are available, the resource is in a different account
      client = ResourceGroupsTaggingApiClient.builder().
          credentialsProvider(
              () -> AwsSessionCredentials
                  .create(context.getClientContext().getCustom().get("AWS_ACCESS_KEY_ID"),
                      context.getClientContext().getCustom().get("AWS_SECRET_ACCESS_KEY"),
                      context.getClientContext().getCustom().get("AWS_SESSION_TOKEN")
                  )).
          region(Region.of(region)).
          build();
    }

    String arn = input.get("arn");

    Arn awsArn = Arn.fromString(arn);

    boolean validArn = true;
    if (awsArn.region().isPresent()) {
      if (awsArn.resource().resource().contentEquals("root") && StringUtils.isBlank(awsArn.region().get())) {
        validArn = false;
      }

    }

    if (validArn) {
      List<ResourceTagMapping> resourceTagMappings = client
          .getResources(GetResourcesRequest.builder().resourceARNList(arn).build())
          .resourceTagMappingList();

      List<app.dassana.action.Tag> tagList = new LinkedList<>();
      for (ResourceTagMapping resourceTagMapping : resourceTagMappings) {
        List<Tag> tags = resourceTagMapping.tags();
        for (Tag tag : tags) {
          app.dassana.action.Tag tag1 = new app.dassana.action.Tag();
          tag1.setKey(tag.key());
          tag1.setValue(tag.value());
          tagList.add(tag1);
        }

      }
      return tagList;
    } else {
      return new LinkedList<>();
    }

  }
}
