package app.dassana.core.launch;

import static app.dassana.core.contentmanager.ContentManager.GENERAL_CONTEXT;
import static app.dassana.core.contentmanager.ContentManager.NORMALIZE;
import static app.dassana.core.contentmanager.ContentManager.POLICY_CONTEXT;
import static app.dassana.core.contentmanager.ContentManager.RESOURCE_CONTEXT;

import app.dassana.core.api.PingHandler;
import app.dassana.core.contentmanager.ContentManager;
import app.dassana.core.launch.model.Message;
import app.dassana.core.launch.model.ProcessingResponse;
import app.dassana.core.launch.model.Request;
import app.dassana.core.launch.model.ValidationResult;
import app.dassana.core.launch.model.ValidationResult.ValidYaml;
import app.dassana.core.launch.model.ValidationResult.WorkFlowType;
import app.dassana.core.launch.model.WorkflowNotFundException;
import app.dassana.core.launch.model.severity;
import app.dassana.core.rule.RuleMatch;
import app.dassana.core.util.JsonyThings;
import app.dassana.core.util.StringyThings;
import app.dassana.core.workflow.RequestProcessor;
import app.dassana.core.workflow.model.Filter;
import app.dassana.core.workflow.model.Workflow;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.util.StringUtils;
import io.micronaut.function.aws.MicronautRequestHandler;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * todo: this class has become quite big, needs to be refactored
 * <p>
 * <p>
 * Note that there is only one api handler which is receiving ALL api requests instead of different api handlers
 * handling different api requests. This is by design and it might change in future. The reason is that when you create
 * a lambda function to receive a request from api gateway, a lot of resources and wiring needs to be done- you have a
 * cloudwatch log group, lambda resource policy etc to be setup. Given that we have large number of functions to begin
 * with and more functions (actions) will be available in future, we are choosing not to create a function per api but
 * rather handle api routing within this function.In future, we will have more APIs so even this approach isn't optimal
 * either. But for now, complexity in this class is manageable- there are only a few api routes- /run, /validate and
 * /ping
 */
@Introspected
public class ApiHandler extends
    MicronautRequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

  @Inject RequestProcessor requestProcessor;
  @Inject Gson gson;
  @Inject RuleMatch ruleMatch;
  @Inject PingHandler pingHandler;

  @Inject ContentManager contentManager; //todo: not a good idea to inject an implementation

  private static final Logger logger = LoggerFactory.getLogger(ApiHandler.class);

  public static final String API_PARAM_SKIP_GENERAL_CONTEXT = "skipGeneralContext";
  public static final String API_PARAM_SKIP_POLICY_CONTEXT = "skipPolicyContext";
  public static final String API_PARAM_SKIP_S3UPLOAD = "skipS3Upload";
  public static final String API_PARAM_SKIP_POST_PROCESSOR = "skipPostProcessor";
  public static final String API_PARAM_SKIP_REFRESH_FROM_S3 = "refreshWorkflowsFromS3";
  public static final String API_INCLUDE_INPUT_REQUEST = "includeInputRequest";

  public static final String MISSING_NORMALIZATION_MSG = "Dassana couldn't normalize the alert you sent, please verify "
      + "normalizers filter config";

  public ApiHandler() {
  }

  String handleGet(Request request, String workFlowId) throws Exception {

    for (Workflow workflow : contentManager.getWorkflowSet(request)) {
      if (workflow.getId().contentEquals(workFlowId)) {
        return contentManager.getWorkflowIdToYamlContext().get(workFlowId);
      }
    }
    throw new WorkflowNotFundException("That workflow id wasn't found :(");

  }

  String handleRun(Request request, Map<String, String> queryStringParameters) throws Exception {
    ProcessingResponse processingResponse = requestProcessor.processRequest(request);
    String decoratedJson = processingResponse.getDecoratedJson();
    return handleHints(decoratedJson, queryStringParameters);
  }


  ValidationResult handleValidate(Request request) {

    ValidationResult validationResult = new ValidationResult();
    validationResult.setInvalidRules(new LinkedList<>());

    String inputJson = request.getInputJson();
    JSONObject jsonObject = new JSONObject(inputJson);

    String workflow;
    try {
      workflow = StringyThings.getJsonFromYaml(jsonObject.getString("workflow"));
    } catch (Exception e) {
      ValidYaml validYaml = new ValidYaml();
      validYaml.setValidYaml(false);
      validYaml.setMessage(e.getMessage());
      validationResult.setValidYaml(validYaml);
      return validationResult;
    }

    Workflow workFlowFromContentManager;
    try {
      workFlowFromContentManager = contentManager.getWorkflow(new JSONObject(workflow));
    } catch (Exception e) {
      WorkFlowType workFlowType = new WorkFlowType();
      workFlowType.setValidType(false);
      workFlowType.setMessage(String.format("Valid workflow types are %s, %s and %s", GENERAL_CONTEXT, POLICY_CONTEXT,
          RESOURCE_CONTEXT));
      validationResult.setValidWorkflowType(workFlowType);
      return validationResult;
    }

    for (Filter filter : workFlowFromContentManager.getFilters()) {
      List<String> rules = filter.getRules();
      for (String s : rules) {
        if (!ruleMatch.isValidRule(s)) {
          validationResult.getInvalidRules().add(s);
        }
      }


    }

    WorkFlowType workFlowType = new WorkFlowType();
    workFlowType.setValidType(true);
    validationResult.setValidWorkflowType(workFlowType);

    ValidYaml validYaml = new ValidYaml();
    validYaml.setValidYaml(true);
    validationResult.setValidYaml(validYaml);
    return validationResult;

  }

  @Override
  public APIGatewayProxyResponseEvent execute(APIGatewayProxyRequestEvent input) {
    APIGatewayProxyResponseEvent gatewayProxyResponseEvent = new APIGatewayProxyResponseEvent();
    gatewayProxyResponseEvent.setHeaders(getHeaders());

    try {
      String inputJson;
      if (input.getIsBase64Encoded()) {
        inputJson = new String(Base64.getDecoder().decode(input.getBody()));
      } else {
        inputJson = input.getBody();
      }
      if (input.getHttpMethod().toLowerCase().contentEquals("post")) {
        JsonyThings.throwExceptionIfNotValidJsonObj(inputJson);
      }

      if (input.getPath().startsWith("/run") && input.getHttpMethod().toLowerCase().contentEquals("post")) {
        String handleRun = handleRun(getRequestFromQueryParam(input.getQueryStringParameters(), inputJson),
            input.getQueryStringParameters());
        gatewayProxyResponseEvent.setBody(handleRun);
      } else if (input.getPath().startsWith("/run") && input.getHttpMethod().toLowerCase().contentEquals("get")) {
        try {
          String response = handleGet(getRequestFromQueryParam(input.getQueryStringParameters(), inputJson),
              input.getQueryStringParameters().get("workflowId"));
          gatewayProxyResponseEvent.setBody(response);
          gatewayProxyResponseEvent.getHeaders().put("Content-type", "application/x-yaml");

        } catch (WorkflowNotFundException e) {
          gatewayProxyResponseEvent
              .setBody(String.format("Workflow %s not found", input.getQueryStringParameters().get("workflowId")));
          gatewayProxyResponseEvent.setStatusCode(404);
          return gatewayProxyResponseEvent;
        }
      } else if (input.getPath().startsWith("/ping")) {
        gatewayProxyResponseEvent.setBody(gson.toJson(pingHandler.getPingResponse()));

      } else if (input.getPath().startsWith("/validate")) {
        ValidationResult validationResult = handleValidate(
            getRequestFromQueryParam(input.getQueryStringParameters(), inputJson));
        gatewayProxyResponseEvent.setBody(gson.toJson(validationResult));
      } else {
        throw new RuntimeException("Sorry, I can't handle path ".concat(input.getPath()));
      }

      gatewayProxyResponseEvent.setStatusCode(200);
    } catch (Exception e) {
      StringWriter sw = new StringWriter();
      e.printStackTrace(new PrintWriter(sw));
      gatewayProxyResponseEvent.setBody(sw.toString());
      gatewayProxyResponseEvent.setStatusCode(500);
    }
    return gatewayProxyResponseEvent;
  }

  private Map<String, String> getHeaders() {
    Map<String, String> headers = new HashMap<>();
    headers.put("Access-Control-Allow-Headers", "x-api-key , Content-Type");
    headers.put("Access-Control-Allow-Origin", "*");
    headers.put("Access-Control-Allow-Methods", "OPTIONS,POST,GET");
    headers.put("content-type", "application/json");
    return headers;

  }

  private String handleHints(String json, Map<String, String> parameters) {

    JSONObject jsonObject = new JSONObject(json);
    JSONObject dassana = jsonObject.optJSONObject("dassana");

    if (dassana == null || !dassana.has(NORMALIZE)) {
      Message message = new Message();
      message.setSeverity(severity.WARN);
      message.setMsg(MISSING_NORMALIZATION_MSG);
      jsonObject.put("dassana", new JSONObject(gson.toJson(message)));
    } else {//this means that normalization did occur
      String[] workflows = {GENERAL_CONTEXT, RESOURCE_CONTEXT, POLICY_CONTEXT};

      for (String workflow : workflows) {
        JSONObject genContext = dassana.optJSONObject(workflow);
        if (genContext == null) {
          Message message = new Message();
          message.setSeverity(severity.WARN);
          message.setMsg(String.format("Sorry, but no %s workflow ran for the given alert. Please check filter config",
              workflow));
          jsonObject.getJSONObject("dassana").put(workflow, new JSONObject(gson.toJson(message)));
        }
      }


    }

    //we return the dassana only ONLY when we are asked to not include the original alert
    if (parameters != null && parameters.size() > 0
        && parameters.getOrDefault(API_INCLUDE_INPUT_REQUEST, "false").contentEquals("false")) {
      if (jsonObject.optJSONObject("dassana") != null) {
        return jsonObject.getJSONObject("dassana").toString();
      }
    }

    return jsonObject.toString();
  }

  /**
   * DO NOT put anything cloud implementation specific in this method i.e. do not have any aws/gcp/azure specific
   * constructs in this method as we intend to use this method as abstracted method which can be called by anyone who
   * speaks http layer. As you can see, there is no aws api gateway specific parameters.
   *
   * @param parameters   query params
   * @param inputJsonStr input to process
   * @return request obj which the engine will run
   */
  public Request getRequestFromQueryParam(Map<String, String> parameters, String inputJsonStr) {

    if (parameters == null) {
      parameters = new HashMap<>();
    }

    boolean skipGeneralContext = parameters
        .getOrDefault(API_PARAM_SKIP_GENERAL_CONTEXT, "false").contentEquals("true");

    boolean skipPolicyContext = parameters
        .getOrDefault(API_PARAM_SKIP_POLICY_CONTEXT, "false").contentEquals("true");

    boolean skipPostProcessor = parameters
        .getOrDefault(API_PARAM_SKIP_POST_PROCESSOR, "false").contentEquals("true");

    boolean skipS3Upload = parameters
        .getOrDefault(API_PARAM_SKIP_S3UPLOAD, "false").contentEquals("true");

    boolean refreshFromS3 = Boolean.parseBoolean(parameters.getOrDefault(API_PARAM_SKIP_REFRESH_FROM_S3, "false"));
    Request request = new Request(inputJsonStr);

    if (StringUtils.isNotEmpty(inputJsonStr)) {
      JSONObject inputJsonObj = new JSONObject(inputJsonStr);
      String workflow = inputJsonObj.optString("workflow");
      if (StringUtils.isNotEmpty(workflow)) {
        request.setAdditionalWorkflowYaml(workflow);
      }

    }

    request.setRefreshFromS3(refreshFromS3);

    request.setQueueProcessing(false);
    request.setSkipPostProcessor(skipPostProcessor);
    request.setSkipGeneralContext(skipGeneralContext);
    request.setSkipPolicyContext(skipPolicyContext);
    request.setSkipS3Upload(skipS3Upload);
    return request;
  }


}
