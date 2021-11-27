package app.dassana.core.runmanager.launch;

import app.dassana.core.runmanager.client.infra.Factories;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Replaces;
import javax.inject.Singleton;
import software.amazon.awssdk.services.cloudformation.CloudFormationClient;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sts.StsClient;

@Factory
@Replaces(factory = Factories.class)
public class MockClients {

  @Singleton
  CloudFormationClient cloudFormationClient() {
    return new CloudFormationClient() {
      @Override
      public String serviceName() {
        return null;
      }

      @Override
      public void close() {

      }
    };
  }


  @Singleton
  StsClient stsClient() {
    return new StsClient() {
      @Override
      public String serviceName() {
        return null;
      }

      @Override
      public void close() {

      }
    };
  }


  @Singleton
  S3Client s3Client() {
    return new S3Client() {
      @Override
      public String serviceName() {
        return null;
      }

      @Override
      public void close() {

      }
    };
  }


  @Singleton
  SqsClient sqsClient() {
    return new SqsClient() {
      @Override
      public String serviceName() {
        return null;
      }

      @Override
      public void close() {

      }
    };
  }


  @Singleton
  LambdaClient lambdaClient() {
    return new LambdaClient() {
      @Override
      public String serviceName() {
        return null;
      }

      @Override
      public void close() {

      }
    };
  }

  @Singleton
  EventBridgeClient eventBridgeClient() {
    return new EventBridgeClient() {
      @Override
      public String serviceName() {
        return null;
      }

      @Override
      public void close() {

      }
    };
  }

}
