package xdi2.manager.model;


public class Connection extends AbstractConnection {
	
	private String raCloudName;
	private String raCloudNumber;
	private String type;

	
	public String getRaCloudName() {
		return raCloudName;
	}

	public void setRaCloudName(String raCloudName) {
		this.raCloudName = raCloudName;
	}

	public String getRaCloudNumber() {
		return raCloudNumber;
	}

	public void setRaCloudNumber(String raCloudNumber) {
		this.raCloudNumber = raCloudNumber;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
}
