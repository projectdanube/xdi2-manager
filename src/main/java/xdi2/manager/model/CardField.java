package xdi2.manager.model;

public class CardField {

	private String xdiStatement;
	private String value;
	private CardFieldPrivacy privacy;
	
	public CardField() {
		super();
	}
	
	public CardField(String xdiStatement, String value, CardFieldPrivacy privacy) {
		super();
		this.xdiStatement = xdiStatement;
		this.value = value;
		this.privacy = privacy;
	}
	
	public String getXdiStatement() {
		return xdiStatement;
	}
	public void setXdiStatement(String xdiStatement) {
		this.xdiStatement = xdiStatement;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public CardFieldPrivacy getPrivacy() {
		return privacy;
	}
	public void setPrivacy(CardFieldPrivacy privacy) {
		this.privacy = privacy;
	}
	
}
