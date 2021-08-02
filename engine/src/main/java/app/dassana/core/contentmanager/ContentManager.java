package app.dassana.core.contentmanager;

import app.dassana.core.contentmanager.model.SyncResult;
import app.dassana.core.contentmanager.model.WorkflowProcessingResult;
import app.dassana.core.contextualize.model.ContextWorkflow;
import app.dassana.core.launch.model.RequestConfig;
import app.dassana.core.normalize.model.NormalizerWorkflow;
import app.dassana.core.resource.model.ResourcePriorityWorkflow;
import app.dassana.core.risk.RiskConfig;
import app.dassana.core.risk.Rule;
import app.dassana.core.rule.MatchType;
import app.dassana.core.workflow.model.Step;
import app.dassana.core.workflow.model.Vendor;
import app.dassana.core.workflow.model.Workflow;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.micronaut.core.util.StringUtils;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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
  private final Set<NormalizerWorkflow> normalizerWorkflows = ConcurrentHashMap.newKeySet();
  private final Set<ContextWorkflow> contextWorkflows = ConcurrentHashMap.newKeySet();
  private final Set<ResourcePriorityWorkflow> resourcePriorities = ConcurrentHashMap.newKeySet();
  private long lastUpdated = 0;

  private static final Logger logger = LoggerFactory.getLogger(ContentManager.class);


  public ContentManager(RemoteContentDownloadApi contentDownloader) {
    this.contentDownloader = contentDownloader;
    syncRemoteContent(0L, null);//because we are in init/constructor, we fetch all workflows from s3
    String embeddedContentPath = Thread.currentThread().getContextClassLoader().getResource("workflows/").getFile();
    processDir(new File(embeddedContentPath));
  }


  RiskConfig getRiskConfig(JSONObject workFlowJson) {

    RiskConfig riskConfig = new RiskConfig();
    Set<Rule> riskRules = new HashSet<>();

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

  ResourcePriorityWorkflow getResourcePriorityWorkflow(JSONObject jsonObject) {
    ResourcePriorityWorkflow resourcePriorityWorkflow = new ResourcePriorityWorkflow();

    JSONObject filter = jsonObject.optJSONObject("filter");
    if (filter != null) {

      String filterString = filter.getString("match-type");

      if (filterString.contentEquals("all")) {
        resourcePriorityWorkflow.setMatchType(MatchType.ALL);
      } else if (filterString.contentEquals("any")) {
        resourcePriorityWorkflow.setMatchType(MatchType.ANY);
      }

      JSONArray rules = filter.optJSONArray("rules");
      List<String> rulesForEvent = new LinkedList<>();

      if (rules != null) {
        for (int j = 0; j < rules.length(); j++) {
          String rulesString = rules.getString(j);
          rulesForEvent.add(rulesString);
        }
        Collections.reverse(rulesForEvent);
        resourcePriorityWorkflow.setFilterRules(rulesForEvent);

      }
    } else {
      resourcePriorityWorkflow.setFilterRules(new LinkedList<>());
    }
    RiskConfig riskConfig = getRiskConfig(jsonObject);
    resourcePriorityWorkflow.setRiskConfig(riskConfig);
    return resourcePriorityWorkflow;


  }

  ContextWorkflow getContextWorkflow(JSONObject jsonObject) {
    var contextWorkflow = new ContextWorkflow();

    JSONObject filterJsonObj = jsonObject.getJSONObject("filter");
    JSONArray vendorsArray = filterJsonObj.getJSONArray("vendors");

    contextWorkflow.setCategory(jsonObject.optString("category"));
    contextWorkflow.setSubCategory(jsonObject.optString("subcategory"));

    List<Vendor> vendors = new LinkedList<>();

    for (int i = 0; i < vendorsArray.length(); i++) {
      JSONObject vendorObj = vendorsArray.getJSONObject(i);
      Vendor vendor = new Vendor();
      String name = vendorObj.getString("name");
      vendor.setName(name);
      String matchTypeStr = vendorObj.getString("match-type");
      if (matchTypeStr.contentEquals("any")) {
        vendor.setMatchType(MatchType.ANY);
      } else if (matchTypeStr.contentEquals("all")) {
        vendor.setMatchType(MatchType.ALL);
      } else {
        throw new IllegalArgumentException(
            String.format("Unexpected match type %s", matchTypeStr));
      }

      JSONArray rulesArray = vendorObj.getJSONArray("rules");
      List<String> rules = new LinkedList<>();
      for (int j = 0; j < rulesArray.length(); j++) {
        rules.add(rulesArray.getString(j));
      }
      Collections.reverse(rules);
      vendor.setRules(rules);
      vendors.add(vendor);
    }
    contextWorkflow.setVendors(vendors);

    RiskConfig riskConfig = getRiskConfig(jsonObject);
    contextWorkflow.setRiskConfig(riskConfig);
    return contextWorkflow;


  }

  NormalizerWorkflow getNormalizerWorkflow(JSONObject jsonObject) {

    var normalizerWorkflow = new NormalizerWorkflow();

    JSONObject normalizedOutput = jsonObject.getJSONObject("normalized-output");

    normalizerWorkflow.setStepId(normalizedOutput.getString("step-id"));

    JSONObject output = normalizedOutput.getJSONObject("output");
    normalizerWorkflow.setAlertIdJsonPath(output.getString("alertId"));
    normalizerWorkflow.setVendorPolicyJqPath(output.getString("vendorPolicy"));
    normalizerWorkflow.setCspJqPath(output.getString("csp"));
    normalizerWorkflow.setResourceContainerJqPath(output.getString("resourceContainer"));
    normalizerWorkflow.setRegionJqPath(output.getString("region"));
    normalizerWorkflow.setServiceJqPath(output.getString("service"));
    normalizerWorkflow.setResourceTypeJqPath(output.getString("resourceType"));
    normalizerWorkflow.setResourceIdJqPath(output.getString("resourceId"));
    normalizerWorkflow.setCanonicalIdJqPath(output.getString("canonicalId"));

    normalizerWorkflow.setVendorName(jsonObject.getString("vendor-name"));

    JSONObject outPutQueue = jsonObject.optJSONObject("output-queue");
    if (outPutQueue != null) {
      boolean outputQueueEnabled = outPutQueue.getBoolean("enabled");
      normalizerWorkflow.setOutputQueueEnabled(outputQueueEnabled);
    } else {
      normalizerWorkflow.setOutputQueueEnabled(false);
    }

    JSONObject filter = jsonObject.optJSONObject("filter");
    if (filter != null) {

      String filterString = filter.getString("match-type");

      if (filterString.contentEquals("all")) {
        normalizerWorkflow.setMatchType(MatchType.ALL);
      } else if (filterString.contentEquals("any")) {
        normalizerWorkflow.setMatchType(MatchType.ANY);
      }

      JSONArray rules = filter.optJSONArray("rules");
      List<String> rulesForEvent = new LinkedList<>();

      if (rules != null) {
        for (int j = 0; j < rules.length(); j++) {
          String rulesString = rules.getString(j);
          rulesForEvent.add(rulesString);
        }
        Collections.reverse(rulesForEvent);
        normalizerWorkflow.setFilterRules(rulesForEvent);

      }

      JSONObject postProcessor = jsonObject.optJSONObject("post-processor");

      List<Step> postProcessorSteps = new LinkedList<>();

      if (postProcessor != null) {
        JSONArray steps = postProcessor.optJSONArray("steps");

        if (steps != null) {
          for (int i = 0; i < steps.length(); i++) {
            JSONObject stepsJSONObject = steps.getJSONObject(i);
            Step step = new Step();
            step.setId(stepsJSONObject.getString("id"));
            step.setUses(stepsJSONObject.getString("uses"));
            postProcessorSteps.add(step);
          }
        }
      }
      normalizerWorkflow.setPostProcessorSteps(postProcessorSteps);
    }
    return normalizerWorkflow;
  }

  private void processWorkFlowFile(File file) throws IOException {
    logger.debug("Processing workflow file {}", file.getAbsolutePath());
    String content = new String(Files.readAllBytes(Paths.get(file.getPath())));

    ObjectMapper yamlReader = new ObjectMapper(new YAMLFactory());
    Object obj = yamlReader.readValue(content, Object.class);
    ObjectMapper jsonWriter = new ObjectMapper();
    String json = jsonWriter.writeValueAsString(obj);

    JSONObject jsonObject = new JSONObject(json);

    String type = jsonObject.optString("type");
    if (StringUtils.isNotEmpty(type)) {//if type is missing, it is not a workflow
      if (type.contentEquals("normalize")) {
        NormalizerWorkflow normalizerWorkflow = getNormalizerWorkflow(jsonObject);
        normalizerWorkflow.setOutput(getOutputFields(jsonObject));
        normalizerWorkflow.setId(jsonObject.getString("id"));
        normalizerWorkflow.setSteps(getSteps(jsonObject));
        normalizerWorkflows.remove(normalizerWorkflow);
        normalizerWorkflows.add(normalizerWorkflow);
      } else if (type.contentEquals("contextualize")) {
        ContextWorkflow contextWorkflow = getContextWorkflow(jsonObject);
        contextWorkflow.setOutput(getOutputFields(jsonObject));
        contextWorkflow.setId(jsonObject.getString("id"));
        contextWorkflow.setSteps(getSteps(jsonObject));
        contextWorkflows.remove(contextWorkflow);
        contextWorkflows.add(contextWorkflow);
      } else if (type.contentEquals("resource-priority")) {
        ResourcePriorityWorkflow resourcePriorityWorkflow = getResourcePriorityWorkflow(jsonObject);
        resourcePriorityWorkflow.setId(jsonObject.getString("id"));
        resourcePriorityWorkflow.setSteps(getSteps(jsonObject));
        resourcePriorityWorkflow.setOutput(getOutputFields(jsonObject));
        resourcePriorities.remove(resourcePriorityWorkflow);
        resourcePriorities.add(resourcePriorityWorkflow);

      } else {
        throw new IllegalArgumentException("Unrecognized workflow type :".concat(type));
      }

    }
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

  public SyncResult syncRemoteContent(Long lastSuccessfulSync, RequestConfig requestConfig) {

    SyncResult syncResult = new SyncResult();
    syncResult.setCacheHit(true);

    boolean stale = System.currentTimeMillis() - lastUpdated > TimeUnit.MINUTES.toMillis(SYNC_INTERVAL_IN_MINS);

    //sync every SYNC_INTERVAL_IN_MINS
    if ((requestConfig != null && requestConfig.isRefreshFromS3()) || stale) {
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
  public Set<? extends Workflow> getWorkflowSet(Class workflow, RequestConfig requestConfig) {
    syncRemoteContent(lastUpdated, requestConfig);
    if (workflow.getName().contentEquals(ResourcePriorityWorkflow.class.getName())) {
      return resourcePriorities;
    } else if (workflow.getName().contentEquals(NormalizerWorkflow.class.getName())) {
      return normalizerWorkflows;
    } else if (workflow.getName().contentEquals(ContextWorkflow.class.getName())) {
      return contextWorkflows;
    } else {
      throw new RuntimeException("Unsupported workflow type ".concat(workflow.getName()));
    }
  }


  private List<Map<String, String>> getOutputFields(JSONObject jsonObject) {
    List<Map<String, String>> outputFields = new LinkedList<>();

    var output = jsonObject.optJSONArray("output");
    if (output != null) {
      for (int i = 0; i < output.length(); i++) {

        JSONObject field = output.getJSONObject(i);
        String name = field.getString("name");
        String value = field.getString("value");
        Map<String, String> fieldMap = new HashMap<>();
        fieldMap.put("name", name);
        fieldMap.put("value", value);
        outputFields.add(fieldMap);

      }
      return outputFields;

    } else {
      return new LinkedList<>();
    }

  }
}
