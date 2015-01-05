package xdi2.manager.model;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.collections4.map.MultiValueMap;

public abstract class AbstractConnection {

	private String xdi;
	private String xdiAddress;

	private boolean requireSignature;
	private boolean requireSecretToken;

	private MultiValueMap<String, String> permissions;

	public AbstractConnection() {
		permissions = new MultiValueMap<String, String>();
	}

	public void addPermission(String subGraph, String action) {
		this.permissions.put(subGraph, action);
	}

	public String getXdi() {
		return xdi;
	}

	public void setXdi(String xdi) {
		this.xdi = xdi;
	}

	public MultiValueMap<String, String> getPermissions() {
		return permissions;
	}

	public void setPermissions(MultiValueMap<String, String> permissions) {
		this.permissions = permissions;
	}

	public boolean getRequireSignature() {
		return requireSignature;
	}

	public void setRequireSignature(boolean requireSignature) {
		this.requireSignature = requireSignature;
	}

	public boolean getRequireSecretToken() {
		return requireSecretToken;
	}

	public void setRequireSecretToken(boolean requireSecretToken) {
		this.requireSecretToken = requireSecretToken;
	}

	public String getXdiAddress() {
		return xdiAddress;
	}

	public void setXdiAddress(String xdiAddress) {
		this.xdiAddress = xdiAddress;
	}
	
	public String getId() {
		return convertXdiAddressToId(this.xdiAddress);
	}
	
	public static String convertIdToXdiAddress(String id) {
		return StringUtils.newStringUtf8(Base64.decodeBase64(id));
	}
	
	public static String convertXdiAddressToId(String xdiAddress) {
		return Base64.encodeBase64String(xdiAddress.getBytes());
	}
}
