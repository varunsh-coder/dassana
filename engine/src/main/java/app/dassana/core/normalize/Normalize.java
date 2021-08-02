package app.dassana.core.normalize;

import app.dassana.core.contentmanager.ContentManagerApi;
import app.dassana.core.launch.model.RequestConfig;
import app.dassana.core.normalize.model.NormalizationResult;
import app.dassana.core.normalize.model.NormalizedWorkFlowOutputWithId;
import app.dassana.core.normalize.model.NormalizerWorkflow;
import app.dassana.core.rule.JsonQuery;
import app.dassana.core.rule.RuleMatch;
import app.dassana.core.workflow.WorkflowRunner;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class Normalize {

  private static final Logger logger = LoggerFactory.getLogger(Normalize.class);

  @Inject private RuleMatch ruleMatch;
  @Inject private WorkflowRunner workflowRunner;
  @Inject private ContentManagerApi<NormalizerWorkflow> contentManagerApi;
  @Inject private JsonQuery jsonQuery;

  public NormalizationResult runNormalizers(RequestConfig requestConfig) throws Exception {
    long start = System.currentTimeMillis();
    String receivedJsonStr = requestConfig.getInputJson();

    NormalizationResult normalizationResult = new NormalizationResult();
    normalizationResult.setNormalizerWorkflowUsed(new NormalizerWorkflow());
    normalizationResult.setNormalizerWorkflowUsed(new NormalizerWorkflow());

    Set<NormalizerWorkflow> workflowSet = contentManagerApi.getWorkflowSet(NormalizerWorkflow.class,requestConfig);

    for (NormalizerWorkflow normalizerWorkflow : workflowSet) {

      boolean ruleMatch;
      try {
        ruleMatch = this.ruleMatch.ruleMatch(normalizerWorkflow.getFilterRules(), receivedJsonStr,
            normalizerWorkflow.getMatchType());
      } catch (Exception e) {
        logger.warn("assuming {} to not match due to error {}", normalizerWorkflow.getId(), e.getMessage());
        ruleMatch = false;
      }
      if (ruleMatch) {
        normalizationResult.setNormalizerWorkflowUsed(normalizerWorkflow);
        logger.info("vendor {} workflow is going to normalize the input", normalizerWorkflow.getVendorName());
        List<Map<String, Object>> workFlowOutput = workflowRunner
            .runWorkFlow(normalizerWorkflow, receivedJsonStr, null).getStepOutput();
        logger.debug("Workflow output is {}", workFlowOutput);

        normalizationResult
            .setNormalizedWorkFlowOutputWithId(getNormalizedWorkFlowOutput(workFlowOutput, normalizerWorkflow));
        normalizationResult.setTimeTaken(System.currentTimeMillis() - start);
        return normalizationResult;
      }

    }//end running all normalizers

    normalizationResult.setTimeTaken(System.currentTimeMillis() - start);
    return normalizationResult;
  }

  public NormalizedWorkFlowOutputWithId getNormalizedWorkFlowOutput(List<Map<String, Object>> workFlowOutput,
      NormalizerWorkflow normalizerWorkflow) throws Exception {

    NormalizedWorkFlowOutputWithId normalizedWorkFlowOutput = new NormalizedWorkFlowOutputWithId();
    normalizedWorkFlowOutput.setWorkflowId(normalizerWorkflow.getId());

    String stepId = normalizerWorkflow.getStepId();

    JSONObject jsonObject = new JSONObject();

    for (Map<String, Object> map : workFlowOutput) {

      if (map.containsKey(stepId)) {
        Object o = map.get(stepId);
        jsonObject.put(stepId, o);
      }

    }

    String jsonData = jsonObject.toString();
    normalizedWorkFlowOutput
        .setAlertId(jsonQuery.query(normalizerWorkflow.getAlertIdJsonPath(), jsonData).asText());
    normalizedWorkFlowOutput.setCsp(jsonQuery.query(normalizerWorkflow.getCspJqPath(), jsonData).asText());
    normalizedWorkFlowOutput
        .setResourceContainer(jsonQuery.query(normalizerWorkflow.getResourceContainerJqPath(), jsonData).asText());
    normalizedWorkFlowOutput.setRegion(jsonQuery.query(normalizerWorkflow.getRegionJqPath(), jsonData).asText());
    normalizedWorkFlowOutput.setService(jsonQuery.query(normalizerWorkflow.getServiceJqPath(), jsonData).asText());
    normalizedWorkFlowOutput
        .setResourceType(jsonQuery.query(normalizerWorkflow.getResourceTypeJqPath(), jsonData).asText());
    normalizedWorkFlowOutput
        .setResourceId(jsonQuery.query(normalizerWorkflow.getResourceIdJqPath(), jsonData).asText());
    normalizedWorkFlowOutput
        .setCanonicalId(jsonQuery.query(normalizerWorkflow.getCanonicalIdJqPath(), jsonData).asText());
    normalizedWorkFlowOutput
        .setVendorPolicy(jsonQuery.query(normalizerWorkflow.getVendorPolicyJqPath(), jsonData).asText());

    return normalizedWorkFlowOutput;


  }


}
