package app.dassana.core.runmanager.client.infra;


import io.micronaut.context.annotation.Factory;
import javax.inject.Singleton;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.services.cloudformation.CloudFormationClient;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sts.StsClient;

/**
 * These clients are used by Dassana Engine for alert processing purpose and have no bearing whatsoever on the actions
 * called by the engine.
 * <p>
 * If you add a client here, make sure to update the MockClients.java also otherwise Micronaut test won't replace this
 * bean
 */
@Factory
public class Factories {

  @Singleton
  CloudFormationClient cloudFormationClient() {
    return CloudFormationClient.builder().httpClient(UrlConnectionHttpClient.builder().build()).build();
  }


  @Singleton
  StsClient stsClient() {
    return StsClient.builder().httpClient(UrlConnectionHttpClient.builder().build()).build();
  }


  @Singleton
  S3Client s3Client() {
    return S3Client.builder().httpClient(UrlConnectionHttpClient.builder().build()).build();
  }


  @Singleton
  SqsClient sqsClient() {
    return SqsClient.builder().httpClient(UrlConnectionHttpClient.builder().build()).build();
  }


  @Singleton
  LambdaClient lambdaClient() {
    return LambdaClient.builder().httpClient(UrlConnectionHttpClient.builder().build()).build();
  }

  @Singleton
  EventBridgeClient eventBridgeClient() {
    return EventBridgeClient.builder().httpClient(UrlConnectionHttpClient.builder().build()).build();
  }


}
