package app.dassana.core.runmanager.contentmanager.validator;


import app.dassana.core.runmanager.launch.model.Message;
import app.dassana.core.runmanager.launch.model.Severity;
import app.dassana.core.runmanager.util.StringyThings;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Consumes;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import java.util.LinkedList;
import java.util.List;
import javax.inject.Inject;

@Controller("/workflows/validate")
public class Validate {

  @Inject private WorkflowValidator workflowValidator;


  @Post
  @Consumes(MediaType.APPLICATION_YAML)
  HttpResponse<List<Message>> validate(@Body String inputBody) {

    List<Message> messages = new LinkedList<>();
    try {
      workflowValidator.handleValidate(StringyThings.getJsonFromYaml(inputBody));
    } catch (DassanaWorkflowValidationException dassanaWorkflowValidationException) {
      messages.addAll(dassanaWorkflowValidationException.getMessages());
    } catch (Exception e) {
      messages.add(new Message(e.getMessage(), Severity.ERROR));
    }

    if (messages.size() > 0) {
      return HttpResponse.badRequest().body(messages);
    }

    return HttpResponse.ok();

  }


}
