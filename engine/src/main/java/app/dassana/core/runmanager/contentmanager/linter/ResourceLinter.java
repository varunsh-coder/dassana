package app.dassana.core.runmanager.contentmanager.linter;

import app.dassana.core.runmanager.contentmanager.validator.ValidationException;
import app.dassana.core.runmanager.contentmanager.linter.pojo.Csp;
import app.dassana.core.runmanager.contentmanager.linter.pojo.Field;
import app.dassana.core.runmanager.contentmanager.linter.pojo.Provider;
import app.dassana.core.runmanager.contentmanager.linter.pojo.Service;
import app.dassana.core.runmanager.contentmanager.ContentManager;
import app.dassana.core.runmanager.launch.model.Message;
import app.dassana.core.runmanager.launch.model.Severity;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.gson.Gson;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class ResourceLinter extends GeneralLinter{

	public final static String hierarchyYamlPath = "/schemas/resource-hierarchy/resource-hierarchy.yaml";
	private Gson gson = new Gson();
	private Map<String, Set<String>> cspToService = new HashMap<>();
	private Map<String, Set<String>> serviceToResource = new HashMap<>();

	@Override
	public List<Message> validate(String json) throws IOException {
		List<Message> messages = new ArrayList<>();
		String missingFields = validator.validate(json, ContentManager.RESOURCE_CONTEXT);

		if(missingFields == null) {
			StatusMsg stepStatus = commonLinter.validateSteps(json);
			if (stepStatus.isError()) {
				messages.add(new Message(stepStatus.getMsg(), Severity.WARN));
			}

			StatusMsg resourceStatus = validateResourceHierarchy(json);

			if (resourceStatus.isError()) {
				messages.add(new Message(resourceStatus.getMsg(), Severity.WARN));
			}
		}else{
			messages.add(new Message(missingFields, Severity.WARN));
		}

		return messages;
	}

	@Override
	public void validate(Map<String, Object> data, String filename) throws IOException {
		super.validate(data, filename);
		validateResourceHierarchy(data, filename);
	}

	@Override
	public void init() throws IOException {
		super.init();
		ObjectMapper om = new ObjectMapper(new YAMLFactory());
		Csp csp = om.readValue(new File(content + hierarchyYamlPath), Csp.class);

		for(Provider provider : csp.getProviders()){
			addResourcesToMaps(provider);
		}
	}

	private void validateResourceHierarchy(Map<String, Object> data, String filename) {
		StatusMsg statusMsg = isValidPolicy(data);
		if(statusMsg.isError()){
			throw new ValidationException(statusMsg.getMsg() + " in file: " + filename);
		}
	}

	private StatusMsg validateResourceHierarchy(String json){
		Map<String, Object> data = gson.fromJson(json, Map.class);
		StatusMsg statusMsg = isValidPolicy(data);
		return statusMsg;
	}

	private StatusMsg isValidPolicy(Map<String, Object> map){
		StatusMsg statusMsg = new StatusMsg(false);

		String csp = (String) map.get(ContentManager.FIELDS.CSP.getName()),
						service = (String) map.get(ContentManager.FIELDS.SERVICE.getName()),
						resource = (String) map.get(ContentManager.FIELDS.RESOURCE_TYPE.getName());

		boolean isValid = cspToService.containsKey(csp) && cspToService.get(csp).contains(service)
						&& serviceToResource.get(service).contains(resource);

		if(!isValid){
			statusMsg = setErrorMessages(csp, service, resource);
		}

		return statusMsg;
	}

	private StatusMsg setErrorMessages(String csp, String service, String resource){
		String errField = !cspToService.containsKey(csp) ? "invalid " + ContentManager.FIELDS.CSP.getName() + ": [" + csp + "], "
						+  helpText(cspToService, true): !cspToService.get(csp).contains(service) ?
						"invalid "+ ContentManager.FIELDS.SERVICE.getName() + ": [" + service + "], " + helpText(cspToService, csp, true):
										"invalid "+ContentManager.FIELDS.RESOURCE_TYPE.getName() +": [" + resource + "], " + helpText(serviceToResource, service, true);
		return new StatusMsg(true, errField);
	}

	private void addResourcesToMaps(Provider provider){
		cspToService.put(provider.getId(), new HashSet<>());
		for(Service service : provider.getServices()){
			cspToService.get(provider.getId()).add(service.getId());
			serviceToResource.put(service.getId(), new HashSet<>());
			List<Field> resources = service.getResources();
			if(resources != null) {
				for (Field resource : resources) {
					serviceToResource.get(service.getId()).add(resource.getId());
				}
			}
		}
	}

}
