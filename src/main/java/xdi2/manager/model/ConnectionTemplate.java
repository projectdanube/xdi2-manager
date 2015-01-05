package xdi2.manager.model;

public class ConnectionTemplate extends AbstractConnection {

	private String tag;
	private String description;
	
	
	public String getTag() {
		return tag;
	}
	public void setTag(String tag) {
		this.tag = tag;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
}
