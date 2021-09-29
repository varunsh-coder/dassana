package app.dassana.core.workflow.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class WorkflowResponse {

	@JsonProperty("default")
	private boolean isDefault = true;
	@JsonProperty("workflow")
	private String workflow;

	public WorkflowResponse(String workflow) {
		this.workflow = workflow;
	}

	public WorkflowResponse(String workflow, boolean isDefault) {
		this.workflow = workflow;
		this.isDefault = isDefault;
	}

	public boolean isDefault() {
		return isDefault;
	}

	public String getWorkflow() {
		return workflow;
	}
}
