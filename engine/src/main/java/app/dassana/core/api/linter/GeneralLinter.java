package app.dassana.core.api.linter;

import app.dassana.core.api.ValidationException;
import app.dassana.core.api.linter.pojo.Csp;
import app.dassana.core.api.linter.pojo.Provider;
import app.dassana.core.contentmanager.ContentManager;
import app.dassana.core.launch.model.Message;
import app.dassana.core.launch.model.Severity;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.gson.Gson;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class GeneralLinter extends BaseLinter{

	private Gson gson = new Gson();
	private Set<String> csps = new HashSet<>();
	protected CommonLinter commonLinter = new CommonLinter();

	@Override
	public void init() throws IOException {
		loadCsps();
		commonLinter.init();
	}

	@Override
	public List<Message> validate(String json) throws IOException {
		List<Message> messages = new ArrayList<>();
		String missingFields = validator.validate(json, ContentManager.GENERAL_CONTEXT);

		if(missingFields == null) {
			StatusMsg cspStatus = validateCsp(json);
			StatusMsg actionStatus = commonLinter.validateSteps(json);
			//StatusMsg requiredStatus = commonLinter.validateRequiredFields(json);

			if (cspStatus.isError()) {
				messages.add(new Message(cspStatus.getMsg(), Severity.WARN));
			}
			if (actionStatus.isError()) {
				messages.add(new Message(actionStatus.getMsg(), Severity.WARN));
			}
			/*if (requiredStatus.isError()) {
				messages.add(new Message(requiredStatus.getMsg(), Severity.WARN));
			}*/
		}else {
			messages.add(new Message(missingFields, Severity.WARN));
		}

		return messages;
	}

	@Override
	public void validate(Map<String, Object> data, String filename) throws IOException {
		validateCsp(data, filename);
		commonLinter.validateSteps(data, filename);
	}

	private void validateCsp(Map<String, Object> data, String filename){
		StatusMsg statusMsg = validateNormVendor(ContentManager.FIELDS.CSP, data, csps);
		if(statusMsg.isError()){
			throw new ValidationException(statusMsg.getMsg() + " in file: " + filename);
		}
	}

	private void loadCsps() throws IOException {
		ObjectMapper om = new ObjectMapper(new YAMLFactory());
		Csp csp = om.readValue(new File(content + "/schemas/resource-hierarchy/resource-hierarchy.yaml"), Csp.class);

		for(Provider provider : csp.getProviders()){
			String cspField = provider.getId();
			csps.add(cspField);
		}
	}

	private StatusMsg validateCsp(String json) {
		Map<String, Object> data = gson.fromJson(json, Map.class);
		StatusMsg statusMsg = validateNormVendor(ContentManager.FIELDS.CSP, data, csps);
		return statusMsg;
	}

}
