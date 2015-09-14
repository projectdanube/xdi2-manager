package xdi2.manager.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import xdi2.client.exceptions.Xdi2ClientException;
import xdi2.core.ContextNode;
import xdi2.core.LiteralNode;
import xdi2.core.Relation;
import xdi2.core.constants.XDIConstants;
import xdi2.core.constants.XDIDictionaryConstants;
import xdi2.core.constants.XDISecurityConstants;
import xdi2.core.features.linkcontracts.LinkContractTemplates;
import xdi2.core.features.linkcontracts.LinkContracts;
import xdi2.core.features.linkcontracts.instance.LinkContract;
import xdi2.core.features.linkcontracts.template.LinkContractTemplate;
import xdi2.core.syntax.XDIAddress;
import xdi2.core.syntax.XDIStatement;
import xdi2.core.util.XDIAddressUtil;
import xdi2.core.util.iterators.ReadOnlyIterator;
import xdi2.manager.model.CloudUser;
import xdi2.manager.model.Connection;
import xdi2.manager.model.ConnectionTemplate;
import xdi2.manager.model.PersonalProfile;
import xdi2.manager.model.status.SelfCloudStatus;
import xdi2.manager.util.XdiModelConverter;
import xdi2.messaging.Message;
import xdi2.messaging.MessageCollection;
import xdi2.messaging.MessageEnvelope;
import xdi2.messaging.response.MessagingResponse;
import xdi2.messaging.target.contributor.impl.keygen.GenerateKeyContributor;

@Service
public class CloudService {
	private static final Logger log = LoggerFactory.getLogger(CloudService.class);

	@Autowired
	private XdiModelConverter xdiModelConverter;
	
	@Autowired
	private ReverseNameResolutionService reverseNameResolutionService;

	@Secured("IS_AUTHENTICATED")
	public PersonalProfile getPersonalProfile() throws Xdi2ClientException {
		CloudUser user = (CloudUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		MessageEnvelope messageEnvelope = new MessageEnvelope();
		MessageCollection messageCollection = messageEnvelope.getMessageCollection(user.getCloudNumber().getXDIAddress(), true);
		Message message = messageCollection.createMessage();
		message = user.prepareMessageToCloud(message);
		xdiModelConverter.prepareMessageForPersonalProfile(message);

		long start = System.currentTimeMillis();
		MessagingResponse messagingResponse = user.getXdiClient().send(messageEnvelope);
		log.debug("network time: " + (System.currentTimeMillis() - start));		

		PersonalProfile profile = xdiModelConverter.convertToPersonalProfile(messagingResponse.getResultGraph());

		return profile;
	}

	@Secured("IS_AUTHENTICATED")
	public void updatePersonalProfile(PersonalProfile profile) throws Xdi2ClientException {
		Assert.notNull(profile);

		CloudUser user = (CloudUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		MessageEnvelope messageEnvelope = new MessageEnvelope();
		MessageCollection messageCollection = messageEnvelope.getMessageCollection(user.getCloudNumber().getXDIAddress(), true);
		Message message = messageCollection.createMessage();
		message = user.prepareMessageToCloud(message);

		List<XDIStatement> statements = xdiModelConverter.convertPersonalProfileToStatements(profile);
		message.createSetOperation(statements.iterator());

		user.getXdiClient().send(messageEnvelope);

	}

	@Secured("IS_AUTHENTICATED")
	public long getAllStatementsCount() throws Xdi2ClientException {
		CloudUser user = (CloudUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		MessageEnvelope messageEnvelope = new MessageEnvelope();
		MessageCollection messageCollection = messageEnvelope.getMessageCollection(user.getCloudNumber().getXDIAddress(), true);
		Message message = messageCollection.createMessage();
		message = user.prepareMessageToCloud(message);
		message.createGetOperation(XDIConstants.XDI_ADD_ROOT);

		MessagingResponse messagingResponse = user.getXdiClient().send(messageEnvelope);

		return messagingResponse.getResultGraph().getRootContextNode().getAllStatementCount();
	}

	@Secured("IS_AUTHENTICATED")
	public List<String> getCloudNames()  throws Xdi2ClientException {
		CloudUser user = (CloudUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		
		MessageEnvelope messageEnvelope = new MessageEnvelope();
		MessageCollection messageCollection = messageEnvelope.getMessageCollection(user.getCloudNumber().getXDIAddress(), true);
		Message message = messageCollection.createMessage();
		message = user.prepareMessageToCloud(message);
		
		message.createGetOperation(XDIStatement.create(user.getCloudNumber() + "/$is$ref/{}"));
		
		MessagingResponse messagingResponse = user.getXdiClient().send(messageEnvelope);

		ReadOnlyIterator<Relation> relations = messagingResponse.getResultGraph().getDeepRelations(user.getCloudNumber().getXDIAddress(), XDIAddress.create("$is$ref"));

		List<String> cloudNames = new ArrayList<String>();
		while (relations.hasNext()) {
			Relation r = relations.next();
			cloudNames.add(r.getTargetXDIAddress().toString());
		}

		return cloudNames;
	}

	@Secured("IS_AUTHENTICATED")
	public String getCloudNumber()  throws Xdi2ClientException {
		CloudUser user = (CloudUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		
		MessageEnvelope messageEnvelope = new MessageEnvelope();
		MessageCollection messageCollection = messageEnvelope.getMessageCollection(user.getCloudNumber().getXDIAddress(), true);
		Message message = messageCollection.createMessage();
		message = user.prepareMessageToCloud(message);
		
		message.createGetOperation(XDIStatement.create(user.getCloudNumber() + "/$is$ref/{}"));
		
		MessagingResponse messagingResponse = user.getXdiClient().send(messageEnvelope);

		ContextNode contextNode = messagingResponse.getResultGraph().getDeepContextNode(user.getCloudNumber().getXDIAddress());
		return contextNode.toString();
	}
	
	@Secured("IS_AUTHENTICATED")
	public List<String> getDependents()  throws Xdi2ClientException {
		CloudUser user = (CloudUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		
		MessageEnvelope messageEnvelope = new MessageEnvelope();
		MessageCollection messageCollection = messageEnvelope.getMessageCollection(user.getCloudNumber().getXDIAddress(), true);
		Message message = messageCollection.createMessage();
		message = user.prepareMessageToCloud(message);
		
		message.createGetOperation(XDIStatement.create(user.getCloudNumber() + "/$is#guardian/{}"));
		
		MessagingResponse messagingResponse = user.getXdiClient().send(messageEnvelope);

		ReadOnlyIterator<Relation> relations = messagingResponse.getResultGraph().getDeepRelations(user.getCloudNumber().getXDIAddress(), XDIAddress.create("$is#guardian"));

		List<String> dependents = new ArrayList<String>();
		while (relations.hasNext()) {
			Relation r = relations.next();
			
			String dependentCloudNumber = r.getTargetXDIAddress().toString();
			dependents.add(reverseNameResolutionService.getCloudName(dependentCloudNumber));
		}

		return dependents;
	}
	
	@Secured("IS_AUTHENTICATED")
	public List<String> getGuardians()  throws Xdi2ClientException {
		CloudUser user = (CloudUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		
		MessageEnvelope messageEnvelope = new MessageEnvelope();
		MessageCollection messageCollection = messageEnvelope.getMessageCollection(user.getCloudNumber().getXDIAddress(), true);
		Message message = messageCollection.createMessage();
		message = user.prepareMessageToCloud(message);
		
		message.createGetOperation(XDIStatement.create(user.getCloudNumber() + "/#guardian/{}"));
		
		MessagingResponse messagingResponse = user.getXdiClient().send(messageEnvelope);

		ReadOnlyIterator<Relation> relations = messagingResponse.getResultGraph().getDeepRelations(user.getCloudNumber().getXDIAddress(), XDIAddress.create("#guardian"));

		List<String> guardians = new ArrayList<String>();
		while (relations.hasNext()) {
			Relation r = relations.next();
			
			String dependentCloudNumber = r.getTargetXDIAddress().toString();
			
			// there is a link contract with this relation
			if (dependentCloudNumber.startsWith("[=]"))
				guardians.add(reverseNameResolutionService.getCloudName(dependentCloudNumber));
		}

		return guardians;
	}

	@Secured("IS_AUTHENTICATED")
	public void createConnection(Connection connection) throws Xdi2ClientException {
		Assert.notNull(connection);

		CloudUser user = (CloudUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		MessageEnvelope messageEnvelope = new MessageEnvelope();
		MessageCollection messageCollection = messageEnvelope.getMessageCollection(user.getCloudNumber().getXDIAddress(), true);
		Message message = messageCollection.createMessage();
		message = user.prepareMessageToCloud(message);

		LinkContract l = xdiModelConverter.convertConnectionToLinkContract(connection);

		message.createSetOperation(l.getContextNode().getGraph());

		user.getXdiClient().send(messageEnvelope);

	}

	@Secured("IS_AUTHENTICATED")
	public List<Connection> getConnections() throws Xdi2ClientException {
		CloudUser user = (CloudUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		MessageEnvelope messageEnvelope = new MessageEnvelope();
		MessageCollection messageCollection = messageEnvelope.getMessageCollection(user.getCloudNumber().getXDIAddress(), true);
		Message message = messageCollection.createMessage();
		message = user.prepareMessageToCloud(message);
		message.createGetOperation(user.getCloudNumber().getXDIAddress());

		MessagingResponse messagingResponse = user.getXdiClient().send(messageEnvelope);

		Iterator<LinkContract> linkContracts = LinkContracts.getAllLinkContracts(messagingResponse.getResultGraph());

		List<Connection> connections = new ArrayList<>();
		while (linkContracts.hasNext()) {
			LinkContract linkContract = linkContracts.next();
			connections.add(xdiModelConverter.convertLinkContractToConnection(linkContract));
		}

		return connections;
	}

	@Secured("IS_AUTHENTICATED")
	public Connection getConnection(final String id) throws Xdi2ClientException {
		Assert.hasLength(id);

		CloudUser user = (CloudUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		MessageEnvelope messageEnvelope = new MessageEnvelope();
		MessageCollection messageCollection = messageEnvelope.getMessageCollection(user.getCloudNumber().getXDIAddress(), true);
		Message message = messageCollection.createMessage();
		message = user.prepareMessageToCloud(message);

		String xdiAddress = Connection.convertIdToXdiAddress(id);
		message.createGetOperation(XDIAddress.create(xdiAddress));

		MessagingResponse messagingResponse = user.getXdiClient().send(messageEnvelope);

		Iterator<LinkContract> linkContracts = LinkContracts.getAllLinkContracts(messagingResponse.getResultGraph());		
		if (linkContracts.hasNext() == false) {
			return null;
		}

		return xdiModelConverter.convertLinkContractToConnection(linkContracts.next());
	}

	@Secured("IS_AUTHENTICATED")
	public void deleteConnection(String id) throws Xdi2ClientException {
		Assert.hasLength(id);

		CloudUser user = (CloudUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		MessageEnvelope messageEnvelope = new MessageEnvelope();
		MessageCollection messageCollection = messageEnvelope.getMessageCollection(user.getCloudNumber().getXDIAddress(), true);
		Message message = messageCollection.createMessage();
		message = user.prepareMessageToCloud(message);

		String xdiAddress = Connection.convertIdToXdiAddress(id);
		message.createDelOperation(XDIAddress.create(xdiAddress));

		user.getXdiClient().send(messageEnvelope);

	}

	//	@Secured("IS_AUTHENTICATED")
	//	public void updateConnection(Connection connection) throws Xdi2ClientException {
	//		Assert.notNull(connection);
	//		
	//				
	//	}

	@Secured("IS_AUTHENTICATED")
	public void createConnectionTemplate(ConnectionTemplate connectionTemplate) throws Xdi2ClientException {
		Assert.notNull(connectionTemplate);

		CloudUser user = (CloudUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		MessageEnvelope messageEnvelope = new MessageEnvelope();
		MessageCollection messageCollection = messageEnvelope.getMessageCollection(user.getCloudNumber().getXDIAddress(), true);
		Message message = messageCollection.createMessage();
		message = user.prepareMessageToCloud(message);

		LinkContractTemplate l = xdiModelConverter.convertConnectionTemplateToLinkContractTemplate(connectionTemplate);

		message.createSetOperation(l.getContextNode().getGraph());

		user.getXdiClient().send(messageEnvelope);

	}

	@Secured("IS_AUTHENTICATED")
	public List<ConnectionTemplate> getConnectionTemplates() throws Xdi2ClientException {
		CloudUser user = (CloudUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		MessageEnvelope messageEnvelope = new MessageEnvelope();
		MessageCollection messageCollection = messageEnvelope.getMessageCollection(user.getCloudNumber().getXDIAddress(), true);
		Message message = messageCollection.createMessage();
		message = user.prepareMessageToCloud(message);
		message.createGetOperation(user.getCloudNumber().getXDIAddress());

		MessagingResponse messagingResponse = user.getXdiClient().send(messageEnvelope);

		Iterator<LinkContractTemplate> linkContracts = LinkContractTemplates.getAllLinkContractTemplates(messagingResponse.getResultGraph());

		List<ConnectionTemplate> connections = new ArrayList<>();
		while (linkContracts.hasNext()) {
			LinkContractTemplate linkContractTemplate = linkContracts.next();
			connections.add(xdiModelConverter.convertLinkContractTemplateToConnectionTemplate(linkContractTemplate));
		}

		return connections;
	}

	@Secured("IS_AUTHENTICATED")
	public ConnectionTemplate getConnectionTemplate(final String id) throws Xdi2ClientException {
		Assert.hasLength(id);

		CloudUser user = (CloudUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		MessageEnvelope messageEnvelope = new MessageEnvelope();
		MessageCollection messageCollection = messageEnvelope.getMessageCollection(user.getCloudNumber().getXDIAddress(), true);
		Message message = messageCollection.createMessage();
		message = user.prepareMessageToCloud(message);

		String xdiAddress = Connection.convertIdToXdiAddress(id);
		message.createGetOperation(XDIAddress.create(xdiAddress));

		MessagingResponse messagingResponse = user.getXdiClient().send(messageEnvelope);

		Iterator<LinkContractTemplate> linkContractTemplate = LinkContractTemplates.getAllLinkContractTemplates(messagingResponse.getResultGraph());		
		if (linkContractTemplate.hasNext() == false) {
			return null;
		}

		return xdiModelConverter.convertLinkContractTemplateToConnectionTemplate(linkContractTemplate.next());
	}

	@Secured("IS_AUTHENTICATED")
	public void deleteConnectionTemplate(String id) throws Xdi2ClientException {
		Assert.hasLength(id);

		CloudUser user = (CloudUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		MessageEnvelope messageEnvelope = new MessageEnvelope();
		MessageCollection messageCollection = messageEnvelope.getMessageCollection(user.getCloudNumber().getXDIAddress(), true);
		Message message = messageCollection.createMessage();
		message = user.prepareMessageToCloud(message);

		String xdiAddress = Connection.convertIdToXdiAddress(id);
		message.createDelOperation(XDIAddress.create(xdiAddress));

		user.getXdiClient().send(messageEnvelope);

	}

	public SelfCloudStatus getCloudStatus() throws Xdi2ClientException {
		CloudUser user = (CloudUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		
		SelfCloudStatus status = new SelfCloudStatus(user.getCloudName());

		status.setCloudNumber(this.getCloudNumber());
		status.setCloudNames(this.getCloudNames());
		status.setDependents(this.getDependents());
		status.setGuardians(this.getGuardians());

		return status;
	}

	private String[] getKeyPair(String keyType) throws Xdi2ClientException {
		CloudUser user = (CloudUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		MessageEnvelope messageEnvelope = new MessageEnvelope();
		MessageCollection messageCollection = messageEnvelope.getMessageCollection(user.getCloudNumber().getXDIAddress(), true);
		Message message = messageCollection.createMessage();
		message = user.prepareMessageToCloud(message);

		String subsegment = null;		
		if ("ENCRYPT".equals(keyType)) {
			message.createGetOperation(XDIAddressUtil.concatXDIAddresses(user.getCloudNumber().getXDIAddress(), XDISecurityConstants.XDI_ADD_MSG_ENCRYPT_KEYPAIR));
			subsegment = "$msg$encrypt$keypair";
		}
		else {
			message.createGetOperation(XDIAddressUtil.concatXDIAddresses(user.getCloudNumber().getXDIAddress(), XDISecurityConstants.XDI_ADD_MSG_SIG_KEYPAIR));
			subsegment = "$msg$sig$keypair";
		}

		MessagingResponse messagingResponse = user.getXdiClient().send(messageEnvelope);

		String[] keyPair = new String[2];

		LiteralNode l = messagingResponse.getResultGraph().getRootContextNode().getDeepLiteralNode(XDIAddressUtil.concatXDIAddresses(user.getCloudNumber().getXDIAddress(), XDIAddress.create(subsegment + "<$public><$key>")));
		keyPair[0] = l != null ? l.getLiteralDataString() : null;		

		l = messagingResponse.getResultGraph().getRootContextNode().getDeepLiteralNode(XDIAddressUtil.concatXDIAddresses(user.getCloudNumber().getXDIAddress(), XDIAddress.create(subsegment + "<$private><$key>")));
		keyPair[1] = l != null ? l.getLiteralDataString() : null;

		return keyPair;
	}

	private void generateNewKeyPair(String keyType) throws Xdi2ClientException {
		CloudUser user = (CloudUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		MessageEnvelope messageEnvelope = new MessageEnvelope();
		MessageCollection messageCollection = messageEnvelope.getMessageCollection(user.getCloudNumber().getXDIAddress(), true);
		Message message = messageCollection.createMessage();
		message = user.prepareMessageToCloud(message);

		if ("ENCRYPT".equals(keyType)) {
			message.createOperation(GenerateKeyContributor.XDI_ADD_DO_KEYPAIR, XDIStatement.fromComponents(XDIAddressUtil.concatXDIAddresses(user.getCloudNumber().getXDIAddress(), XDISecurityConstants.XDI_ADD_MSG_ENCRYPT_KEYPAIR), XDIDictionaryConstants.XDI_ADD_IS_TYPE, XDIAddress.create("$rsa$2048")));
		}
		else {
			message.createOperation(GenerateKeyContributor.XDI_ADD_DO_KEYPAIR, XDIStatement.fromComponents(XDIAddressUtil.concatXDIAddresses(user.getCloudNumber().getXDIAddress(), XDISecurityConstants.XDI_ADD_MSG_SIG_KEYPAIR), XDIDictionaryConstants.XDI_ADD_IS_TYPE, XDIAddress.create("$rsa$2048")));
		}

		user.getXdiClient().send(messageEnvelope);	
	}

	private void deleteKeyPair(String keyType) throws Xdi2ClientException {
		CloudUser user = (CloudUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		MessageEnvelope messageEnvelope = new MessageEnvelope();
		MessageCollection messageCollection = messageEnvelope.getMessageCollection(user.getCloudNumber().getXDIAddress(), true);
		Message message = messageCollection.createMessage();
		message = user.prepareMessageToCloud(message);

		if ("ENCRYPT".equals(keyType)) {
			message.createDelOperation(XDIAddressUtil.concatXDIAddresses(user.getCloudNumber().getXDIAddress(), XDISecurityConstants.XDI_ADD_MSG_ENCRYPT_KEYPAIR));			
		}
		else {			
			message.createDelOperation(XDIAddressUtil.concatXDIAddresses(user.getCloudNumber().getXDIAddress(), XDISecurityConstants.XDI_ADD_MSG_SIG_KEYPAIR));
		}

		user.getXdiClient().send(messageEnvelope);	
	}

	public String[] getSignatureKeyPair() throws Xdi2ClientException {
		return this.getKeyPair("SIG");
	}

	public String[] getEncryptKeyPair() throws Xdi2ClientException {
		return this.getKeyPair("ENCRYPT");
	}

	public void generateSignatureNewKeyPair() throws Xdi2ClientException {
		this.generateNewKeyPair("SIG");
	}

	public void generateEncryptNewKeyPair() throws Xdi2ClientException {
		this.generateNewKeyPair("ENCRYPT");
	}

	public void deleteSignatureKeyPair() throws Xdi2ClientException {
		this.deleteKeyPair("SIG");
	}

	public void deleteEncryptKeyPair() throws Xdi2ClientException {
		this.deleteKeyPair("ENCRYPT");
	}




}
