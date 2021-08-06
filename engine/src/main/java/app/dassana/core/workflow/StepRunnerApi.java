package app.dassana.core.workflow;

import app.dassana.core.workflow.model.Step;
import app.dassana.core.workflow.model.StepRunResponse;
import app.dassana.core.workflow.model.Workflow;
import java.util.Map;

public interface StepRunnerApi {

  StepRunResponse runStep(Workflow workflow,
      Step step,
      String inputJson,
      Map<String, Object> simpleOutput) throws Exception;

}
