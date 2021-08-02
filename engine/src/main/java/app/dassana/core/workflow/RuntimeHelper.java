package app.dassana.core.workflow;

import app.dassana.core.workflow.model.runtime.AwsCreds;
import app.dassana.core.workflow.model.runtime.AwsRuntimeContext;
import app.dassana.core.workflow.model.runtime.CrossAccountRoleAssumptionError;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.env.PropertySource;
import io.micronaut.core.util.StringUtils;
import java.util.Collection;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.GetFunctionRequest;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.model.AssumeRoleRequest;
import software.amazon.awssdk.services.sts.model.Credentials;
import software.amazon.awssdk.services.sts.model.StsException;

@Singleton
public class RuntimeHelper {


  private static final Logger logger = LoggerFactory.getLogger(RuntimeHelper.class);
  private String stackName;

  public static final String CROSS_ACCOUNT_ROLE_NAME_ENV_VAR = "dassanaCrossAccountRoleName";

  @Inject private ApplicationContext applicationContext;
  @Inject private LambdaClient lambdaClient;
  @Inject StsClient stsClient;


  private String currentAccountId = null;


  public String getStackName() {

    String dassanaStackName = System.getenv().get("DASSANA_STACK_NAME");
    if (StringUtils.isNotEmpty(dassanaStackName)) {
      return dassanaStackName;
    }

    final String DEFAULT_STACK_NAME = System.getenv().getOrDefault("", "dassana-core");

    if (StringUtils.isEmpty(stackName)) {
      try {
        Collection<PropertySource> propertySources = applicationContext.getEnvironment()
            .getPropertySources();

        for (PropertySource propertySource : propertySources) {
          Object aws_lambda_function_name = propertySource.get("AWS_LAMBDA_FUNCTION_NAME");
          if (aws_lambda_function_name != null) {
            Map<String, String> tags = lambdaClient
                .getFunction(
                    GetFunctionRequest.builder()
                        .functionName(String.valueOf(aws_lambda_function_name)).build())
                .tags();

            if (tags.containsKey("aws:cloudformation:stack-name")) {
              String stackNameFromTag = tags.get("aws:cloudformation:stack-name");
              stackName = stackNameFromTag;
              return stackNameFromTag;
            } else {
              logger
                  .warn(
                      "Unable to determine stack name from runtime tags, using default value {}. Following tags "
                          + "were found but we did not see aws:cloudformation:stack-name {}",
                      DEFAULT_STACK_NAME, tags);
            }

          }

        }
      } catch (Exception e) {
        logger
            .warn("Error in determining stack name using tags, using default value {}. Error was: ",
                DEFAULT_STACK_NAME, e);
      }
      logger.warn("Using default stack name {}", DEFAULT_STACK_NAME);
      stackName = DEFAULT_STACK_NAME;
    }
    return stackName;


  }

  public boolean crossAccountRoleRequired(String targetAccountId) {

    if (currentAccountId == null) {
      currentAccountId = stsClient.getCallerIdentity().account();
    }
    return !currentAccountId.contentEquals(targetAccountId);

  }

  AwsRuntimeContext getAwsRuntimeContext(String targetAccountId, String resourceRegion) {
    String roleArn = "";
    try {
      if (System.getenv().containsKey(CROSS_ACCOUNT_ROLE_NAME_ENV_VAR)) {

        String crossAccountRoleName = System.getenv(CROSS_ACCOUNT_ROLE_NAME_ENV_VAR);

        roleArn = "arn:aws:iam::".concat(targetAccountId).concat(":role/")
            .concat(crossAccountRoleName);
        Credentials credentials = stsClient.assumeRole(AssumeRoleRequest.builder().roleArn(roleArn)
            .roleSessionName("dassanaCrossAccountSession")
            .build())
            .credentials();

        return new AwsRuntimeContext(new AwsCreds(credentials.accessKeyId(),
            credentials.secretAccessKey(), credentials.sessionToken()), resourceRegion);


      } else {
        throw new CrossAccountRoleAssumptionError(
            "CrossAccountRole not available, make sure the engine is running with environment variable dassanaCrossAccountRoleName set");
      }
    } catch (StsException e) {
      throw new CrossAccountRoleAssumptionError(
          String.format(
              "Cross Account Role couldn't be assumed on target account %s using role arn %s",
              targetAccountId, roleArn));
    }

  }

}
