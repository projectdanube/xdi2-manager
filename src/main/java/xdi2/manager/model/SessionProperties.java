package xdi2.manager.model;

import java.io.Serializable;

public class SessionProperties implements Serializable {
	private static final long serialVersionUID = 8982798629085140357L;
	
	private String cloudName;
	private String cloudNumber;
	private String xdiEndpointUri;
	private Environment environment;
	private String cloudCardAppUri;

	public SessionProperties() {
		super();
	}

	public SessionProperties(CloudUser cloudUser) {
		this.cloudName = cloudUser.getCloudName();
		this.cloudNumber = cloudUser.getCloudNumber().toString();
		this.xdiEndpointUri = cloudUser.getXdiEndpointUri();
		this.environment = cloudUser.getEnvironment();
	}
	
	
	public String getCloudName() {
		return cloudName;
	}
	public void setCloudName(String cloudName) {
		this.cloudName = cloudName;
	}
	public String getCloudNumber() {
		return cloudNumber;
	}
	public void setCloudNumber(String cloudNumber) {
		this.cloudNumber = cloudNumber;
	}
	public String getXdiEndpointUri() {
		return xdiEndpointUri;
	}
	public void setXdiEndpointUri(String xdiEndpointUri) {
		this.xdiEndpointUri = xdiEndpointUri;
	}
	public Environment getEnvironment() {
		return environment;
	}
	public void setEnvironment(Environment environment) {
		this.environment = environment;
	}	
	public String getCloudCardAppUri() {
		return cloudCardAppUri;
	}
	public void setCloudCardAppUri(String cloudCardAppUri) {
		this.cloudCardAppUri = cloudCardAppUri;
	}
	
}
