package xdi2.manager.model;

import java.io.Serializable;

import xdi2.client.XDIClient;
import xdi2.client.http.XDIHttpClient;
import xdi2.core.features.linkcontracts.instance.RootLinkContract;
import xdi2.core.syntax.CloudNumber;
import xdi2.messaging.Message;

public class CloudUser implements Serializable {
	private static final long serialVersionUID = -3527442227194320885L;
	
	private String cloudName;
	private String cloudNumber;
	private String xdiEndpointUrl;
	private String secretToken;	
	private Environment environment;

	public CloudUser(String cloudName, CloudNumber cloudNumber, String xdiEndpointUrl, String secretToken, Environment environment) {
		super();
		this.cloudName = cloudName;
		this.cloudNumber = cloudNumber.toString();
		this.xdiEndpointUrl = xdiEndpointUrl;
		this.secretToken = secretToken;
		this.environment = environment;
	}

	public Message prepareMessageToCloud(Message message) {
		message.setToPeerRootXDIArc(getCloudNumber().getPeerRootXDIArc());
		message.setLinkContract(RootLinkContract.class);
		message.setSecretToken(this.secretToken);

		return message;
	}
	
	public XDIClient getXdiClient() {
		return new XDIHttpClient(this.xdiEndpointUrl);
	}

	public CloudNumber getCloudNumber() {
		return CloudNumber.create(this.cloudNumber);
	}

	public String getCloudName() {
		return cloudName;
	}
	
	public String getSecretToken() {
		return secretToken;
	}

	public String getXdiEndpointUrl() {
		return xdiEndpointUrl;
	}
	
	public Environment getEnvironment() {
		return environment;
	}
}
