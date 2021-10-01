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

    // default to raw alert. will change if there is detail and findings tags
    JSONObject finding = inputJson;

    // only the eventBridge alert has detail and findings∂ tags
    if (finding.has("detail")) {
      finding = inputJson.getJSONObject("detail")
          .getJSONArray("findings").getJSONObject(0);
    }

    // type will determine which resource to process
    // what if there is more than one resource in the type array??
    String policyId = finding.getJSONArray("Types").getString(0);

    Normalize normalize = new Normalize(finding, policyId);
    // normalize will handle all 3 different resource type [IAM, EC2, S3]
    Response response = normalize.normalize();

    return response;
  }
}
