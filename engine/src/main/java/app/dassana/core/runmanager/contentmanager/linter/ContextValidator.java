package app.dassana.core.runmanager.contentmanager.linter;

import app.dassana.core.runmanager.contentmanager.validator.ValidationException;
import app.dassana.core.runmanager.contentmanager.ContentManager;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class ContextValidator {

	public static final String missingFieldsStr = "Missing required fields: %s";
	private static final String[] normRequired = new String[]{ContentManager.VENDOR_ID};
	private static final String[] genRequired = new String[]{ContentManager.RESOURCE_CONTEXT_CSP};
	private static final String[] resRequired = new String[]{ContentManager.RESOURCE_CONTEXT_CSP,
					ContentManager.RESOURCE_CONTEXT_SERVICE, ContentManager.RESOURCE_CONTEXT_TYPE};
	private static final String[] polRequired = new String[]{ContentManager.RESOURCE_CONTEXT_CSP,
					ContentManager.RESOURCE_CONTEXT_SERVICE, ContentManager.RESOURCE_CONTEXT_TYPE,
					ContentManager.POLICY_CONTEXT_CLASS, ContentManager.POLICY_CONTEXT_SUBCLASS,
					ContentManager.POLICY_CONTEXT_CAT, ContentManager.POLICY_CONTEXT_FILTERS};

	private Gson gson = new Gson();

	private String getMissingFields(JsonObject json, String[] fields){
		List<String> missingFields = new ArrayList<>();
		for(String field : fields) {
			JsonElement element = json.get(field);
			if (element == null || element.isJsonNull()) { //validate field is required
				missingFields.add(field);
			}
		}

		JsonElement classField = json.get(ContentManager.FIELDS.CLASS.getName());
		String alertClass = classField != null ? classField.getAsString() : null;

		if(ContentManager.FIELDS.RISK.getName().equals(alertClass) && json.get("subcategory") == null){
			missingFields.add(ContentManager.FIELDS.SUB_CATEGORY.getName());
		}

		String errorMsg = missingFields.isEmpty() ? null : String.format(missingFieldsStr, missingFields);

		return errorMsg;
	}

	private String[] requiredArray(String context){
		String[] arr = ContentManager.POLICY_CONTEXT.equals(context) ? polRequired :
						ContentManager.RESOURCE_CONTEXT.equals(context) ? resRequired :
										ContentManager.GENERAL_CONTEXT.equals(context) ? genRequired :
														ContentManager.NORMALIZE.equals(context) ? normRequired : null;
		if(arr == null){
			throw new ValidationException("Invalid context: " + context);
		}
		return arr;
	}

	public String validate(String jsonStr, String context){
		JsonObject json = gson.fromJson(jsonStr, JsonObject.class);
		String[] reqArr = requiredArray(context);
		return getMissingFields(json, reqArr);
	}

}
