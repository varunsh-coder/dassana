package app.dassana.core.restapi;

import app.dassana.core.launch.model.Message;
import app.dassana.core.launch.model.Severity;
import com.google.gson.Gson;
import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.server.exceptions.ExceptionHandler;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.inject.Inject;
import javax.inject.Singleton;

@Requires(classes = {Exception.class, io.micronaut.http.server.exceptions.ExceptionHandler.class})
@Singleton
public class GlobalExceptionHandler implements ExceptionHandler<Exception, HttpResponse> {

  @Inject
  Gson gson;


  @Override
  public HttpResponse handle(HttpRequest request, Exception exception) {
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    exception.printStackTrace(pw);
    Message message = new Message(sw.toString(), Severity.ERROR);
    return HttpResponse.badRequest(gson.toJson(message));
  }
}
