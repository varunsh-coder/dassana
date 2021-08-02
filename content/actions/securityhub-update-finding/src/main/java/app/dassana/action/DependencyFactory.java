package app.dassana.action;

import software.amazon.awssdk.http.crt.AwsCrtAsyncHttpClient;
import software.amazon.awssdk.services.securityhub.SecurityHubAsyncClient;

/**
 * The module containing all dependencies required by the {@link App}.
 */
public class DependencyFactory {

  private DependencyFactory() {
  }

  /**
   * @return an instance of SecurityHubClient
   */
  public static SecurityHubAsyncClient securityHubClient() {
    return SecurityHubAsyncClient.builder().httpClientBuilder(AwsCrtAsyncHttpClient.builder()).build();

  }
}
