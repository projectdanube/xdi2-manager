package xdi2.manager.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.Closure;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.map.MultiValueMap;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import xdi2.core.Graph;
import xdi2.core.LiteralNode;
import xdi2.core.constants.XDILinkContractConstants;
import xdi2.core.features.linkcontracts.LinkContractBase;
import xdi2.core.features.linkcontracts.instance.GenericLinkContract;
import xdi2.core.features.linkcontracts.instance.LinkContract;
import xdi2.core.features.linkcontracts.instance.PublicLinkContract;
import xdi2.core.features.linkcontracts.instance.RootLinkContract;
import xdi2.core.features.linkcontracts.template.LinkContractTemplate;
import xdi2.core.features.policy.PolicyAnd;
import xdi2.core.features.policy.PolicyUtil;
import xdi2.core.impl.memory.MemoryGraphFactory;
import xdi2.core.syntax.CloudNumber;
import xdi2.core.syntax.XDIAddress;
import xdi2.core.syntax.XDIStatement;
import xdi2.core.util.CopyUtil;
import xdi2.core.util.XDIAddressUtil;
import xdi2.core.util.iterators.IterableIterator;
import xdi2.manager.model.AbstractConnection;
import xdi2.manager.model.CloudUser;
import xdi2.manager.model.Connection;
import xdi2.manager.model.ConnectionTemplate;
import xdi2.manager.model.PersonalProfile;
import xdi2.manager.model.PostalAddress;
import xdi2.manager.service.ReverseNameResolutionService;
import xdi2.messaging.Message;

@Component
public class XdiModelConverter {
	private static final Logger log = LoggerFactory.getLogger(XdiModelConverter.class);

	public static XDIAddress XDI_FIRST_NAME = XDIAddress.create("<#first><#name>&");
	public static XDIAddress XDI_LAST_NAME = XDIAddress.create("<#last><#name>&");
	public static XDIAddress XDI_NICKNAME = XDIAddress.create("<#nickname>&");
	public static XDIAddress XDI_GENDER = XDIAddress.create("<#gender>&");
	public static XDIAddress XDI_BIRTH_DATE = XDIAddress.create("<#birth><#date>&");
	public static XDIAddress XDI_NATIONALITY = XDIAddress.create("<#nationality>&");
	public static XDIAddress XDI_PHONE = XDIAddress.create("<#phone>&");
	public static XDIAddress XDI_MOBILE_PHONE = XDIAddress.create("<#mobile><#phone>&");
	public static XDIAddress XDI_WORK_PHONE = XDIAddress.create("<#work><#phone>&");
	public static XDIAddress XDI_EMAIL = XDIAddress.create("<#email>&");
	public static XDIAddress XDI_WEBSITE = XDIAddress.create("<#website>&");
	public static XDIAddress XDI_ADDRESS_STREET = XDIAddress.create("#address<#street>&");
	public static XDIAddress XDI_ADDRESS_COUNTRY = XDIAddress.create("#address<#country>&");
	public static XDIAddress XDI_ADDRESS_LOCALITY = XDIAddress.create("#address<#locality>&");
	public static XDIAddress XDI_ADDRESS_POSTAL_CODE = XDIAddress.create("#address<#postal><#code>&");
	public static XDIAddress XDI_ADDRESS_REGION = XDIAddress.create("#address<#region>&");

	
    public static final Map<String, XDIAddress> XDI_PROFILE;
    static {
    	Map<String, XDIAddress> xdiProfile = new HashMap<String, XDIAddress>();
    	xdiProfile.put("firstName", XDI_FIRST_NAME);
    	xdiProfile.put("lastName", XDI_LAST_NAME);
    	xdiProfile.put("nickname", XDI_NICKNAME);
    	xdiProfile.put("gender", XDI_GENDER);
    	xdiProfile.put("birthDate", XDI_BIRTH_DATE);
    	xdiProfile.put("nationality", XDI_NATIONALITY);
    	xdiProfile.put("phone", XDI_PHONE);
    	xdiProfile.put("mobilePhone", XDI_MOBILE_PHONE);
    	xdiProfile.put("workPhone", XDI_WORK_PHONE);
    	xdiProfile.put("email", XDI_EMAIL);
    	xdiProfile.put("website", XDI_WEBSITE);
    	xdiProfile.put("address_street", XDI_ADDRESS_STREET);
    	xdiProfile.put("address_postalCode", XDI_ADDRESS_POSTAL_CODE);
    	xdiProfile.put("address_locality", XDI_ADDRESS_LOCALITY);
    	xdiProfile.put("address_region", XDI_ADDRESS_REGION);
    	xdiProfile.put("address_country", XDI_ADDRESS_COUNTRY);
    	XDI_PROFILE = Collections.unmodifiableMap(xdiProfile);
    }
	
	
	@Autowired
	private ReverseNameResolutionService reverseNameResolutionService;


	public PersonalProfile convertToPersonalProfile(Graph graph) {
		CloudUser user = (CloudUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		PersonalProfile profile = new PersonalProfile();

		profile.setXdiData(graph.toString("XDI DISPLAY", null));

		profile.setFirstName(getStringLiteralForSegment(graph, user.getCloudNumber(), XDI_FIRST_NAME));
		profile.setLastName(getStringLiteralForSegment(graph, user.getCloudNumber(), XDI_LAST_NAME));
		profile.setNickname(getStringLiteralForSegment(graph, user.getCloudNumber(), XDI_NICKNAME));
		profile.setGender(getStringLiteralForSegment(graph, user.getCloudNumber(), XDI_GENDER));
		profile.setBirthDate(getStringLiteralForSegment(graph, user.getCloudNumber(), XDI_BIRTH_DATE));
		profile.setNationality(getStringLiteralForSegment(graph, user.getCloudNumber(), XDI_NATIONALITY));
		profile.setPhone(getStringLiteralForSegment(graph, user.getCloudNumber(), XDI_PHONE));
		profile.setMobilePhone(getStringLiteralForSegment(graph, user.getCloudNumber(), XDI_MOBILE_PHONE));
		profile.setWorkPhone(getStringLiteralForSegment(graph, user.getCloudNumber(), XDI_WORK_PHONE));
		profile.setEmail(getStringLiteralForSegment(graph, user.getCloudNumber(), XDI_EMAIL));
		profile.setWebsite(getStringLiteralForSegment(graph, user.getCloudNumber(), XDI_WEBSITE));

		PostalAddress address = new PostalAddress();

		address.setStreet(getStringLiteralForSegment(graph, user.getCloudNumber(), XDI_ADDRESS_STREET));
		address.setCountry(getStringLiteralForSegment(graph, user.getCloudNumber(), XDI_ADDRESS_COUNTRY));
		address.setLocality(getStringLiteralForSegment(graph, user.getCloudNumber(), XDI_ADDRESS_LOCALITY));
		address.setPostalCode(getStringLiteralForSegment(graph, user.getCloudNumber(), XDI_ADDRESS_POSTAL_CODE));
		address.setRegion(getStringLiteralForSegment(graph, user.getCloudNumber(), XDI_ADDRESS_REGION));

		profile.setAddress(address);

		return profile;
	}

	public List<XDIStatement> convertPersonalProfileToStatements(PersonalProfile profile) {
		Assert.notNull(profile);

		CloudUser user = (CloudUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		List<XDIStatement> statements = new ArrayList<XDIStatement>();
		addStatementIfNotNull(statements, user.getCloudNumber().toString() + XDI_FIRST_NAME + "/&/", profile.getFirstName());
		addStatementIfNotNull(statements, user.getCloudNumber().toString() + XDI_LAST_NAME + "/&/", profile.getLastName());
		addStatementIfNotNull(statements, user.getCloudNumber().toString() + XDI_NICKNAME + "/&/", profile.getNickname());
		addStatementIfNotNull(statements, user.getCloudNumber().toString() + XDI_GENDER + "/&/", profile.getGender());
		addStatementIfNotNull(statements, user.getCloudNumber().toString() + XDI_BIRTH_DATE + "/&/", profile.getBirthDate());
		addStatementIfNotNull(statements, user.getCloudNumber().toString() + XDI_NATIONALITY + "/&/", profile.getNationality());
		addStatementIfNotNull(statements, user.getCloudNumber().toString() + XDI_PHONE + "/&/", profile.getPhone());
		addStatementIfNotNull(statements, user.getCloudNumber().toString() + XDI_MOBILE_PHONE + "/&/", profile.getMobilePhone());
		addStatementIfNotNull(statements, user.getCloudNumber().toString() + XDI_WORK_PHONE + "/&/", profile.getWorkPhone());
		addStatementIfNotNull(statements, user.getCloudNumber().toString() + XDI_EMAIL + "/&/", profile.getEmail());
		addStatementIfNotNull(statements, user.getCloudNumber().toString() + XDI_WEBSITE + "/&/", profile.getWebsite());


		addStatementIfNotNull(statements, user.getCloudNumber().toString() + XDI_ADDRESS_STREET + "/&/", profile.getAddress().getStreet());
		addStatementIfNotNull(statements, user.getCloudNumber().toString() + XDI_ADDRESS_COUNTRY + "/&/", profile.getAddress().getCountry());
		addStatementIfNotNull(statements, user.getCloudNumber().toString() + XDI_ADDRESS_LOCALITY + "/&/", profile.getAddress().getLocality());
		addStatementIfNotNull(statements, user.getCloudNumber().toString() + XDI_ADDRESS_POSTAL_CODE + "/&/", profile.getAddress().getPostalCode());
		addStatementIfNotNull(statements, user.getCloudNumber().toString() + XDI_ADDRESS_REGION + "/&/", profile.getAddress().getRegion());

		return statements;
	}

	public void prepareMessageForPersonalProfile(Message message) {
		Assert.notNull(message);

		CloudUser user = (CloudUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		message.createGetOperation(XDIAddress.create(user.getCloudNumber().toString() + XDI_FIRST_NAME));
		message.createGetOperation(XDIAddress.create(user.getCloudNumber().toString() + XDI_LAST_NAME));
		message.createGetOperation(XDIAddress.create(user.getCloudNumber().toString() + XDI_NICKNAME));
		message.createGetOperation(XDIAddress.create(user.getCloudNumber().toString() + XDI_GENDER));
		message.createGetOperation(XDIAddress.create(user.getCloudNumber().toString() + XDI_BIRTH_DATE));
		message.createGetOperation(XDIAddress.create(user.getCloudNumber().toString() + XDI_NATIONALITY ));
		message.createGetOperation(XDIAddress.create(user.getCloudNumber().toString() + XDI_PHONE));
		message.createGetOperation(XDIAddress.create(user.getCloudNumber().toString() + XDI_MOBILE_PHONE));
		message.createGetOperation(XDIAddress.create(user.getCloudNumber().toString() + XDI_WORK_PHONE ));
		message.createGetOperation(XDIAddress.create(user.getCloudNumber().toString() + XDI_EMAIL));
		message.createGetOperation(XDIAddress.create(user.getCloudNumber().toString() + XDI_WEBSITE));
		message.createGetOperation(XDIAddress.create(user.getCloudNumber().toString() + XDI_ADDRESS_STREET ));
		message.createGetOperation(XDIAddress.create(user.getCloudNumber().toString() + XDI_ADDRESS_COUNTRY));
		message.createGetOperation(XDIAddress.create(user.getCloudNumber().toString() + XDI_ADDRESS_LOCALITY));
		message.createGetOperation(XDIAddress.create(user.getCloudNumber().toString() + XDI_ADDRESS_POSTAL_CODE));
		message.createGetOperation(XDIAddress.create(user.getCloudNumber().toString() + XDI_ADDRESS_REGION));

	}

	private static String getStringLiteralForSegment(Graph graph, CloudNumber cloudNumber, XDIAddress segment) {

		LiteralNode l = graph.getRootContextNode().getDeepLiteralNode(XDIAddressUtil.concatXDIAddresses(cloudNumber.getXDIAddress(), segment));
		return l != null ? l.getLiteralDataString() : null;
	}

	private static void addStatementIfNotNull(List<XDIStatement> statements, String segment, String value) {
		if(StringUtils.isNotEmpty(value)) {
			statements.add(XDIStatement.create(segment + "\"" + value + "\""));
		}
		else {
			statements.add(XDIStatement.create(segment + "null"));
		}
	}

	private AbstractConnection convertLCBaseToAbstractConnection(LinkContractBase<?> linkContract, AbstractConnection connection) {
		Assert.notNull(linkContract);
		Assert.notNull(connection);

		// isolate the LC part of the graph
		Graph tempGraph = MemoryGraphFactory.getInstance().openGraph();
		CopyUtil.copyContextNode(linkContract.getContextNode(), tempGraph, null);
		connection.setXdi(tempGraph.toString("XDI DISPLAY", null));

		connection.setXdiAddress(linkContract.getContextNode().getXDIAddress().toString());

		// Permissions
		IterableIterator<XDIAddress> permissions = linkContract.getPermissionTargetXDIAddresses(XDILinkContractConstants.XDI_ADD_GET);
		CollectionUtils.forAllDo(permissions.iterator(), new PermissionsClosure<XDIAddress>(connection, "get"));

		permissions = linkContract.getPermissionTargetXDIAddresses(XDILinkContractConstants.XDI_ADD_SET);
		CollectionUtils.forAllDo(permissions.iterator(), new PermissionsClosure<XDIAddress>(connection, "set"));

		permissions = linkContract.getPermissionTargetXDIAddresses(XDILinkContractConstants.XDI_ADD_DEL);
		CollectionUtils.forAllDo(permissions.iterator(), new PermissionsClosure<XDIAddress>(connection, "del"));

		permissions = linkContract.getPermissionTargetXDIAddresses(XDILinkContractConstants.XDI_ADD_ALL);
		CollectionUtils.forAllDo(permissions.iterator(), new PermissionsClosure<XDIAddress>(connection, "all"));

		IterableIterator<XDIStatement> permissionStatements = linkContract.getPermissionTargetXDIStatements(XDILinkContractConstants.XDI_ADD_GET);
		CollectionUtils.forAllDo(permissionStatements.iterator(), new PermissionsClosure<XDIStatement>(connection, "get"));

		permissionStatements = linkContract.getPermissionTargetXDIStatements(XDILinkContractConstants.XDI_ADD_SET);
		CollectionUtils.forAllDo(permissionStatements.iterator(), new PermissionsClosure<XDIStatement>(connection, "set"));

		permissionStatements = linkContract.getPermissionTargetXDIStatements(XDILinkContractConstants.XDI_ADD_DEL);
		CollectionUtils.forAllDo(permissionStatements.iterator(), new PermissionsClosure<XDIStatement>(connection, "del"));

		permissionStatements = linkContract.getPermissionTargetXDIStatements(XDILinkContractConstants.XDI_ADD_ALL);
		CollectionUtils.forAllDo(permissionStatements.iterator(), new PermissionsClosure<XDIStatement>(connection, "all"));

		return connection;
	}

	public Connection convertLinkContractToConnection(LinkContract linkContract) {
		Assert.notNull(linkContract);

		CloudUser user = (CloudUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		Connection connection = new Connection();
		connection = (Connection) this.convertLCBaseToAbstractConnection(linkContract, connection);

		if (linkContract instanceof RootLinkContract) {
			connection.setType("ROOT");
		}
		else if (linkContract instanceof PublicLinkContract) {
			connection.setType("PUBLIC");
		}
		else if (linkContract instanceof GenericLinkContract) {
			connection.setType("GENERIC");

			GenericLinkContract glc = (GenericLinkContract) linkContract;

			connection.setRaCloudNumber(glc.getRequestingAuthority().toString());
			connection.setRaCloudName(reverseNameResolutionService.getCloudName(connection.getRaCloudNumber()));
		}

		return connection;
	}

	public ConnectionTemplate convertLinkContractTemplateToConnectionTemplate(LinkContractTemplate linkContractTemplate) {
		Assert.notNull(linkContractTemplate);

		ConnectionTemplate connectionTemplate = new ConnectionTemplate();

		connectionTemplate = (ConnectionTemplate) this.convertLCBaseToAbstractConnection(linkContractTemplate, connectionTemplate);

		String tag = StringUtils.substringAfter(connectionTemplate.getXdiAddress(), "#");
		tag = StringUtils.remove(tag, linkContractTemplate.getXdiVariable().getBaseXDIArc().toString());
		connectionTemplate.setTag(tag);

		LiteralNode l = linkContractTemplate.getContextNode().getDeepLiteralNode(XDIAddress.create(connectionTemplate.getXdiAddress() + "<#description>&"));
		connectionTemplate.setDescription(l != null ? l.getLiteralDataString() : null);

		return connectionTemplate;
	}

	private LinkContractBase<?> convertAbstractConnectionToLinkContractBase(AbstractConnection connection, LinkContractBase<?> linkContractBase) {
		Assert.notNull(connection);

		if (connection.getPermissions().size() <= 0) {
			throw new RuntimeException("Something has to be shared.");
		}

		MultiValueMap<String, String> permissions = connection.getPermissions();

		for (String targetXDIAddress : permissions.keySet()) {

			// there is some json serialization issue and instead of String I'm getting ArrayList<String>
			Object listTemp = permissions.iterator(targetXDIAddress).next();
			@SuppressWarnings("unchecked")
			List<String> list = (List<String>) listTemp;

			for (String p : list) {
				XDIAddress permissionAddress = null;
				if ("get".equals(p))  permissionAddress = XDILinkContractConstants.XDI_ADD_GET;
				else if ("set".equals(p))  permissionAddress = XDILinkContractConstants.XDI_ADD_SET;
				else if ("del".equals(p))  permissionAddress = XDILinkContractConstants.XDI_ADD_DEL;
				else if ("all".equals(p))  permissionAddress = XDILinkContractConstants.XDI_ADD_ALL;
				else continue;

				linkContractBase.setPermissionTargetXDIAddress(permissionAddress, XDIAddress.create(targetXDIAddress));
			}
		}

		return linkContractBase;
	}

	public LinkContract convertConnectionToLinkContract (Connection connection) {
		Assert.notNull(connection);

		if (StringUtils.isEmpty(connection.getRaCloudNumber())) {
			throw new RuntimeException("Invalid Cloud Name.");
		}

		CloudUser user = (CloudUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		Graph g = MemoryGraphFactory.getInstance().openGraph();
		LinkContract l = GenericLinkContract.findGenericLinkContract(g, XDIAddress.create(user.getCloudNumber().toString()), XDIAddress.create(connection.getRaCloudNumber()), null, true);

		l = (LinkContract) this.convertAbstractConnectionToLinkContractBase(connection, l);

		PolicyAnd policyAnd = l.getPolicyRoot(true).createAndPolicy(true);
		PolicyUtil.createSenderIsOperator(policyAnd, XDIAddress.create(connection.getRaCloudNumber()));

		if (connection.getRequireSecretToken()) PolicyUtil.createSecretTokenValidOperator(policyAnd);
		if (connection.getRequireSignature()) PolicyUtil.createSignatureValidOperator(policyAnd);

		return l;
	}

	public LinkContractTemplate convertConnectionTemplateToLinkContractTemplate (ConnectionTemplate connectionTemplate) {
		Assert.notNull(connectionTemplate);

		CloudUser user = (CloudUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		Graph g = MemoryGraphFactory.getInstance().openGraph();
		LinkContractTemplate lct = LinkContractTemplate.findLinkContractTemplate(g, XDIAddress.create(user.getCloudNumber() + "#" + connectionTemplate.getTag()), true);

		lct = (LinkContractTemplate) this.convertAbstractConnectionToLinkContractBase(connectionTemplate, lct);
		
		
		if (StringUtils.isNotEmpty(connectionTemplate.getDescription())) {
//			String xdiAddress = lct.getContextNode().getXDIAddress().toString();
//			XDIStatement description = XDIStatement.create(xdiAddress + "&/&/" + connectionTemplate.getDescription());
//			CopyUtil.copyStatement(description, lct.getContextNode().getGraph(), null);
			
			log.debug("skipping description...");
		}
		

		PolicyAnd policyAnd = lct.getPolicyRoot(true).createAndPolicy(true);
		PolicyUtil.createSenderIsOperator(policyAnd, user.getCloudNumber().getXDIAddress());

		if (connectionTemplate.getRequireSecretToken()) PolicyUtil.createSecretTokenValidOperator(policyAnd);
		if (connectionTemplate.getRequireSignature()) PolicyUtil.createSignatureValidOperator(policyAnd);

		return lct;
	}



	private static class PermissionsClosure<T> implements Closure<T> {
		private AbstractConnection connection;
		private String permission;

		public PermissionsClosure (AbstractConnection connection, String permission) {
			this.connection = connection;
			this.permission = permission;
		}

		@Override
		public void execute(T input) {
			this.connection.addPermission(input.toString(), this.permission);
		}
	}

}
