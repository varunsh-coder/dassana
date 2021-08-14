package app.dassana.core.api;

import app.dassana.core.launch.model.PingResponse;
import com.vdurmont.semver4j.Semver;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import javax.inject.Singleton;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

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


  private String getLatestVersion() throws IOException {

    JSONObject jsonObject = new JSONObject(getServerResponse(con.getInputStream()));
    return jsonObject.getString("tag_name");


  }

  private String getServerResponse(InputStream inputStream) throws IOException {
    try {
      return IOUtils.toString(inputStream, Charset.defaultCharset());
    } catch (IOException e) {
      if (e.getMessage().contains("stream is closed")) {
        con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        return IOUtils.toString(con.getInputStream(), Charset.defaultCharset());
      } else {
        throw new RuntimeException(e);
      }
    }

  }


}
