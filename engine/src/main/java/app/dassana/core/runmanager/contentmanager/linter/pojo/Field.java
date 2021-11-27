package app.dassana.core.runmanager.contentmanager.linter.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Field {
	@JsonProperty
	private String id;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
}
