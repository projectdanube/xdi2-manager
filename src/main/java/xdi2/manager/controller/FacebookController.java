package xdi2.manager.controller;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import xdi2.client.exceptions.Xdi2ClientException;
import xdi2.manager.model.FacebookConnect;
import xdi2.manager.model.FacebookProfile;
import xdi2.manager.service.FacebookService;

@Controller
public class FacebookController extends AbstractController {
	private static final Logger log = LoggerFactory.getLogger(FacebookController.class);


	public final static String OAUTH_RETURN_URL = "/cloud/facebook/";
	
	@Autowired
	FacebookService facebookService;
		
	@ResponseBody
	@RequestMapping(value = "/api/1.0/cloud/facebook/", method = RequestMethod.GET)
	public FacebookConnect getFacebookConnectStatus() throws Xdi2ClientException, IOException {
		return facebookService.getFacebookConnectStatus();
	}
	
	@ResponseBody
	@RequestMapping(value = "/api/1.0/cloud/facebook/", method = RequestMethod.DELETE)
	public void revokeFacebookConnect() throws Xdi2ClientException, IOException, JSONException {
		facebookService.revokeFacebookConnect();
	}
	
	@ResponseBody
	@RequestMapping(value = "/api/1.0/cloud/facebook/profile/", method = RequestMethod.GET)
	public FacebookProfile getFacebookProfile() throws Xdi2ClientException, IOException {
		return facebookService.getFacebookProfile();
	}
	
	@RequestMapping(value = OAUTH_RETURN_URL, method = RequestMethod.GET)
	public String handleFacebookOAuthResponse(
			@RequestParam(required=false) String code,
			@RequestParam(required=false) String state,
			@RequestParam(required=false) String error,
			@RequestParam(required=false) String error_code,
			@RequestParam(required=false) String error_description,
			@RequestParam(required=false) String error_reason,
			Model model) {


		if (StringUtils.isNotBlank(code) && StringUtils.isNotBlank(state)) {			
			try {
				facebookService.handleFacebookOAuthResponse(code, state);
				model.addAttribute("successMsg", "You cloud was connected with Facebook! Please close this window.");
			} catch (Exception e) {
				model.addAttribute("errorMsg", "Error connecting cloud with Facebook: " + e.getMessage());
				log.error("Error connecting with facebook", e);
			}
		}
		else if (StringUtils.isNotBlank(error)) {
			String errorDetails = error + "(" + error_code + ") - " + error_description + "\t" + error_reason;
			
			model.addAttribute("errorDetails", errorDetails);
			model.addAttribute("errorMsg", "Error connecting cloud with Facebook");			
		}
		
		
		return "facebook";
	}
	
}
