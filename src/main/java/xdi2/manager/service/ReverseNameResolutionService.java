package xdi2.manager.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import xdi2.core.syntax.XDIAddress;
import xdi2.discovery.XDIDiscoveryClient;
import xdi2.discovery.XDIDiscoveryResult;

@Service
public class ReverseNameResolutionService {
	private static final Logger log = LoggerFactory.getLogger(ReverseNameResolutionService.class);

	public String getCloudName(String cloudNumber) {
		Assert.hasLength(cloudNumber);

		long start = System.currentTimeMillis();
		String cloudName;
		
		try {

			XDIDiscoveryResult result = XDIDiscoveryClient.DEFAULT_DISCOVERY_CLIENT.discover(XDIAddress.create(cloudNumber));
			cloudName = result.getCloudNames()[0].toString();
		} catch (Exception e) {
			log.warn("Not possible to get a cloud name for " + cloudNumber + " - " + e.getMessage());
			return null;
		}

		if (cloudName == null) {
			log.warn("Not possible to get a cloud name for " + cloudNumber);
			return null;
		}
			
		
		log.debug("CloudNumber " + cloudNumber + " translated to " + cloudName + " in " + (System.currentTimeMillis() - start) + "ms");

		return cloudName;
	}
}
