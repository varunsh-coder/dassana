package app.dassana.core.workflow.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class WorkflowResponse {

	private static final String DEFAULT = "default";
	private static final String WORKFLOW = "workflow";

	@JsonProperty(DEFAULT)
	private boolean isDefault = true;

	@JsonProperty(WORKFLOW)
	private String workflow;

	private Gson gson = new Gson();

	public WorkflowResponse(String workflow) {
		this.workflow = workflow;
	}

	public WorkflowResponse(String workflow, boolean isDefault) {
		this.workflow = workflow;
		this.isDefault = isDefault;
	}

	public String toJson() {
		Map<String, Object> map = new HashMap<>();
		map.put(DEFAULT, isDefault);
		map.put(WORKFLOW, workflow);
		return gson.toJson(map);
	}

}
