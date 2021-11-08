package app.dassana.core.client.infra;

import static app.dassana.core.contentmanager.infra.S3WorkflowManager.LAST_UPDATED_KEY;
import static app.dassana.core.contentmanager.infra.S3WorkflowManager.WORKFLOW_PATH_IN_S3;

import io.micronaut.context.annotation.Value;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.apache.commons.io.IOUtils;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.model.ServerSideEncryption;
import software.amazon.awssdk.services.s3.paginators.ListObjectsV2Iterable;

/**
 * this class is a simple abstraction over s3 service, intended to be sub-classed for testing purposes
 */
@Singleton
public class S3Store {

  @Inject
  S3Client s3Client;

  @Value("${env.dassanaBucket}")
  String s3Bucket;

  public Long getLastUpdatedValueFromS3() {

    HeadObjectResponse headObjectResponse;
    try {
      headObjectResponse = s3Client.headObject(
          HeadObjectRequest.builder().bucket(s3Bucket).key(LAST_UPDATED_KEY).build());
      return headObjectResponse.lastModified().toEpochMilli();
    } catch (NoSuchKeyException noSuchKeyException) {
      return 0L;
    }

  }

  public List<String> getAllContent() {
    List<String> remoteContent = new LinkedList<>();

    Objects.requireNonNull(s3Bucket, "dassanaBucket must be specified as env var");
    //this is made available via CFT. Checkout "Variables" of DassanaEngine
    // and DassanaApi. In local dev setup, manually set it

    ListObjectsV2Request request = ListObjectsV2Request.builder().bucket(s3Bucket).prefix(WORKFLOW_PATH_IN_S3).build();
    ListObjectsV2Iterable response = s3Client.listObjectsV2Paginator(request);
    for (ListObjectsV2Response page : response) {
      for (S3Object object : page.contents()) {
        if (object.size() > 0) {
          try {
            s3Client
                .getObject(GetObjectRequest.builder().
                    bucket(s3Bucket).key(object.key())
                    .build(), (getObjectResponse, abortableInputStream) -> {//handle the object
                  String customWorkflowFile = IOUtils.toString(abortableInputStream, Charset.defaultCharset());
                  remoteContent.add(customWorkflowFile);
                  return null;
                });
          } catch (S3Exception exception) {//see https://github.com/aws/aws-sdk-java-v2/issues/428
            if (exception.toBuilder().statusCode() != 304) {
              throw new RuntimeException(exception);
            }
          }

        }
      }
    }//all s3 files have been processed now
    return remoteContent;
  }

  public void upload(String key, String body) {
    s3Client.putObject(
        PutObjectRequest.builder().bucket(s3Bucket).key(key).
            serverSideEncryption(ServerSideEncryption.AWS_KMS)
            .build(),
        RequestBody.fromString(body, Charset.defaultCharset()));
    s3Client.putObject(PutObjectRequest.builder().bucket(s3Bucket).key(LAST_UPDATED_KEY).build(),
        RequestBody.empty());

  }

  public void delete(String key) {
    s3Client.deleteObject(DeleteObjectRequest.builder().bucket(s3Bucket).key(key).build());
    s3Client.putObject(PutObjectRequest.builder().bucket(s3Bucket).key(LAST_UPDATED_KEY).build(),
        RequestBody.empty());

  }

}
