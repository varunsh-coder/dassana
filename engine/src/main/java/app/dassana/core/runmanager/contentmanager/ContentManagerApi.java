package app.dassana.core.runmanager.contentmanager;

import app.dassana.core.runmanager.launch.model.Request;
import app.dassana.core.workflowmanager.workflow.model.Workflow;
import java.util.Set;

public interface ContentManagerApi {

  Set<Workflow> getWorkflowSet(Request request) throws Exception;

}
