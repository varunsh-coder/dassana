package app.dassana.core.api;

import app.dassana.core.launch.model.PingResponse;
import com.vdurmont.semver4j.Semver;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.inject.Singleton;
import org.json.JSONObject;
import software.amazon.awssdk.utils.IoUtils;

@Singleton
public class PingHandler {

  URL url = new URL("https://api.github.com/repos/dassana-io/dassana/releases/latest");
  HttpURLConnection con;

  public PingHandler() throws IOException {
    con = (HttpURLConnection) url.openConnection();
    con.setRequestMethod("GET");
  }


  public PingResponse getPingResponse() throws IOException {
    String latestVersion = getLatestVersion();
    String currentVersion = System.getenv().get("version");
    PingResponse pingResponse = new PingResponse();
    pingResponse.setLatestVersion(latestVersion);
    pingResponse.setInstalledVersion(currentVersion);

    Semver installed = new Semver(latestVersion);
    Semver latest = new Semver(latestVersion);

    if (latest.isGreaterThan(installed)) {
      pingResponse.setUpgradeAvailable(true);
      pingResponse.setMessage("Hey there! An upgrade to Dassana is available. Please visit https://docs.dassana"
          + ".io/docs/guides/ops/updating-dassana for instructions");
    } else {
      pingResponse.setUpgradeAvailable(false);
    }
    return pingResponse;

  }


  String getLatestVersion() throws IOException {
    try (InputStream outputStream = con.getInputStream()) {
      String response = IoUtils.toUtf8String(outputStream);
      JSONObject jsonObject = new JSONObject(response);
      return jsonObject.getString("tag_name");

    }

  }

}
