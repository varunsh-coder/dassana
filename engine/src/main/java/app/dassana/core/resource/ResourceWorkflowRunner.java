package app.dassana.core.resource;

import app.dassana.core.contentmanager.ContentManagerApi;
import app.dassana.core.contextualize.RiskCalcActionRequest;
import app.dassana.core.launch.model.RequestConfig;
import app.dassana.core.normalize.model.NormalizationResult;
import app.dassana.core.normalize.model.NormalizedWorkFlowOutput;
import app.dassana.core.normalize.model.NormalizedWorkFlowOutputWithId;
import app.dassana.core.resource.model.ResourcePriorityWorkflow;
import app.dassana.core.risk.Risk;
import app.dassana.core.risk.RiskConfig;
import app.dassana.core.rule.RuleMatch;
import app.dassana.core.workflow.StepRunnerApi;
import app.dassana.core.workflow.WorkflowRunner;
import app.dassana.core.workflow.model.Step;
import app.dassana.core.workflow.model.WorkflowOutput;
import app.dassana.core.workflow.model.WorkflowOutputWithRisk;
import com.google.gson.Gson;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.lambda.LambdaClient;

@Singleton
public class ResourceWorkflowRunner {

  private static final Logger logger = LoggerFactory.getLogger(ResourceWorkflowRunner.class);

  @Inject private RuleMatch ruleMatch;
  @Inject private WorkflowRunner workflowRunner;
  @Inject private StepRunnerApi stepRunnerApi;
  @Inject private ContentManagerApi<ResourcePriorityWorkflow> contentManagerApi;
  @Inject private LambdaClient lambdaClient;
  @Inject private Gson gson;


  public WorkflowOutputWithRisk getResourceRisk(NormalizationResult normalizationResult, RequestConfig requestConfig) throws Exception {

    long start = System.currentTimeMillis();
    WorkflowOutputWithRisk resourcePriorityResult = new WorkflowOutputWithRisk();

    if (normalizationResult != null) {
      NormalizedWorkFlowOutputWithId normalizedWorkFlowOutputWithId =
          normalizationResult.getNormalizedWorkFlowOutputWithId();

      String normalizedJson = gson.toJson(normalizedWorkFlowOutputWithId);

      Set<ResourcePriorityWorkflow> workflowSet = contentManagerApi.getWorkflowSet(ResourcePriorityWorkflow.class,requestConfig);

      for (ResourcePriorityWorkflow resourcePriorityWorkflow : workflowSet) {

        if (resourcePriorityWorkflow.getFilterRules().size() > 0) {
          boolean ruleMatch;

          try {
            ruleMatch = this.ruleMatch
                .ruleMatch(resourcePriorityWorkflow.getFilterRules(), normalizedJson,
                    resourcePriorityWorkflow.getMatchType());
          } catch (Exception e) {
            logger.warn("Assuming resource prioritization workflow {} to not match due to error {}",
                resourcePriorityWorkflow.getId(), e.getMessage());
            ruleMatch = false;
          }

          if (ruleMatch) {
            resourcePriorityResult.setRisk(
                getEvaluatedRisk(resourcePriorityWorkflow, normalizedJson, normalizedWorkFlowOutputWithId).getRisk());
            resourcePriorityResult.setWorkflowUsed(resourcePriorityWorkflow);
            resourcePriorityResult.setTimeTakenMs(System.currentTimeMillis() - start);
            return resourcePriorityResult;
          }
        }
      }
    }
    resourcePriorityResult.setTimeTakenMs(System.currentTimeMillis() - start);
    return new WorkflowOutputWithRisk();

  }

  private WorkflowOutputWithRisk getEvaluatedRisk(ResourcePriorityWorkflow resourcePriorityWorkflow,
      String normalizedJson,
      NormalizedWorkFlowOutput normalizedWorkFlowOutput) throws Exception {

    WorkflowOutput workflowOutput = workflowRunner
        .runWorkFlow(resourcePriorityWorkflow, normalizedJson, normalizedWorkFlowOutput);

    JSONObject requestJsonObjForRiskCalc = new JSONObject();

    for (Map<String, Object> map : workflowOutput.getStepOutput()) {
      for (String key : map.keySet()) {
        requestJsonObjForRiskCalc.put(key, map.get(key));
      }
    }

    //the normalized fields should be available for risk rules in addition to the fields returned by steps
    Map<String, Object> normalizedJsonMap = new JSONObject(normalizedJson).toMap();
    normalizedJsonMap.forEach((s, o) -> requestJsonObjForRiskCalc.put(s, o));

    RiskConfig riskConfig = resourcePriorityWorkflow.getRiskConfig();

    RiskCalcActionRequest riskCalcActionRequest = new RiskCalcActionRequest();
    riskCalcActionRequest.setJsonData(requestJsonObjForRiskCalc);
    riskCalcActionRequest.setRiskRules(riskConfig.getRiskRules());
    riskCalcActionRequest.setDefaultRisk(riskConfig.getDefaultRisk());

    JSONObject jsonObject = new JSONObject(riskCalcActionRequest);
    Step step = new Step();
    step.setUses("RiskCalcAction");
    step.setId("riskCalc");

    String responseFromRiskCalcAction = stepRunnerApi
        .runStep(resourcePriorityWorkflow, step, jsonObject.toString(), null)
        .getResponse();

    Risk risk = new Risk();
    JSONObject riskCalcActionResponseJsonObj = new JSONObject(responseFromRiskCalcAction);

    risk.setName(riskCalcActionResponseJsonObj.getString("name"));
    risk.setRiskValue(riskCalcActionResponseJsonObj.getString("risk"));
    risk.setCondition(riskCalcActionResponseJsonObj.optString("condition"));

    WorkflowOutputWithRisk workflowOutputWithRisk = new WorkflowOutputWithRisk();
    workflowOutputWithRisk.setRisk(risk);
    workflowOutputWithRisk.setStepOutput(workflowOutput.getStepOutput());
    workflowOutputWithRisk.setWorkflowOutput(workflowOutput.getWorkflowOutput());

    return workflowOutputWithRisk;


  }

}
