package xdi2.manager.model;

import java.util.HashMap;
import java.util.Map;

public class FacebookProfile {
	
	private Map<String, FacebookProfileField> fields;
	
	public FacebookProfile() {
		super();
		this.fields = new HashMap<String, FacebookProfileField>();
	}

	public Map<String, FacebookProfileField> getFields() {
		return fields;
	}

	public void putField(String field, FacebookProfileField value) {
		this.fields.put(field, value);
	}
	
	public FacebookProfileField getField(String field) {
		return this.fields.get(field);
	}	
	
}
