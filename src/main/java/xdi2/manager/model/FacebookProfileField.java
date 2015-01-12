package xdi2.manager.model;

public class FacebookProfileField {

	private String xdiAddress;
	private String value;
		
	public FacebookProfileField(String value, String xdiAddress) {
		super();
		this.xdiAddress = xdiAddress;
		this.value = value;
	}
	
	
	public String getXdiAddress() {
		return xdiAddress;
	}
	public void setXdiAddress(String xdiAddress) {
		this.xdiAddress = xdiAddress;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
}
