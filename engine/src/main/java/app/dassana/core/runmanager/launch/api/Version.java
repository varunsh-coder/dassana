package app.dassana.core.runmanager.launch.api;

import app.dassana.core.runmanager.launch.handler.VersionHandler;
import app.dassana.core.runmanager.launch.handler.VersionResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import java.io.IOException;
import javax.inject.Inject;

@Controller("/version")
public class Version {

  @Inject private VersionHandler versionHandler;

  @Get
  VersionResponse getVersion() throws IOException {
    return versionHandler.getVersionResponse();
  }


}
