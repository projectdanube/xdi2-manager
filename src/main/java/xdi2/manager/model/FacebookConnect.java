package xdi2.manager.model;

public class FacebookConnect {

	private String oAuthUri;
	private String userId;
	private String accessToken;
	
	
	public String getoAuthUri() {
		return oAuthUri;
	}
	public void setoAuthUri(String oAuthUri) {
		this.oAuthUri = oAuthUri;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getAccessToken() {
		return accessToken;
	}
	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}
}
