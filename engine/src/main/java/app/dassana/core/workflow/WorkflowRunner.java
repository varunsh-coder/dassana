package app.dassana.core.workflow;

import app.dassana.core.contentmanager.ContentManagerApi;
import app.dassana.core.normalize.model.NormalizedWorkFlowOutput;
import app.dassana.core.workflow.model.Step;
import app.dassana.core.workflow.model.Workflow;
import app.dassana.core.workflow.model.WorkflowOutput;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import io.micronaut.core.util.StringUtils;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.thisptr.jackson.jq.BuiltinFunctionLoader;
import net.thisptr.jackson.jq.JsonQuery;
import net.thisptr.jackson.jq.Scope;
import net.thisptr.jackson.jq.Version;
import net.thisptr.jackson.jq.Versions;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class WorkflowRunner {

  private static final Logger logger = LoggerFactory.getLogger(WorkflowRunner.class);

  @Inject ContentManagerApi contentManager;
  @Inject StepRunnerApi stepRunnerApi;

  Gson gson = new Gson();

  Scope rootScope = Scope.newEmptyScope();
  private static final ObjectMapper MAPPER = new ObjectMapper();

  public WorkflowRunner() {
    BuiltinFunctionLoader.getInstance().loadFunctions(Versions.JQ_1_6, rootScope);
  }

  public WorkflowOutput runWorkFlow(Workflow workflow, String inputJson,
      NormalizedWorkFlowOutput normalizedWorkFlowOutput) throws Exception {

    WorkflowOutput workflowOutput = new WorkflowOutput();

    List<Step> workflowSteps = workflow.getSteps();

    Map<String, Object> stepToOutPutMap = new HashMap<>();
    for (Step step : workflowSteps) {
      String stepPayload;
      if (!step.getFields().isEmpty()) {//the step uses "with" keyword
        stepPayload = getPayloadForStep(step.getFields(), stepToOutPutMap, inputJson);
      } else {
        stepPayload = inputJson;
      }

      String stepOutputJson;
      stepOutputJson = runStep(workflow, step, stepPayload, normalizedWorkFlowOutput);

      Object fromJson = gson.fromJson(stepOutputJson, Object.class);
      logger.info("Output from step {} is {}", step.getId(), stepOutputJson);
      stepToOutPutMap.put(step.getId(), fromJson);

    }//end running of all steps

    //merge the step results into the event
    JSONObject jsonObject = new JSONObject(inputJson);

    for (Entry<String, Object> mapEntry : stepToOutPutMap.entrySet()) {
      String stepId = mapEntry.getKey();
      Object stepOutput = mapEntry.getValue();
      jsonObject.put(stepId, stepOutput);
    }

    List<Map<String, Object>> list = new LinkedList<>();

    for (Entry<String, Object> entry : stepToOutPutMap.entrySet()) {
      String stepId = entry.getKey();
      Object o = entry.getValue();

      Map<String, Object> map = new HashMap<>();
      map.put(stepId, o);
      list.add(map);

    }
    workflowOutput.setStepOutput(list);

    List<Map<String, Object>> workflowOutPut = new LinkedList<>();
    List<Map<String, String>> outputFields = workflow.getOutput();

    for (Map<String, String> map : outputFields) {

      String fieldName = map.get("name");
      String value = map.get("value");
      String valueDataType = map.getOrDefault("value-type", "string");
      JsonQuery jsonQuery = JsonQuery.compile(value, Version.LATEST);
      JsonNode in = MAPPER.readTree(jsonObject.toString());

      Scope childScope = Scope.newChildScope(rootScope);

      jsonQuery.apply(childScope, in, jsonNode -> {
        Map<String, Object> field = new HashMap<>();
        if (valueDataType.contentEquals("string")) {
          field.put(fieldName, jsonNode.asText());
        } else if (valueDataType.contentEquals("long")) {
          field.put(fieldName, jsonNode.asLong());
        } else if (valueDataType.contentEquals("boolean")) {
          field.put(fieldName, jsonNode.asBoolean());
        } else if (valueDataType.contentEquals("int")) {
          field.put(fieldName, jsonNode.asInt());
        } else if (valueDataType.contentEquals("object")) {
          field.put(fieldName, jsonNode.toString());
        } else {
          throw new UnsupportedOperationException("Unsupported value type".concat(valueDataType));
        }
        workflowOutPut.add(field);

      });

    }
    workflowOutput.setWorkflowOutput(workflowOutPut);

    return workflowOutput;
  }


  private String getPayloadForStep(List<Map<String, String>> fields,
      Map<String, Object> stepToOutPutMap,
      String inputJson) throws JsonProcessingException {

    JSONObject eventPayloadJsonObject = new JSONObject(inputJson);

    Map<String, Object> eventPayloadAsMap = eventPayloadJsonObject.toMap();
    eventPayloadAsMap.put("steps", stepToOutPutMap);

    JSONObject mergedJsonObj = new JSONObject(eventPayloadAsMap);

    JSONObject fieldJsonObj = new JSONObject();

    for (Map<String, String> field : fields) {

      String name = field.get("name");
      String value = field.get("value");
      String valueType = field.get("value-type");

      if (StringUtils.isEmpty(valueType) || valueType.contentEquals("jq")) { //assume the value type to be a
        // JQ value type

        JsonQuery jsonQuery = JsonQuery.compile(value, Version.LATEST);
        JsonNode in = MAPPER.readTree(mergedJsonObj.toString());

        Scope childScope = Scope.newChildScope(rootScope);
        jsonQuery.apply(childScope, in, out -> fieldJsonObj.put(name, out.asText()));


      } else if (valueType.contentEquals("string")) {
        fieldJsonObj.put(name, value);
      }

    }

    return fieldJsonObj.toString();

  }


  private String runStep(Workflow workflow, Step step, String payLoad,
      NormalizedWorkFlowOutput normalizedWorkFlowOutput) throws Exception {
    return stepRunnerApi.runStep(workflow, step, payLoad, normalizedWorkFlowOutput).getResponse();

  }


}