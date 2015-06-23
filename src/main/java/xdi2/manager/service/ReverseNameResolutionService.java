package xdi2.manager.service;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import xdi2.client.XDIClient;
import xdi2.client.exceptions.Xdi2ClientException;
import xdi2.client.exceptions.Xdi2DiscoveryException;
import xdi2.client.http.XDIHttpClient;
import xdi2.client.http.ssl.XDI2X509TrustManager;
import xdi2.core.Relation;
import xdi2.core.features.linkcontracts.instance.PublicLinkContract;
import xdi2.core.syntax.CloudNumber;
import xdi2.core.syntax.XDIAddress;
import xdi2.core.syntax.XDIStatement;
import xdi2.core.util.XDIStatementUtil;
import xdi2.discovery.XDIDiscoveryClient;
import xdi2.discovery.XDIDiscoveryResult;
import xdi2.manager.model.Environment;
import xdi2.messaging.Message;
import xdi2.messaging.MessageEnvelope;
import xdi2.messaging.MessageResult;

@Service
public class ReverseNameResolutionService {
	private static final Logger log = LoggerFactory.getLogger(ReverseNameResolutionService.class);

	private static final XDIStatement XDI_CLOUD_NAMES = XDIStatement.create("/$is$ref/{}");
	
	private ConcurrentMap<String, String> namesCache;

	public ReverseNameResolutionService() {
		this.namesCache = new ConcurrentHashMap<String, String>();
	}

	public String getCloudName(Environment env, CloudNumber cloudNumber) {
		Assert.notNull(env);
		Assert.notNull(cloudNumber);

		return getCloudName(env, cloudNumber.toString());
	}

	public String getCloudName(Environment env, String cloudNumber) {
		Assert.notNull(env);
		Assert.hasLength(cloudNumber);

		long start = System.currentTimeMillis();
		String cloudName = namesCache.get(env + cloudNumber);
		if (cloudName == null) {
			try {
				cloudName = getCloudNameFromPublicLC(env, cloudNumber);
			}
			catch (Exception e) {
				log.warn("Not possible to get a cloud name for [" + env + "] " + cloudNumber + " - " + e.getMessage());
				return null;
			}

			if (cloudName == null) {
				log.warn("Not possible to get a cloud name for [" + env + "] " + cloudNumber);
				return null;
			}
			
			namesCache.put(env + cloudNumber, cloudName);
		}
		
		log.debug("CloudNumber [" + env + "] " + cloudNumber + " translated to " + cloudName + " in " + (System.currentTimeMillis() - start) + "ms");

		return cloudName;
	}




	private String getCloudNameFromPublicLC(Environment env, String cloudNumberStr) throws Xdi2DiscoveryException, Xdi2ClientException, MalformedURLException {

		CloudNumber cloudNumber = CloudNumber.create(cloudNumberStr);
		
		// Discover Cloud Endpoint
		XDI2X509TrustManager.enable();
		XDIDiscoveryClient xdiDiscoveryClient;
		if (env == Environment.OTE) 
			xdiDiscoveryClient = XDIDiscoveryClient.NEUSTAR_OTE_DISCOVERY_CLIENT;
		else
			xdiDiscoveryClient = XDIDiscoveryClient.NEUSTAR_PROD_DISCOVERY_CLIENT;

        XDIDiscoveryResult result = xdiDiscoveryClient.discoverFromRegistry(cloudNumber.getXDIAddress(), null);
        if (result == null)
        	return null;
        
        URL xdiEndpoint = result.getXdiEndpointUrl();
        
        // Query cloud for cloud names
		XDIClient client = new XDIHttpClient(xdiEndpoint);
		MessageEnvelope me = new MessageEnvelope();
		Message m = me.createMessage(XDIAddress.create("$anon"));
		m.setToPeerRootXDIArc(cloudNumber.getPeerRootXDIArc());
		m.setLinkContract(PublicLinkContract.class);

		m.createGetOperation(XDIStatementUtil.concatXDIStatement(cloudNumber.getXDIAddress(), XDI_CLOUD_NAMES));

		MessageResult mr = client.send(me, null);
		
		// read the response
		Relation relation = mr.getGraph().getDeepRelation(cloudNumber.getXDIAddress(), XDIAddress.create("$is$ref"));
		if (relation == null) return null;
		
		return relation.getTargetXDIAddress().toString();
	}
}
