package app.dassana.resourceid.sh;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import java.util.Map;
import org.json.JSONObject;

/**
 * Lambda function entry point. You can change to use other pojo type or implement a different RequestHandler.
 *
 * @see <a href=https://docs.aws.amazon.com/lambda/latest/dg/java-handler.html>Lambda Java Handler</a> for more
 * information
 */
public class App implements RequestHandler<Map<String, Object>, Object> {


  public App() {

  }

  @Override
  public NormalizationResult handleRequest(Map<String, Object> event, final Context context) {

    JSONObject inputJsonObj = new JSONObject(event);
    Normalize normalize = new Normalize(inputJsonObj.toString());
    return normalize.normalize();
  }
}
