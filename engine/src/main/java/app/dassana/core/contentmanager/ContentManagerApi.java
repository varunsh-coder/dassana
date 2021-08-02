package app.dassana.core.contentmanager;

import app.dassana.core.contentmanager.model.SyncResult;
import app.dassana.core.launch.model.RequestConfig;
import app.dassana.core.workflow.model.Workflow;
import java.util.Set;

public interface ContentManagerApi<T extends Workflow> {

  /**
   * One time sync. Useful for debugging/api usage.
   *
   * @return SyncResult which might be helpful for debugging, monitoring etc
   */
  SyncResult syncRemoteContent(Long lastSuccessfulSync,RequestConfig requestConfig);

  /**
   * Returns a set of workflows for a given type. The expectation from implementation is that the content is
   * automatically refreshed
   *
   * @param t type of workflow e.g. Normalizer etc
   * @return set of workflows
   */
  Set<T> getWorkflowSet(Class<? extends Workflow> t, RequestConfig requestConfig);


}
