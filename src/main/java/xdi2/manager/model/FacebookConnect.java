package xdi2.manager.model;

public class FacebookConnect {

	private String oAuthUrl;
	private String userId;
	private String accessToken;
	
	
	public String getoAuthUrl() {
		return oAuthUrl;
	}
	public void setoAuthUrl(String oAuthUrl) {
		this.oAuthUrl = oAuthUrl;
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
