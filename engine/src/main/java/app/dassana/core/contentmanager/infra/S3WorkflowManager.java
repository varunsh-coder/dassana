package app.dassana.core.contentmanager.infra;

import static app.dassana.core.workflow.processor.Decorator.DASSANA_KEY;

import app.dassana.core.client.infra.S3Store;
import app.dassana.core.contentmanager.RemoteContentDownloadApi;
import app.dassana.core.workflow.model.WorkflowOutputWithRisk;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import io.micronaut.context.annotation.Value;
import io.micronaut.core.util.StringUtils;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import javax.inject.Singleton;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.json.JSONObject;

@Singleton
public class S3WorkflowManager implements RemoteContentDownloadApi {

  private final S3Store s3Store;
  public static final String WORKFLOW_PATH_IN_S3 = "workflows/";
  public static final String LAST_UPDATED_KEY = "content-last-updated";
  public static final String ALERT_ID_KEY = "alertId";
  public static final String VENDOR_ID_KEY = "vendorId";

  public S3WorkflowManager(S3Store s3Store) {
    this.s3Store = s3Store;
  }

  @Value("${env.dassanaBucket}")
  String s3Bucket;

  LoadingCache<String, Long> cache =
      CacheBuilder.newBuilder().expireAfterAccess(Duration.ofSeconds(30)).build(new CacheLoader<>() {
        @Override
        public Long load(String s) {
          return getLastUpdatedValueFromS3();
        }
      });

  protected String getPath(Optional<WorkflowOutputWithRisk> normalizationResult) {

    String vendorId = (String) normalizationResult.get().getOutput().get(VENDOR_ID_KEY);
    if (! StringUtils.isNotEmpty(vendorId)) {
      vendorId = "unknownVendor";
    }

    String alertsPrefix = "alerts".concat("/").concat(vendorId);

    String path;
    if (normalizationResult.isEmpty()) {
      path = alertsPrefix.concat("/unprocessed/").concat(getAlertId(null));
    } else {
      path = alertsPrefix.concat("/".concat(getAlertId(normalizationResult.get())));
    }
    return path;
  }

  // if alertId is available, use it, if not, use a random UUID
  private String getAlertId(WorkflowOutputWithRisk workflowOutputWithRisk) {
    if (workflowOutputWithRisk == null) {
      return UUID.randomUUID().toString();
    } else {
      String alertId = (String) workflowOutputWithRisk.getOutput().get(ALERT_ID_KEY);
      if (StringUtils.isNotEmpty(alertId)) {
        return alertId;
      } else {
        return UUID.randomUUID().toString();
      }
    }
  }

  protected String getUploadedPath(Optional<WorkflowOutputWithRisk> normalizationResult,
      String jsonToUpload, String path) {
    if (normalizationResult.isPresent()) {
      JSONObject dassanaDecoratedJsonObj = new JSONObject(jsonToUpload);
      JSONObject updatedDassanaObj = dassanaDecoratedJsonObj.getJSONObject(DASSANA_KEY)
          .put("alertKey", "s3://".concat(s3Bucket).concat("/").concat(path));
      dassanaDecoratedJsonObj.put(DASSANA_KEY, updatedDassanaObj);
      return dassanaDecoratedJsonObj.toString();
    } else {
      return jsonToUpload;
    }
  }

  public String uploadedToS3(Optional<WorkflowOutputWithRisk> normalizationResult,
      String jsonToUpload) {
    String path = getPath(normalizationResult);
    s3Store.upload(path, jsonToUpload);

    return getUploadedPath(normalizationResult, jsonToUpload, path);
  }

  @Override
  public List<String> downloadContent() {
    return s3Store.getAllContent();
  }

  @Override
  public Long getLastUpdated(boolean useCache) throws ExecutionException {

    if (useCache) {
      return cache.get("foo");
    } else {
      return getLastUpdatedValueFromS3();
    }

  }

  private Long getLastUpdatedValueFromS3() {
    return s3Store.getLastUpdatedValueFromS3();
  }

}
