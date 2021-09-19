package app.dassana.core.contentmanager;

import java.io.File;
import java.util.Optional;

public interface WorkflowApi {

  Optional<File> downloadContent(Long lastDownloaded);
  void deleteContent(String workflowId);

}
