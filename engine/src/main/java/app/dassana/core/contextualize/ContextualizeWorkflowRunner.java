package app.dassana.core.contextualize;

import app.dassana.core.contentmanager.ContentManagerApi;
import app.dassana.core.contextualize.model.ContextWorkflow;
import app.dassana.core.launch.model.RequestConfig;
import app.dassana.core.normalize.model.NormalizationResult;
import app.dassana.core.normalize.model.NormalizedWorkFlowOutputWithId;
import app.dassana.core.risk.Risk;
import app.dassana.core.risk.RiskConfig;
import app.dassana.core.rule.RuleMatch;
import app.dassana.core.workflow.StepRunnerApi;
import app.dassana.core.workflow.WorkflowRunner;
import app.dassana.core.workflow.model.Step;
import app.dassana.core.workflow.model.Vendor;
import app.dassana.core.workflow.model.WorkflowOutputWithRisk;
import com.google.gson.Gson;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.lambda.LambdaClient;

@Singleton
public class ContextualizeWorkflowRunner {

  private static final Logger logger = LoggerFactory.getLogger(ContextualizeWorkflowRunner.class);

  @Inject private RuleMatch ruleMatch;
  @Inject private WorkflowRunner workflowRunner;
  @Inject private ContentManagerApi<ContextWorkflow> contentManagerApi;
  @Inject private LambdaClient lambdaClient;
  @Inject private StepRunnerApi stepRunnerApi;
  @Inject private Gson gson;


  public WorkflowOutputWithRisk getContext(NormalizationResult normalizationResult,
      RequestConfig requestConfig)
      throws Exception {

    long start = System.currentTimeMillis();

    if (normalizationResult.getNormalizerWorkflowUsed() != null) {

      NormalizedWorkFlowOutputWithId normalizedWorkFlowOutputWithId =
          normalizationResult.getNormalizedWorkFlowOutputWithId();

      WorkflowOutputWithRisk contextualizationResult = new WorkflowOutputWithRisk();
      Set<ContextWorkflow> workflowSet = contentManagerApi.getWorkflowSet(ContextWorkflow.class, requestConfig);
      List<Map<String, Object>> workflowOutput;
      for (ContextWorkflow contextWorkflow : workflowSet) {
        List<Vendor> vendors = contextWorkflow.getVendors();

        for (Vendor vendor : vendors) {
          List<String> vendorRules = vendor.getRules();
          boolean ruleMatch;
          try {
            ruleMatch = this.ruleMatch
                .ruleMatch(vendorRules, gson.toJson(normalizedWorkFlowOutputWithId), vendor.getMatchType());
          } catch (Exception e) {
            logger.warn("Assuming contextualization workflow {} to not match due to error {}",
                contextWorkflow.getId(), e.getMessage());
            ruleMatch = false;
          }
          if (ruleMatch) {
            workflowOutput = workflowRunner.runWorkFlow(contextWorkflow, gson.toJson(normalizedWorkFlowOutputWithId),
                normalizedWorkFlowOutputWithId).getStepOutput();
            contextualizationResult.setWorkflowOutput(workflowOutput);
            contextualizationResult.setWorkflowUsed(contextWorkflow);
            Risk contextualRisk = getContextualRisk(workflowOutput, contextWorkflow);
            contextualizationResult.setRisk(contextualRisk);
            contextualizationResult.setTimeTakenMs(System.currentTimeMillis() - start);
            return contextualizationResult;
          }
        }

      }
      contextualizationResult.setTimeTakenMs(System.currentTimeMillis() - start);
    }
    WorkflowOutputWithRisk workflowOutputWithRisk = new WorkflowOutputWithRisk();
    workflowOutputWithRisk.setWorkflowUsed(new ContextWorkflow());

    return workflowOutputWithRisk;


  }


  private String getJsonDataFromWorkFlowOutPut(List<Map<String, Object>> workFlowOutput) {

    JSONObject jsonObject = new JSONObject();

    for (Map<String, Object> map : workFlowOutput) {
      for (Entry<String, Object> entry : map.entrySet()) {
        jsonObject.put(entry.getKey(), entry.getValue());
      }

    }

    return jsonObject.toString();

  }


  private Risk getContextualRisk(List<Map<String, Object>> workFlowOutput,
      ContextWorkflow contextWorkflowUsed) throws Exception {

    RiskConfig riskConfig = contextWorkflowUsed.getRiskConfig();

    RiskCalcActionRequest riskCalcActionRequest = new RiskCalcActionRequest();
    riskCalcActionRequest.setJsonData(new JSONObject(getJsonDataFromWorkFlowOutPut(workFlowOutput)));
    riskCalcActionRequest.setRiskRules(riskConfig.getRiskRules());
    riskCalcActionRequest.setDefaultRisk(riskConfig.getDefaultRisk());

    JSONObject jsonObject = new JSONObject(riskCalcActionRequest);
    Step step = new Step();
    step.setUses("RiskCalcAction");
    step.setId("riskCalc");

    String responseFromRiskCalcAction = stepRunnerApi.runStep(contextWorkflowUsed, step, jsonObject.toString(), null)
        .getResponse();

    Risk risk = new Risk();

    JSONObject riskCalcActionResponseJsonObj = new JSONObject(responseFromRiskCalcAction);

    risk.setName(riskCalcActionResponseJsonObj.getString("name"));
    risk.setRiskValue(riskCalcActionResponseJsonObj.getString("risk"));
    risk.setCondition(riskCalcActionResponseJsonObj.optString("condition"));

    return risk;


  }


}
