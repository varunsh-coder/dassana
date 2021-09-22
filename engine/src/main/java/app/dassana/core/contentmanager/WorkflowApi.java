package app.dassana.core.contentmanager;

import java.io.File;
import java.util.Optional;

public interface WorkflowApi {

  Optional<File> downloadContent(Long lastDownloaded);
  Optional<String> isCustomWorkflow(String workflowId);
  void deleteContent(String workflowId);

}
