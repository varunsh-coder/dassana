package app.dassana.core.contentmanager;

import app.dassana.core.contentmanager.model.SyncResult;
import app.dassana.core.contentmanager.model.WorkflowProcessingResult;
import app.dassana.core.launch.model.Request;
import app.dassana.core.normalize.model.NormalizerWorkflow;
import app.dassana.core.policycontext.model.PolicyContext;
import app.dassana.core.resource.model.GeneralContext;
import app.dassana.core.resource.model.ResourceContext;
import app.dassana.core.risk.model.RiskConfig;
import app.dassana.core.risk.model.Rule;
import app.dassana.core.rule.MatchType;
import app.dassana.core.workflow.model.Filter;
import app.dassana.core.workflow.model.Output;
import app.dassana.core.workflow.model.Step;
import app.dassana.core.workflow.model.ValueType;
import app.dassana.core.workflow.model.VendorFilter;
import app.dassana.core.workflow.model.Workflow;
import app.dassana.core.workflow.model.WorkflowSchemaVersion;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.micronaut.core.util.StringUtils;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import javax.inject.Singleton;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class ContentManager implements ContentManagerApi {

  private static final int SYNC_INTERVAL_IN_MINS = 10;

  private final RemoteContentDownloadApi contentDownloader;
  private final Set<Workflow> workflowSet = ConcurrentHashMap.newKeySet();
  private long lastUpdated = 0;

  public static final String POLICY_CONTEXT = "policy-context";
  public static final String POLICY_CONTEXT_CAT = "category";
  public static final String POLICY_CONTEXT_SUB_CAT = "subcategory";
  public static final String GENERAL_CONTEXT = "general-context";
  public static final String RESOURCE_CONTEXT = "resource-context";
  public static final String NORMALIZE = "normalize";

  private static final Logger logger = LoggerFactory.getLogger(ContentManager.class);


  public ContentManager(RemoteContentDownloadApi contentDownloader) {
    this.contentDownloader = contentDownloader;
    syncRemoteContent(0L, null);//because we are in init/constructor, we fetch all workflows from s3
    String embeddedContentPath = Thread.currentThread().getContextClassLoader().getResource("workflows/").getFile();
    processDir(new File(embeddedContentPath));
  }


  RiskConfig getRiskConfig(JSONObject workFlowJson) {

    RiskConfig riskConfig = new RiskConfig();
    List<Rule> riskRules = new LinkedList<>();

    JSONObject riskConfigObj = workFlowJson.optJSONObject("risk-config");

    if (riskConfigObj != null) {
      riskConfig.setDefaultRisk(riskConfigObj.getString("default-risk"));
      JSONArray rulesJsonArray = riskConfigObj.optJSONArray("rules");

      if (rulesJsonArray != null) {
        for (int i = 0; i < rulesJsonArray.length(); i++) {
          JSONObject ruleObj = rulesJsonArray.getJSONObject(i);
          String name = ruleObj.getString("name");
          String condition = ruleObj.getString("condition");
          String risk = ruleObj.getString("risk");

          Rule rule = new Rule(name, condition, risk);
          riskRules.add(rule);

        }
        riskConfig.setRiskRules(riskRules);
      }


    }
    return riskConfig;

  }


  PolicyContext getPolicyContext(JSONObject jsonObject) {

    PolicyContext policyContext = new PolicyContext();
    policyContext.setCategory(jsonObject.optString("category"));
    policyContext.setSubCategory(jsonObject.optString("subcategory"));

    return policyContext;


  }

  Workflow getWorkflow(JSONObject jsonObject) {
    Workflow workflow = new Workflow();

    String type = jsonObject.optString("type");

    if (StringUtils.isNotEmpty(type) && type.contentEquals(NORMALIZE)) {
      workflow = new NormalizerWorkflow();

      ((NormalizerWorkflow) workflow).setVendorName(jsonObject.getString("vendor-name"));

      JSONObject postProcessor = jsonObject.optJSONObject("post-processor");

      List<Step> postProcessorSteps = new LinkedList<>();

      if (postProcessor != null) {
        JSONArray postProcessorStepsJsonArray = postProcessor.optJSONArray("steps");

        if (postProcessorStepsJsonArray != null) {
          for (int i = 0; i < postProcessorStepsJsonArray.length(); i++) {
            JSONObject stepsJSONObject = postProcessorStepsJsonArray.getJSONObject(i);
            Step step = new Step();
            step.setId(stepsJSONObject.getString("id"));
            step.setUses(stepsJSONObject.getString("uses"));
            postProcessorSteps.add(step);
          }
        }
      }
      ((NormalizerWorkflow) workflow).setPostProcessorSteps(postProcessorSteps);
      var outputQueue = jsonObject.optJSONObject("output-queue");
      if (outputQueue != null) {
        boolean outputQueueEnabled = outputQueue.getBoolean("enabled");
        ((NormalizerWorkflow) workflow).setOutputQueueEnabled(outputQueueEnabled);
      } else {
        ((NormalizerWorkflow) workflow).setOutputQueueEnabled(false);
      }
    } else if (type.contentEquals(POLICY_CONTEXT)) {
      workflow = getPolicyContext(jsonObject);
      ((PolicyContext) workflow).setCategory(jsonObject.optString(POLICY_CONTEXT_CAT));
      ((PolicyContext) workflow).setSubCategory(jsonObject.optString(POLICY_CONTEXT_SUB_CAT));
      ((PolicyContext) workflow).setRiskConfig(getRiskConfig(jsonObject));

      PolicyContext policyContext = (PolicyContext) workflow;
      policyContext.setVendorFilters(getFilters(jsonObject));


    } else if (type.contentEquals(GENERAL_CONTEXT)) {
      workflow = new GeneralContext();
      ((GeneralContext) workflow).setCsp(jsonObject.getString("csp"));
      ((GeneralContext) workflow).setRiskConfig(getRiskConfig(jsonObject));
    } else if (type.contentEquals(RESOURCE_CONTEXT)) {
      workflow = new ResourceContext();
      ((ResourceContext) workflow).setService(jsonObject.getString("service"));
      ((ResourceContext) workflow).setResourceType(jsonObject.getString("resource-type"));
      ((ResourceContext) workflow).setRiskConfig(getRiskConfig(jsonObject));
    } else {
      throw new IllegalArgumentException("Sorry, we do not recognize the workflow type ".concat(type));
    }

    String schema = String.valueOf(jsonObject.getBigDecimal("schema"));
    if (schema.contentEquals("1.0")) {
      workflow.setSchema(WorkflowSchemaVersion.v1_0);
    } else {
      throw new IllegalArgumentException("Unrecognized schema version ".concat(schema));
    }
    workflow.setType(jsonObject.getString("type"));
    workflow.setId(jsonObject.getString("id"));
    workflow.setName(jsonObject.optString("name"));
    JSONArray labels = jsonObject.optJSONArray("labels");
    if (labels != null) {
      var labelsArray = new LinkedList<String>();
      for (int i = 0; i < labels.length(); i++) {
        labelsArray.add(labels.getString(i));
      }
      workflow.setLabels(labelsArray);
    }

    workflow.setOutput(getOutputs(jsonObject));
    workflow.setSteps(getSteps(jsonObject));

    if (!workflow.getType().equals(POLICY_CONTEXT)) {

      List<VendorFilter> filters = getFilters(jsonObject);
      List<Filter> filterList = new LinkedList<>();
      for (VendorFilter filter : filters) {
        Filter simpleFilter = new Filter();
        simpleFilter.setRules(filter.getRules());
        simpleFilter.setMatchType(filter.getMatchType());
        filterList.add(simpleFilter);
      }

      workflow.setFilters(filterList);

    }

    return workflow;


  }

  private List<Output> getOutputs(JSONObject jsonObject) {

    List<Output> outputs = new LinkedList<>();
    JSONArray optOutputJsonArray = jsonObject.optJSONArray("output");
    if (optOutputJsonArray != null) {
      for (int i = 0; i < optOutputJsonArray.length(); i++) {
        JSONObject outputObj = optOutputJsonArray.getJSONObject(i);
        Output output = new Output();
        output.setValue(outputObj.getString("value"));
        output.setName(outputObj.getString("name"));
        String valueType = outputObj.optString("valueType");
        if (StringUtils.isEmpty(valueType)) {
          output.setValueType(ValueType.JQ_EXPRESSION);
        } else {
          output.setValueType(ValueType.valueOf(valueType));
        }
        outputs.add(output);
      }

    }
    return outputs;

  }


  private List<VendorFilter> getFilters(JSONObject jsonObject) {
    List<VendorFilter> filters = new LinkedList<>();

    JSONArray optJSONArray = jsonObject.optJSONArray("filters");
    if (optJSONArray != null) {
      for (int i = 0; i < optJSONArray.length(); i++) {
        VendorFilter filter = new VendorFilter();
        JSONObject filterObj = optJSONArray.getJSONObject(i);
        String matchType = filterObj.getString("match-type");
        if (matchType.toLowerCase().contentEquals("any")) {
          filter.setMatchType(MatchType.ANY);
        } else if (matchType.toLowerCase().contentEquals("all")) {
          filter.setMatchType(MatchType.ALL);
        } else {
          throw new IllegalArgumentException("Unrecognized filter type ".concat(matchType));
        }
        JSONArray rulesJsonArray = filterObj.optJSONArray("rules");
        List<String> rules = new LinkedList<>();
        if (rulesJsonArray != null) {
          for (int j = 0; j < rulesJsonArray.length(); j++) {
            String rule = rulesJsonArray.optString(j);
            rules.add(rule);
          }
        }

        filter.setRules(rules);
        filters.add(filter);
      }

    }
    return filters;

  }

  private void processWorkFlowFile(File file) throws IOException {
    logger.debug("Processing workflow file {}", file.getAbsolutePath());
    String content = new String(Files.readAllBytes(Paths.get(file.getPath())));

    ObjectMapper yamlReader = new ObjectMapper(new YAMLFactory());
    Object obj = yamlReader.readValue(content, Object.class);
    ObjectMapper jsonWriter = new ObjectMapper();
    String json = jsonWriter.writeValueAsString(obj);

    JSONObject jsonObject = new JSONObject(json);

    Workflow workflow = getWorkflow(jsonObject);
    workflowSet.remove(workflow);
    workflowSet.add(workflow);

  }

  private List<Step> getSteps(JSONObject jsonObject) {

    JSONArray steps = jsonObject.optJSONArray("steps");

    List<Step> stepSet = new LinkedList<>();

    if (steps != null) {
      for (int i = 0; i < steps.length(); i++) {
        JSONObject stepsJSONObject = steps.getJSONObject(i);
        Step step = new Step();
        step.setId(stepsJSONObject.getString("id"));
        step.setUses(stepsJSONObject.getString("uses"));

        JSONArray withKey = stepsJSONObject.optJSONArray("with");

        if (withKey != null) {
          List<Map<String, String>> fields = new LinkedList<>();

          for (int j = 0; j < withKey.length(); j++) {

            JSONObject kvp = withKey.getJSONObject(j);

            Map<String, String> field = new HashMap<>();
            field.put("name", kvp.getString("name"));
            field.put("value", kvp.getString("value"));
            field.put("value-type", kvp.optString("value-type"));
            fields.add(field);
          }
          step.setFields(fields);

        } else {
          step.setFields(new LinkedList<>());
        }

        stepSet.add(step);

      }
    }
    return stepSet;

  }


  public WorkflowProcessingResult processDir(File dir) {
    WorkflowProcessingResult workflowProcessingResult = new WorkflowProcessingResult();
    Map<String, Exception> fileToExceptionMap = new HashMap<>();

    File[] files = dir.listFiles();
    if (files != null) {
      for (File file : files) {
        if (file.isDirectory()) {
          processDir(file);
        } else {
          try {
            processWorkFlowFile(file);
          } catch (Exception e) {
            fileToExceptionMap.put(file.getName(), e);
            logger.warn("{} will be skipped due to error ", file, e);
          }
        }
      }
    }

    workflowProcessingResult.setWorkflowFileToExceptionMap(fileToExceptionMap);
    return workflowProcessingResult;
  }

  public SyncResult syncRemoteContent(Long lastSuccessfulSync, Request request) {

    SyncResult syncResult = new SyncResult();
    syncResult.setCacheHit(true);

    boolean stale = System.currentTimeMillis() - lastUpdated > TimeUnit.MINUTES.toMillis(SYNC_INTERVAL_IN_MINS);

    //sync every SYNC_INTERVAL_IN_MINS
    if ((request != null && request.isRefreshFromS3()) || stale) {
      syncResult.setCacheHit(false);
      Optional<File> optionalFile;
      optionalFile = contentDownloader.downloadContent(lastSuccessfulSync);
      optionalFile.ifPresent(dir -> {
        WorkflowProcessingResult workflowProcessingResult = processDir(dir);
        syncResult.setSuccessful(workflowProcessingResult.getWorkflowFileToExceptionMap().size() <= 0);
      });
      lastUpdated = System.currentTimeMillis();
    }
    syncResult.setContentLastSyncTimeMs(lastUpdated);
    return syncResult;
  }

  @Override
  public Set<Workflow> getWorkflowSet(Request request) {
    syncRemoteContent(lastUpdated, request);
    return workflowSet;
  }

  @Override
  public Map<String, Workflow> getWorkflowIdToWorkflowMap(Request request) {

    Map<String, Workflow> map = new HashMap<>();
    getWorkflowSet(request).forEach(workflow -> map.put(workflow.getId(), workflow));
    return map;
  }

}
