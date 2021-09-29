package app.dassana.core.contentmanager;

import java.io.File;
import java.util.Map;
import java.util.Optional;

public interface WorkflowApi {

  Optional<File> downloadContent();
  void deleteContent(String workflowId);

}
