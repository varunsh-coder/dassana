package app.dassana.core.api.linter;

import app.dassana.core.api.ValidationException;
import app.dassana.core.contentmanager.ContentManager;
import app.dassana.core.launch.model.Message;
import app.dassana.core.launch.model.Severity;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

public class NormalizeLinter extends BaseLinter{

	public final static String vendorListYamlPath = "/schemas/vendors/vendor-list.yaml";
	private Gson gson = new Gson();
	private Set<String> vendors = new HashSet<>();
	private CommonLinter commonLinter = new CommonLinter();
	private GeneralLinter generalLinter = new GeneralLinter();

	@Override
	public void init() throws IOException {
		List<Map<String, String>> dataArr = yaml.load((new FileInputStream(content + vendorListYamlPath)));
		for(Map<String,String> data : dataArr){
			vendors.add(data.get("id"));
		}
		commonLinter.init();
	}

	@Override
	public List<Message> validate(String json) throws IOException {
		List<Message> messages = new ArrayList<>();
		String missingFields = validator.validate(json, ContentManager.NORMALIZE);

		if(missingFields == null) {
			StatusMsg vendorIdStatus = validateVendorId(json);
			StatusMsg actionStatus = commonLinter.validateSteps(json);
			StatusMsg requiredStatus = commonLinter.validateRequiredFields(json);

			if (vendorIdStatus.isError()) {
				messages.add(new Message(vendorIdStatus.getMsg(), Severity.WARN));
			}
			if (requiredStatus.isError()) {
				messages.add(new Message(requiredStatus.getMsg(), Severity.WARN));
			}
			if (actionStatus.isError()) {
				messages.add(new Message(actionStatus.getMsg(), Severity.WARN));
			}
		}else{
			messages.add(new Message(missingFields, Severity.WARN));
		}

		return messages;
	}

	@Override
	public void validate(Map<String, Object> data, String filename) throws IOException {
		commonLinter.validateRequiredFields(data, filename);
		validateVendorId(data, filename);
	}

	private StatusMsg validateVendorId(String json){
		Map<String, Object> data = gson.fromJson(json, Map.class);
		List<Map<String, Object>> outputs = (List<Map<String, Object>>) data.get(ContentManager.FIELDS.OUTPUT.getName());
		StatusMsg statusMsg = containsVendor(outputs);
		if(!statusMsg.isError()){
			statusMsg = validateNormVendor(data);
		}
		return statusMsg;
	}

	private StatusMsg validateNormVendor(Map<String, Object> data){
		String msg = null;
		String vendorId = (String) data.get(ContentManager.FIELDS.VENDOR_ID.getName());
		if(!vendors.contains(data.get(ContentManager.FIELDS.VENDOR_ID.getName()))){
			msg = "Invalid vendor-id field [" + vendorId + "], " + helpText(vendors);
		}
		boolean isError = msg == null ? false : true;
		return new StatusMsg(isError, msg);
	}

	private StatusMsg containsVendor(List<Map<String, Object>> outputs){
		StatusMsg statusMsg = new StatusMsg(false);
		if(outputs != null) {
			for (int i = 0; i < outputs.size() && !statusMsg.isError(); i++) {
				Map<String, Object> output = outputs.get(i);
				if ("vendorId".equals(output.get("name"))) {
					if (!vendors.contains((String) output.get("value"))) {
						statusMsg = new StatusMsg(true, "Invalid vendor id [" + output.get("value") + "]. " + helpText(vendors));
					}
				}
			}
		}

		return statusMsg;
	}

	private void validateVendorId(Map<String, Object> data, String filename){
		List<Map<String, Object>> outputs = (List<Map<String, Object>>) data.get(ContentManager.FIELDS.OUTPUT.getName());
		StatusMsg statusMsg = containsVendor(outputs);
		if(statusMsg.isError()){
			throw new ValidationException(statusMsg.getMsg() + " in file: " + filename);
		}
	}

	public void validateIcons() {
		List<File> files = loadFilesFromPath(content + "/schemas/vendors/icons", new String[]{"svg"});
		for (int i = 0; i < files.size(); i++) {
			File file = files.get(i);
			String name = file.getName().split(".svg")[0];
			if(!vendors.contains(name)){
				throw new ValidationException("Is missing image for file: " + file.getName() + ", " + helpText(vendors));
			}
		}
	}

}
