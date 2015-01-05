package xdi2.manager.model.status;

public class DiscoveryCloudStatus {

	private String cloudName;

	private String cloudNumber;
	private String cloudEndpoint;
	private String encryptionPublicKey;
	private String signaturePublicKey;
	
	public DiscoveryCloudStatus(String cloudName) {
		super();
		this.cloudName = cloudName;
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

	public String getCloudEndpoint() {
		return cloudEndpoint;
	}

	public void setCloudEndpoint(String cloudEndpoint) {
		this.cloudEndpoint = cloudEndpoint;
	}

	public String getEncryptionPublicKey() {
		return encryptionPublicKey;
	}

	public void setEncryptionPublicKey(String encryptionPublicKey) {
		this.encryptionPublicKey = encryptionPublicKey;
	}

	public String getSignaturePublicKey() {
		return signaturePublicKey;
	}

	public void setSignaturePublicKey(String signaturePublicKey) {
		this.signaturePublicKey = signaturePublicKey;
	}
	
	
	
}
