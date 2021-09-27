package app.dassana.core.contentmanager;

import java.io.File;
import java.util.Map;
import java.util.Optional;

public interface WorkflowApi {

  Optional<File> downloadContent();
  Optional<String> isCustomWorkflow(String workflowId);
  void deleteContent(String workflowId);

}
