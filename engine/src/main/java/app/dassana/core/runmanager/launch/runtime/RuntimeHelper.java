package app.dassana.core.runmanager.launch.runtime;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Value;
import io.micronaut.core.util.StringUtils;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.model.AssumeRoleRequest;
import software.amazon.awssdk.services.sts.model.Credentials;
import software.amazon.awssdk.services.sts.model.StsException;

@Singleton
public class RuntimeHelper {


  private static final Logger logger = LoggerFactory.getLogger(RuntimeHelper.class);

  @Value("${env.dassanaStackName}")
  private String stackName;

  @Value("${env.dassanaCrossAccountRoleName}")
  private String dassanaCrossAccountRoleName;

  public static final String CROSS_ACCOUNT_ROLE_NAME_ENV_VAR = "dassanaCrossAccountRoleName";

  @Inject private ApplicationContext applicationContext;
  @Inject private LambdaClient lambdaClient;
  @Inject StsClient stsClient;

  public String getDassanaCrossAccountRoleName() {
    return dassanaCrossAccountRoleName;
  }

  private String currentAccountId = null;

  public boolean crossAccountRoleRequired(String targetAccountId) {

    if (currentAccountId == null) {
      currentAccountId = stsClient.getCallerIdentity().account();
    }
    return !currentAccountId.contentEquals(targetAccountId);

  }

  public String getStackName() {
    return stackName;
  }

  public AwsRuntimeContext getAwsRuntimeContext(String targetAccountId, String resourceRegion) {
    String roleArn = "";
    try {
      if (StringUtils.isNotEmpty(dassanaCrossAccountRoleName)) {

        roleArn = "arn:aws:iam::".concat(targetAccountId).concat(":role/")
            .concat(dassanaCrossAccountRoleName);
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
