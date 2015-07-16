package xdi2.manager.service;

import java.net.URI;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import xdi2.client.XDIClient;
import xdi2.client.exceptions.Xdi2ClientException;
import xdi2.client.exceptions.Xdi2DiscoveryException;
import xdi2.client.impl.http.XDIHttpClient;
import xdi2.client.impl.http.ssl.XDI2X509TrustManager;
import xdi2.core.Relation;
import xdi2.core.features.linkcontracts.instance.PublicLinkContract;
import xdi2.core.syntax.CloudNumber;
import xdi2.core.syntax.XDIAddress;
import xdi2.core.syntax.XDIStatement;
import xdi2.core.util.XDIStatementUtil;
import xdi2.discovery.XDIDiscoveryClient;
import xdi2.discovery.XDIDiscoveryResult;
import xdi2.messaging.Message;
import xdi2.messaging.MessageEnvelope;
import xdi2.messaging.response.MessagingResponse;

@Service
public class ReverseNameResolutionService {
	private static final Logger log = LoggerFactory.getLogger(ReverseNameResolutionService.class);

	private static final XDIStatement XDI_CLOUD_NAMES = XDIStatement.create("/$is$ref/{}");
	
	private ConcurrentMap<String, String> namesCache;

	public ReverseNameResolutionService() {
		this.namesCache = new ConcurrentHashMap<String, String>();
	}

	public String getCloudName(CloudNumber cloudNumber) {
		Assert.notNull(cloudNumber);

		return getCloudName(cloudNumber.toString());
	}

	public String getCloudName(String cloudNumber) {
		Assert.hasLength(cloudNumber);

		long start = System.currentTimeMillis();
		String cloudName = namesCache.get(cloudNumber);
		if (cloudName == null) {
			try {
				cloudName = getCloudNameFromPublicLC(cloudNumber);
			}
			catch (Exception e) {
				log.warn("Not possible to get a cloud name for " + cloudNumber + " - " + e.getMessage());
				return null;
			}

			if (cloudName == null) {
				log.warn("Not possible to get a cloud name for " + cloudNumber);
				return null;
			}
			
			namesCache.put(cloudNumber, cloudName);
		}
		
		log.debug("CloudNumber " + cloudNumber + " translated to " + cloudName + " in " + (System.currentTimeMillis() - start) + "ms");

		return cloudName;
	}




	private String getCloudNameFromPublicLC(String cloudNumberStr) throws Xdi2DiscoveryException, Xdi2ClientException {

		CloudNumber cloudNumber = CloudNumber.create(cloudNumberStr);
		
		// Discover Cloud Endpoint
		XDI2X509TrustManager.enable();
		XDIDiscoveryClient xdiDiscoveryClient = XDIDiscoveryClient.XDI2_DISCOVERY_CLIENT;

        XDIDiscoveryResult result = xdiDiscoveryClient.discoverFromRegistry(cloudNumber.getXDIAddress());
        if (result == null)
        	return null;
        
        URI xdiEndpoint = result.getXdiEndpointUri();
        
        // Query cloud for cloud names
		XDIClient client = new XDIHttpClient(xdiEndpoint);
		MessageEnvelope me = new MessageEnvelope();
		Message m = me.createMessage(XDIAddress.create("$anon"));
		m.setToPeerRootXDIArc(cloudNumber.getPeerRootXDIArc());
		m.setLinkContract(PublicLinkContract.class);

		m.createGetOperation(XDIStatementUtil.concatXDIStatement(cloudNumber.getXDIAddress(), XDI_CLOUD_NAMES));

		MessagingResponse mr = client.send(me);
		
		// read the response
		Relation relation = mr.getResultGraph().getDeepRelation(cloudNumber.getXDIAddress(), XDIAddress.create("$is$ref"));
		if (relation == null) return null;
		
		return relation.getTargetXDIAddress().toString();
	}
}
