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
import xdi2.manager.model.Environment;
import xdi2.manager.model.status.DiscoveryCloudStatus;
import xdi2.manager.util.XdiUtils;

@Service
public class DiscoveryService {

	public DiscoveryCloudStatus getCloudStatus (Environment env, String cloudName) throws Xdi2ClientException {
		Assert.notNull(env);
		Assert.hasLength(cloudName);
		
		cloudName = XdiUtils.normalizeCloudName(cloudName);
		
		DiscoveryCloudStatus status = new DiscoveryCloudStatus(cloudName);
		
		XDIDiscoveryResult result = getXdiDiscoveryForEnv(env).discover(XDIAddress.create(cloudName));

		status.setCloudNumber(ObjectUtils.toString(result.getCloudNumber(), null));

		status.setCloudEndpoint(ObjectUtils.toString(result.getXdiEndpointUri(), null));

		status.setEncryptionPublicKey(result.getEncryptionPublicKey() != null ? Base64.encodeBase64String(result.getEncryptionPublicKey().getEncoded()) : null);
		status.setSignaturePublicKey(result.getSignaturePublicKey() != null ? Base64.encodeBase64String(result.getSignaturePublicKey().getEncoded()) : null);
		
		return status;
	}
	
	public CloudNumber discover (Environment env, String cloudName) throws Xdi2ClientException {
		Assert.notNull(env);
		Assert.hasLength(cloudName);
		cloudName = XdiUtils.normalizeCloudName(cloudName);
		
		XDIDiscoveryResult result = getXdiDiscoveryForEnv(env).discoverFromRegistry(XDIAddress.create(cloudName));

		return result.getCloudNumber();
	}
	
	private XDIDiscoveryClient getXdiDiscoveryForEnv(Environment env) {
		
		if (env == Environment.OTE) 
			return XDIDiscoveryClient.XDI2_NEUSTAR_OTE_DISCOVERY_CLIENT;
		else
			return XDIDiscoveryClient.XDI2_NEUSTAR_PROD_DISCOVERY_CLIENT;
	}
	
}
