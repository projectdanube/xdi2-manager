package xdi2.manager.model;

import java.util.HashMap;
import java.util.Map;

import xdi2.manager.util.XdiUtils;

public class Card {

	@SuppressWarnings("unused")
	private String id;

	private String tag;
	private String description;
	private Map<String, CardField> fields;

	private String xdiAddress;
	private String messageConnectButton;
	private String backgroundImage;
	private boolean isDefault;

	public Card() {
		super();

		this.fields = new HashMap<String, CardField>();
	}


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
	public Map<String, CardField> getFields() {
		return fields;
	}
	public void setFields(Map<String, CardField> fields) {
		this.fields = fields;
	}

	public CardField putField(String fieldKey, CardField field) {
		return this.fields.put(fieldKey, field);
	}

	public CardField getField(String fieldKey) {
		return this.fields.get(fieldKey);
	}

	public String getXdiAddress() {
		return xdiAddress;
	}

	public void setXdiAddress(String xdiAddress) {
		this.xdiAddress = xdiAddress;
	}

	public String getId() {
		return XdiUtils.convertXdiAddressToId(xdiAddress);
	}
	public void setId(String id) {
		// ignore
	}

	public String getMessageConnectButton() {
		return messageConnectButton;
	}

	public void setMessageConnectButton(String messageConnectButton) {
		this.messageConnectButton = messageConnectButton;
	}

	public String getBackgroundImage() {
		return backgroundImage;
	}

	public void setBackgroundImage(String backgroundImage) {
		this.backgroundImage = backgroundImage;
	}


	public boolean isDefault() {
		return isDefault;
	}
	
	public void setDefault(boolean isDefault) {
		this.isDefault = isDefault;
	}
	

}
