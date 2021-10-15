package app.dassana.core.restapi;

import static app.dassana.core.contentmanager.infra.S3WorkflowManager.WORKFLOW_PATH_IN_S3;

import app.dassana.core.client.infra.S3Store;
import app.dassana.core.contentmanager.ContentManager;
import app.dassana.core.contentmanager.Parser;
import app.dassana.core.launch.model.Request;
import app.dassana.core.util.StringyThings;
import app.dassana.core.workflow.model.Workflow;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.micronaut.context.annotation.Value;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.QueryValue;
import java.util.Set;
import javax.inject.Inject;
import org.json.JSONObject;
import software.amazon.awssdk.services.s3.S3Client;

@Controller("/workflows")
public class Workflows {

  @Inject private S3Client s3Client;

  @Inject private ContentManager contentManager;
  @Inject private Parser parser;
  @Inject private S3Store s3Store;

  @Value("${env.dassanaBucket}")
  String dassanaBucket;


  @Delete
  void handleDelete(@QueryValue("workflowId") String workFlowId) {

    String key = WORKFLOW_PATH_IN_S3.concat(workFlowId);
    s3Store.delete(key);
  }


  @Get(produces = MediaType.APPLICATION_YAML)
  HttpResponse<String> getWorkflow(@QueryValue("workflowId") String workFlowId,
      @QueryValue("default") @Nullable Boolean getDefault) throws Exception {

    if (getDefault == null) {
      getDefault = Boolean.FALSE;
    }

    if (getDefault) {//user wants to get the default dassana workflow, so we only look for workflows in the default
      // content set
      Set<Workflow> defaultWorkflowSet = contentManager.getDefaultWorkflowSet();

      for (Workflow workflow : defaultWorkflowSet) {
        if (workflow.getId().contentEquals(workFlowId)) {
          return HttpResponse.ok().body(workflow.getWorkflowFileContent()).
              header("x-workflow-is-default", "true");
        }
      }

    } else {
      Request request = new Request(null);
      request.setUseCache(false);

      Set<Workflow> workflowSet = contentManager.getWorkflowSet(new Request(null));
      for (Workflow workflow : workflowSet) {
        if (workflow.getId().contentEquals(workFlowId)) {
          String isDefault = "true";
          if (!workflow.isDefault()) {
            isDefault = "false";
          }
          return HttpResponse.ok().body(workflow.getWorkflowFileContent()).header("x-workflow-is-default", isDefault);
        }
      }
    }
    return HttpResponse.notFound();

  }


  @Post(consumes = MediaType.APPLICATION_YAML)
  void handleSaveToS3(@Body String body) throws JsonProcessingException {
    Workflow workflow = parser.getWorkflow(new JSONObject(StringyThings.getJsonFromYaml(body)));
    String key = WORKFLOW_PATH_IN_S3.concat(workflow.getId());
    s3Store.upload(key, body);

  }

}
