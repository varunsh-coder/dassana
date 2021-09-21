package app.dassana.core.workflow.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class WorkflowResponse {

	@JsonProperty("default")
	private boolean isDefault = true;
	@JsonProperty("workflow") //set name so default always appears first during json transformation
	private String workflow;

	public WorkflowResponse(String workflow) {
		this.workflow = workflow;
	}

	public WorkflowResponse(String workflow, boolean isDefault) {
		this.workflow = workflow;
		this.isDefault = isDefault;
	}
}
