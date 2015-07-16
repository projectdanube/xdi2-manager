package xdi2.manager.service;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import xdi2.client.exceptions.Xdi2ClientException;
import xdi2.core.syntax.CloudNumber;
import xdi2.core.syntax.XDIAddress;
import xdi2.discovery.XDIDiscoveryClient;
import xdi2.discovery.XDIDiscoveryResult;
import xdi2.manager.model.status.DiscoveryCloudStatus;
import xdi2.manager.util.XdiUtils;

@Service
public class DiscoveryService {

	public DiscoveryCloudStatus getCloudStatus (String cloudName) throws Xdi2ClientException {
		Assert.hasLength(cloudName);
		
		cloudName = XdiUtils.normalizeCloudName(cloudName);
		
		DiscoveryCloudStatus status = new DiscoveryCloudStatus(cloudName);
		
		XDIDiscoveryResult result = XDIDiscoveryClient.XDI2_DISCOVERY_CLIENT.discover(XDIAddress.create(cloudName));

		status.setCloudNumber(ObjectUtils.toString(result.getCloudNumber(), null));

		status.setCloudEndpoint(ObjectUtils.toString(result.getXdiEndpointUri(), null));

		status.setEncryptionPublicKey(result.getEncryptionPublicKey() != null ? Base64.encodeBase64String(result.getEncryptionPublicKey().getEncoded()) : null);
		status.setSignaturePublicKey(result.getSignaturePublicKey() != null ? Base64.encodeBase64String(result.getSignaturePublicKey().getEncoded()) : null);
		
		return status;
	}
	
	public CloudNumber discover (String cloudName) throws Xdi2ClientException {
		Assert.hasLength(cloudName);
		cloudName = XdiUtils.normalizeCloudName(cloudName);
		
		XDIDiscoveryResult result = XDIDiscoveryClient.XDI2_DISCOVERY_CLIENT.discoverFromRegistry(XDIAddress.create(cloudName));

		return result.getCloudNumber();
	}

}
