package xdi2.manager.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import xdi2.client.exceptions.Xdi2ClientException;
import xdi2.manager.model.CloudUser;
import xdi2.manager.model.SessionProperties;
import xdi2.manager.service.Configuration;

@RestController
@RequestMapping("/api/1.0/")
public class GenericController extends AbstractController {
	
	@Autowired
	private Configuration configuration;

	@RequestMapping(value = "/session/properties/", method = RequestMethod.GET)
	public SessionProperties getSessionDetails() throws Xdi2ClientException {
		CloudUser user = (CloudUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		SessionProperties sessionProperties = new SessionProperties(user);
		sessionProperties.setCloudCardAppUri(configuration.getCloudCardAppUri());
		return sessionProperties;
	}
	
}
