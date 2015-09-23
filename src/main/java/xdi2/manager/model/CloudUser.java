package xdi2.manager.model;

import java.io.Serializable;

import xdi2.client.XDIClient;
import xdi2.client.impl.http.XDIHttpClient;
import xdi2.core.features.linkcontracts.instance.RootLinkContract;
import xdi2.core.syntax.CloudNumber;
import xdi2.messaging.Message;

public class CloudUser implements Serializable {
	private static final long serialVersionUID = -3527442227194320885L;
	
	private String cloudName;
	private String cloudNumber;
	private String xdiEndpointUri;
	private String secret;	

	public CloudUser(String cloudName, CloudNumber cloudNumber, String xdiEndpointUri, String secret) {
		super();
		this.cloudName = cloudName;
		this.cloudNumber = cloudNumber.toString();
		this.xdiEndpointUri = xdiEndpointUri;
		this.secret = secret;
	}

	public Message prepareMessageToCloud(Message message) {
		message.setToPeerRootXDIArc(getCloudNumber().getPeerRootXDIArc());
		message.setLinkContractClass(RootLinkContract.class);
		message.setSecretToken(this.secret);

		return message;
	}
	
	public XDIClient getXdiClient() {
		return new XDIHttpClient(this.xdiEndpointUri);
	}

	public CloudNumber getCloudNumber() {
		return CloudNumber.create(this.cloudNumber);
	}

	public String getCloudName() {
		return cloudName;
	}
	
	public String getSecretToken() {
		return secret;
	}

	public String getXdiEndpointUri() {
		return xdiEndpointUri;
	}
}
