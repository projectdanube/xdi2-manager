package xdi2.manager.service;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import xdi2.client.exceptions.Xdi2ClientException;
import xdi2.connector.facebook.api.FacebookApi;
import xdi2.connector.facebook.mapping.FacebookMapping;
import xdi2.connector.facebook.util.GraphUtil;
import xdi2.core.Graph;
import xdi2.core.LiteralNode;
import xdi2.core.constants.XDIAuthenticationConstants;
import xdi2.core.syntax.XDIAddress;
import xdi2.core.syntax.XDIStatement;
import xdi2.core.util.XDIAddressUtil;
import xdi2.manager.controller.FacebookController;
import xdi2.manager.model.CloudUser;
import xdi2.manager.model.FacebookConnect;
import xdi2.manager.model.FacebookProfile;
import xdi2.manager.model.FacebookProfileField;
import xdi2.messaging.Message;
import xdi2.messaging.MessageCollection;
import xdi2.messaging.MessageEnvelope;
import xdi2.messaging.response.MessagingResponse;

@Service
public class FacebookService {
	private static final Logger log = LoggerFactory.getLogger(FacebookService.class);

	public static final XDIAddress XDI_ADD_FACEBOOK_FIRST_NAME = XDIAddress.create("#(user)<#(first_name)>");
	public static final XDIAddress XDI_ADD_FACEBOOK_LAST_NAME = XDIAddress.create("#(user)<#(last_name)>");
	public static final XDIAddress XDI_ADD_FACEBOOK_GENDER = XDIAddress.create("#(user)<#(gender)>");
	public static final XDIAddress XDI_ADD_FACEBOOK_EMAIL = XDIAddress.create("#(user)<#(email)>");
	public static final XDIAddress XDI_ADD_FACEBOOK_WEBSITE = XDIAddress.create("#(user)<#(website)>");
	public static final XDIAddress XDI_ADD_FACEBOOK_BIRTHDAY = XDIAddress.create("#(user)<#(birthday)>");

    public static final Map<String, XDIAddress> XDI_FACEBOOK_PROFILE;
    static {
    	Map<String, XDIAddress> xdiFacebookProfile = new HashMap<String, XDIAddress>();
    	xdiFacebookProfile.put("firstName", XDI_ADD_FACEBOOK_FIRST_NAME);
    	xdiFacebookProfile.put("lastName", XDI_ADD_FACEBOOK_LAST_NAME);
    	xdiFacebookProfile.put("gender", XDI_ADD_FACEBOOK_GENDER);
    	xdiFacebookProfile.put("birthDate", XDI_ADD_FACEBOOK_BIRTHDAY);
    	xdiFacebookProfile.put("email", XDI_ADD_FACEBOOK_EMAIL);
    	xdiFacebookProfile.put("website", XDI_ADD_FACEBOOK_WEBSITE);
    	XDI_FACEBOOK_PROFILE = Collections.unmodifiableMap(xdiFacebookProfile);
    }

	@Autowired
	FacebookApi facebookApi;

	@Autowired
	FacebookMapping facebookMapping;

	public FacebookConnect getFacebookConnectStatus() throws Xdi2ClientException, IOException {
		CloudUser user = (CloudUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		FacebookConnect facebookConnect = new FacebookConnect();

		// Generate the OAuth URI
		String oAuthUri = facebookApi.startOAuth(null, generateOAuthReturnUri(), user.getCloudNumber().getXDIAddress());
		log.debug("Facebook OAuth URI " + oAuthUri);
		facebookConnect.setoAuthUri(oAuthUri);

		// Get User Id and Access Token
		MessageEnvelope messageEnvelope = new MessageEnvelope();
		MessageCollection messageCollection = messageEnvelope.getMessageCollection(user.getCloudNumber().getXDIAddress(), true);
		Message message = messageCollection.createMessage();
		message = user.prepareMessageToCloud(message);
		message.createGetOperation(FacebookMapping.XDI_ADD_FACEBOOK_CONTEXT);
		MessagingResponse messagingResponse = user.getXdiClient().send(messageEnvelope);

		XDIAddress facebookUserIdXri = GraphUtil.retrieveFacebookUserIdXri(messagingResponse.getResultGraph(), user.getCloudNumber().getXDIAddress());
		if (facebookUserIdXri != null) {

			String facebookAccessToken = GraphUtil.retrieveFacebookAccessToken(messagingResponse.getResultGraph(), facebookUserIdXri);

			facebookConnect.setUserId(facebookUserIdXri.toString());
			facebookConnect.setAccessToken(facebookAccessToken);

			log.debug(user.getCloudName() + " is connected to facebook (" + facebookUserIdXri.toString() + ") with token " + facebookAccessToken);
		}

		return facebookConnect;
	}	

	public void handleFacebookOAuthResponse(String code, String state) throws Exception {
		CloudUser user = (CloudUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		log.debug("Got response from facebook: " + code);

		facebookApi.checkState(state, user.getCloudNumber().getXDIAddress());

		String facebookAccessToken = facebookApi.exchangeCodeForAccessToken(generateOAuthReturnUri(), code);
		if (facebookAccessToken == null) throw new Exception("No access token received.");

		String facebookUserId = facebookApi.retrieveUserId(facebookAccessToken);
		XDIAddress facebookUserIdXri = facebookMapping.facebookUserIdToFacebookUserIdXri(facebookUserId);
		
		// Facebook User Id ex: (https://facebook.com/)[=]!1111/$ref/(https://facebook.com/)[=]!10205481317089832
		XDIStatement facebookUserIdStatement = XDIStatement.create(FacebookMapping.XDI_ADD_FACEBOOK_CONTEXT + user.getCloudNumber().toString() + "/$ref/" + FacebookMapping.XDI_ADD_FACEBOOK_CONTEXT + facebookUserIdXri);
		
		// Facebook OAuth Token ex: (https://facebook.com/)[=]!10205481317089832<$oauth><$token>&/&/"dfasdhfgasdfaghsdf"
		XDIStatement facebookAccessTokenStatement = XDIStatement.create("" + FacebookMapping.XDI_ADD_FACEBOOK_CONTEXT + facebookUserIdXri + XDIAuthenticationConstants.XDI_ADD_OAUTH_TOKEN + "/&/\"" + facebookAccessToken + "\"");

		MessageEnvelope messageEnvelope = new MessageEnvelope();
		MessageCollection messageCollection = messageEnvelope.getMessageCollection(user.getCloudNumber().getXDIAddress(), true);
		Message message = messageCollection.createMessage();
		message = user.prepareMessageToCloud(message);
		message.createSetOperation(facebookUserIdStatement);
		message.createSetOperation(facebookAccessTokenStatement);

		user.getXdiClient().send(messageEnvelope);

	}

	public void revokeFacebookConnect() throws Xdi2ClientException, IOException, JSONException {
		CloudUser user = (CloudUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		FacebookConnect facebookConnect = getFacebookConnectStatus();
		
		if (facebookConnect.getAccessToken() != null) {
			facebookApi.revokeAccessToken(facebookConnect.getAccessToken());
			
			XDIAddress facebookAccessTokenXdiAddress = XDIAddress.create("" + FacebookMapping.XDI_ADD_FACEBOOK_CONTEXT + facebookConnect.getUserId() + XDIAuthenticationConstants.XDI_ADD_OAUTH_TOKEN);
			XDIAddress facebookUserIdXdiAddress = XDIAddress.create("" + FacebookMapping.XDI_ADD_FACEBOOK_CONTEXT + user.getCloudNumber());

			MessageEnvelope messageEnvelope = new MessageEnvelope();
			MessageCollection messageCollection = messageEnvelope.getMessageCollection(user.getCloudNumber().getXDIAddress(), true);
			Message message = messageCollection.createMessage();
			message = user.prepareMessageToCloud(message);
			message.createDelOperation(facebookAccessTokenXdiAddress);
			message.createDelOperation(facebookUserIdXdiAddress);

			user.getXdiClient().send(messageEnvelope);
			
		}
	}

	private String generateOAuthReturnUri() {
		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
		
		String returnUri = request.getRequestURI().toString().replace(request.getRequestURI(), "") + request.getContextPath();
		returnUri += FacebookController.OAUTH_RETURN_URI;
		log.debug("Facebook OAuth return url: " + returnUri);
		
		return returnUri;
	}
	
	public FacebookProfile getFacebookProfile() throws Xdi2ClientException, IOException {
		CloudUser user = (CloudUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		FacebookConnect facebookConnect = getFacebookConnectStatus();

		FacebookProfile profile = new FacebookProfile();
		
		XDIAddress facebookContext = XDIAddress.create("" + FacebookMapping.XDI_ADD_FACEBOOK_CONTEXT + facebookConnect.getUserId());
		
		MessageEnvelope messageEnvelope = new MessageEnvelope();
		MessageCollection messageCollection = messageEnvelope.getMessageCollection(user.getCloudNumber().getXDIAddress(), true);
		Message message = messageCollection.createMessage();
		message = user.prepareMessageToCloud(message);
		
		for (XDIAddress fieldXdiAddress : XDI_FACEBOOK_PROFILE.values()) {
			message.createGetOperation(XDIAddressUtil.concatXDIAddresses(facebookContext, fieldXdiAddress));
		}

		MessagingResponse messagingResponse = user.getXdiClient().send(messageEnvelope);
		
		for (String field : XDI_FACEBOOK_PROFILE.keySet()) {
			profile.putField(field, generateFacebookField(XDIAddressUtil.concatXDIAddresses(facebookContext, XDI_FACEBOOK_PROFILE.get(field)), messagingResponse.getResultGraph()));
		}
		
		return profile;
	}
	
	private FacebookProfileField generateFacebookField(XDIAddress fieldXdiAddress, Graph graph) {
		
		LiteralNode l = graph.getRootContextNode().getDeepLiteralNode(fieldXdiAddress);
		if (l != null) {
			return new FacebookProfileField(l.getLiteralDataString(), fieldXdiAddress.toString());
		}
		
		return null;
	}
	

}
