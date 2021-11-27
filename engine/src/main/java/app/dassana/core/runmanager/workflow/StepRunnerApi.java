package app.dassana.core.runmanager.workflow;

import app.dassana.core.workflowmanager.workflow.model.Step;
import app.dassana.core.workflowmanager.workflow.model.StepRunResponse;
import app.dassana.core.workflowmanager.workflow.model.Workflow;

public interface StepRunnerApi {

  StepRunResponse runStep(Workflow workflow,
      Step step,
      String inputJson,
      String simpleOutputJson) throws Exception;

}
