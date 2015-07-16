package xdi2.manager.controller;

import org.apache.commons.lang.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import xdi2.client.exceptions.Xdi2ClientException;
import xdi2.manager.model.CloudUser;
import xdi2.manager.model.status.DiscoveryCloudStatus;
import xdi2.manager.model.status.SelfCloudStatus;
import xdi2.manager.service.CloudService;
import xdi2.manager.service.DiscoveryService;

@RestController
@RequestMapping("/api/1.0/")
public class CloudStatusController extends AbstractController {
	private static final Logger log = LoggerFactory.getLogger(CloudStatusController.class);
	
	@Autowired
	private DiscoveryService discoveryService;
	
	@Autowired
	private CloudService cloudService;	
	
	@RequestMapping(value = "/discovery/status/", method = RequestMethod.GET)
	public DiscoveryCloudStatus getDiscoveryStatus() throws Xdi2ClientException {
		CloudUser user = (CloudUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		
		long start = System.currentTimeMillis();
		DiscoveryCloudStatus status = discoveryService.getCloudStatus(user.getCloudName());
		log.debug("getDiscoveryStatus (" + user.getCloudName() + ") took " + (System.currentTimeMillis() - start) + " ms.");
		
		return status;
	}
	
	@RequestMapping(value = "/cloud/status/", method = RequestMethod.GET)
	public SelfCloudStatus getPublicLCCloudStatus() throws Xdi2ClientException {
		SelfCloudStatus status = cloudService.getCloudStatus();
		return status;
	}
	
	@RequestMapping(value = "/discovery/{cloudName}", method = RequestMethod.GET)
	public String discoverCloudName(@PathVariable String cloudName) throws Xdi2ClientException {
		CloudUser user = (CloudUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		return ObjectUtils.toString("\"" + discoveryService.discover(cloudName) + "\"", null);
	}

}
