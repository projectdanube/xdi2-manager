package xdi2.manager.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import xdi2.client.exceptions.Xdi2ClientException;
import xdi2.connector.facebook.mapping.FacebookMapping;
import xdi2.core.ContextNode;
import xdi2.core.Graph;
import xdi2.core.Literal;
import xdi2.core.Relation;
import xdi2.core.features.nodetypes.XdiEntityCollection;
import xdi2.core.features.nodetypes.XdiEntityMember;
import xdi2.core.features.nodetypes.XdiEntityMemberUnordered;
import xdi2.core.syntax.XDIAddress;
import xdi2.core.syntax.XDIStatement;
import xdi2.core.util.XDIAddressUtil;
import xdi2.core.util.XDIStatementUtil;
import xdi2.core.util.iterators.ReadOnlyIterator;
import xdi2.manager.model.Card;
import xdi2.manager.model.CardField;
import xdi2.manager.model.CardFieldLink;
import xdi2.manager.model.CardFieldPrivacy;
import xdi2.manager.model.CloudUser;
import xdi2.manager.service.CloudService;
import xdi2.manager.service.Configuration;
import xdi2.manager.service.FacebookService;

@Component
public class CardXdiModelConverter {
	private static final Logger log = LoggerFactory.getLogger(CardXdiModelConverter.class);
	
	@Autowired
	private CloudService cloudService;
	
	@Autowired
	private Configuration configuration;

	public static XDIAddress XDI_CARDS = XDIAddress.create("[$card]");
	public static XDIAddress XDI_CARD_PUBLIC = XDIAddress.create("$public");
	public static XDIAddress XDI_CARD_PRIVATE = XDIAddress.create("$private");
	public static XDIAddress XDI_CARD_DEFAULT = XDIAddress.create("$card");

	public static XDIAddress XDI_CARD_DESCRIPTION = XDIAddress.create("$public<#description>&");
	public static XDIAddress XDI_CARD_TAG = XDIAddress.create("$public<#tag>&");
	public static XDIAddress XDI_CARD_CONNECT_BUTTON = XDIAddress.create("$public<#connect><#button>&");
	public static XDIAddress XDI_CARD_BACKGROUND_IMAGE = XDIAddress.create("$public<#background><#image>&");
	
	private static CardField DUMMY_CARD_FIELD = new CardField(null, null, CardFieldPrivacy.ONLY_ME);


	public List<Card> convertXdiToCards(Graph graph) {
		Assert.notNull(graph);
		CloudUser user = (CloudUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		List<Card> cards = new ArrayList<Card>();
		
		ContextNode contextNode = graph.getDeepContextNode(XDIAddressUtil.concatXDIAddresses(user.getCloudNumber().getXDIAddress(), XDI_CARDS));
		if (contextNode == null) {
			return cards;
		}
		XdiEntityCollection cardCollection = XdiEntityCollection.fromContextNode(contextNode);
		if (cardCollection == null) {
			return cards;
		}
		long start = System.currentTimeMillis();
		ReadOnlyIterator<XdiEntityMember> iCards = cardCollection.getXdiMembers();

		while (iCards.hasNext()) {
			XdiEntityMember cardXdi = iCards.next();

			cards.add(convertXdiToCard(cardXdi));
		}
		log.debug("Took " + (System.currentTimeMillis()-start) + "ms to convert " + cards.size() + " cards");
		return cards;
	}

	public Card convertXdiToCard(XdiEntityMember cardXdi) {
		Assert.notNull(cardXdi);

		Card card = new Card();

		// get general card info
		card.setXdiAddress(cardXdi.getXDIAddress().toString());

		Literal l = cardXdi.getGraph().getRootContextNode().getDeepLiteral(XDIAddressUtil.concatXDIAddresses(cardXdi.getXDIAddress(), XDI_CARD_DESCRIPTION));
		if (l != null) {
			card.setDescription(l.getLiteralDataString());
		}
		l = cardXdi.getGraph().getRootContextNode().getDeepLiteral(XDIAddressUtil.concatXDIAddresses(cardXdi.getXDIAddress(), XDI_CARD_TAG));
		if (l != null) {
			card.setTag(l.getLiteralDataString());
		}
		l = cardXdi.getGraph().getRootContextNode().getDeepLiteral(XDIAddressUtil.concatXDIAddresses(cardXdi.getXDIAddress(), XDI_CARD_CONNECT_BUTTON));
		if (l != null) {
			card.setMessageConnectButton(l.getLiteralDataString());
		}
		l = cardXdi.getGraph().getRootContextNode().getDeepLiteral(XDIAddressUtil.concatXDIAddresses(cardXdi.getXDIAddress(), XDI_CARD_BACKGROUND_IMAGE));
		if (l != null) {
			card.setBackgroundImage(l.getLiteralDataString());
		}

		// get profile info
		for (String key : XdiModelConverter.XDI_PROFILE.keySet()) {			
			card.putField(key, convertXdiToCardField(cardXdi, XdiModelConverter.XDI_PROFILE.get(key)));
		}

		return card;
	}

	private CardField convertXdiToCardField (XdiEntityMember cardXdi, XDIAddress fieldXdi) {
		Assert.notNull(cardXdi);
		Assert.notNull(fieldXdi);

		CardField field = new CardField();		

		XDIAddress fieldXdiAddress = XDIAddressUtil.concatXDIAddresses(cardXdi.getXDIAddress(), XDI_CARD_PUBLIC, fieldXdi);
		ContextNode fieldContextNode = cardXdi.getGraph().getRootContextNode().getDeepContextNode(fieldXdiAddress);
		field.setPrivacy(CardFieldPrivacy.PUBLIC);
		if (fieldContextNode == null) {
			fieldXdiAddress = XDIAddressUtil.concatXDIAddresses(cardXdi.getXDIAddress(), XDI_CARD_PRIVATE, fieldXdi);
			fieldContextNode = cardXdi.getGraph().getRootContextNode().getDeepContextNode(fieldXdiAddress);
			field.setPrivacy(CardFieldPrivacy.PRIVATE);
			if (fieldContextNode == null) {
				// field doesnt exist, returning dummy...
				return DUMMY_CARD_FIELD;
			}
		}

		Literal l = fieldContextNode.getLiteral();
		if (l != null) {
			field.setValue(l.getLiteralDataString());
			field.setXdiStatement(l.getStatement().toString());
		}
		else {
			Relation ref = fieldContextNode.getRelation(XDIAddress.create("$ref"));
			if (ref != null) {				
				String refTarget = ref.toString().substring(ref.toString().indexOf("$ref/") + 5);
				field.setLinked(refTarget.startsWith(FacebookMapping.XDI_ADD_FACEBOOK_CONTEXT.toString()) ? CardFieldLink.FACEBOOK : CardFieldLink.PROFILE);
				field.setXdiStatement(ref.toString());
				field.setValue(refTarget);
			}
			else {
				log.debug("Address is not literal neither $ref, What is it? " + fieldContextNode.getGraph().toString("XDI DISPLAY", null));
				return DUMMY_CARD_FIELD;
			}
		}

		return field;
	}

	public List<XDIStatement> convertCardToXdi (Card card) throws Xdi2ClientException {
		Assert.notNull(card);
		CloudUser user = (CloudUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		XDIAddress cardXdiAddress = null;		
		if (StringUtils.isNotBlank(card.getXdiAddress())) {
			cardXdiAddress = XDIAddress.create(card.getXdiAddress());
		}
		else {
			XDIAddress cardsCollectionXdiAddress = XDIAddressUtil.concatXDIAddresses(user.getCloudNumber().getXDIAddress(), XDI_CARDS);
			cardXdiAddress = XDIAddressUtil.concatXDIAddresses(cardsCollectionXdiAddress, XdiEntityMemberUnordered.createRandomUuidXDIArc(XdiEntityCollection.class));
		}

		List<XDIStatement> statements = new ArrayList<XDIStatement>();

		addStatement(statements, XDIAddressUtil.concatXDIAddresses(cardXdiAddress, XDI_CARD_DESCRIPTION), card.getDescription());
		addStatement(statements, XDIAddressUtil.concatXDIAddresses(cardXdiAddress, XDI_CARD_TAG), card.getTag());
		
		addStatement(statements, XDIAddressUtil.concatXDIAddresses(cardXdiAddress, XDI_CARD_BACKGROUND_IMAGE), card.getBackgroundImage());
		
		addCardButtonMessages(statements, cardXdiAddress);
		
		for (String key : card.getFields().keySet()) {
			addStatement(statements, cardXdiAddress, card, key);
		}
		
		// Put card's public info in Public LC
		statements.addAll(getCardInPublicLCStatement(cardXdiAddress, card));
		
		addCardShortcut(statements, cardXdiAddress, card);
		
		if (card.isDefault()) {
			statements.add(XDIStatement.create(user.getCloudNumber() + "$card/$ref/" + cardXdiAddress));
		}

		return statements;
	}

	private void addStatement (List<XDIStatement> statements, XDIAddress fieldXdi, String value) {
		if (StringUtils.isBlank(value)) {
			return;
		}
		statements.add(XDIStatementUtil.concatXDIStatement(fieldXdi, XDIStatement.create("/&/" + "\"" + value + "\"")));
	}

	private void addStatement (List<XDIStatement> statements, XDIAddress cardXdiAddress, Card card, String key) {
		CloudUser user = (CloudUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		CardField cardField = card.getField(key);
		if (cardField == null) {
			return;
		}

		XDIAddress cardFieldXdiAddress = null;
		switch (cardField.getPrivacy()) {
		case PRIVATE:
			cardFieldXdiAddress = XDIAddressUtil.concatXDIAddresses(cardXdiAddress, XDI_CARD_PRIVATE, XdiModelConverter.XDI_PROFILE.get(key));
			break;
		case PUBLIC:
			cardFieldXdiAddress = XDIAddressUtil.concatXDIAddresses(cardXdiAddress, XDI_CARD_PUBLIC, XdiModelConverter.XDI_PROFILE.get(key));
			break;
		case ONLY_ME:
		default:
			return;	
		}

		XDIStatement statement = null;
		if (CardFieldLink.PROFILE.equals(cardField.getLinked())) {
			XDIAddress profileFieldXdiAddress = XDIAddressUtil.concatXDIAddresses(user.getCloudNumber().getXDIAddress(), XdiModelConverter.XDI_PROFILE.get(key));
			statement = XDIStatementUtil.concatXDIStatement(cardFieldXdiAddress, XDIStatement.create("/$ref/" + profileFieldXdiAddress.toString()));
		}
		else if (CardFieldLink.FACEBOOK.equals(cardField.getLinked()))  {
			XDIAddress facebookFieldAddress = FacebookService.XDI_FACEBOOK_PROFILE.get(key);
			if (facebookFieldAddress == null) return; //ignore it

			statement = XDIStatementUtil.concatXDIStatement(cardFieldXdiAddress, XDIStatement.create("/$ref/" + cardField.getValue()));
		}
		else {
			statement = XDIStatementUtil.concatXDIStatement(cardFieldXdiAddress, XDIStatement.create("/&/\"" + cardField.getValue() + "\""));
		}

		statements.add(statement);
	}
	
	public List<XDIStatement> getCardInPublicLCStatement (XDIAddress cardXdiAddress, Card card) {
		Assert.notNull(cardXdiAddress);
		CloudUser user = (CloudUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		List<XDIStatement> statements = new ArrayList<XDIStatement>();
		statements.add(XDIStatement.create("(" + user.getCloudNumber() + "/$public)$do/$get/" + cardXdiAddress + XDI_CARD_PUBLIC));

		return statements;
	}
	
	private void addCardButtonMessages(List<XDIStatement> statements, XDIAddress cardXdiAddress) throws Xdi2ClientException {
		CloudUser user = (CloudUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		String signatureKeys[] = cloudService.getSignatureKeyPair();
		
		String cloudCardUrl = null;
		try {
			cloudCardUrl = configuration.getCloudCardAppUrl() + user.getEnvironment() + "/" + URLEncoder.encode(cardXdiAddress.toString(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			log.error("Unable to URL encode (" + cardXdiAddress.toString() + ") ");
		}

		String messageConnectXdiAddress = user.getCloudNumber() + "[$msg]@0";
		String messageConnect = messageConnectXdiAddress + "/$is()/({$to})\n";
		messageConnect += messageConnectXdiAddress + "/$do/({$to}/[+]!:uuid:ca51aeb9-e09e-4305-89d7-87a944a1e1fa)[+]!:uuid:ca51aeb9-e09e-4305-89d7-87a944a1e1fa#community$do\n";
		messageConnect += messageConnectXdiAddress + "/$is$do/(" + user.getCloudNumber() + "/[+]!:uuid:ca51aeb9-e09e-4305-89d7-87a944a1e1fa)[+]!:uuid:ca51aeb9-e09e-4305-89d7-87a944a1e1fa#community$do\n";
		messageConnect += messageConnectXdiAddress + "$do/$do$is{}/+danubeclouds#forever{$do}\n";
		messageConnect += messageConnectXdiAddress + "{$card}/$is/" + cardXdiAddress + "\n";
		messageConnect += messageConnectXdiAddress + "<#return><$uri>&/&/\"" + cloudCardUrl + "\"\n";

		String messageGetXdiAddress = user.getCloudNumber() + "[$msg]@1";
		messageConnect += messageGetXdiAddress + "/$is()/({$to})\n";
		messageConnect += messageGetXdiAddress + "/$do/({$to}/[+]!:uuid:ca51aeb9-e09e-4305-89d7-87a944a1e1fa)[+]!:uuid:ca51aeb9-e09e-4305-89d7-87a944a1e1fa#community$do\n";
		messageConnect += messageGetXdiAddress + "/$is$do/("+ user.getCloudNumber() +"/{$to})+danubeclouds#forever$do\n";
		messageConnect += messageGetXdiAddress + "$do/$get$is/" + cardXdiAddress + "\n";
		messageConnect += "(" + messageGetXdiAddress + "$do/$get$is)" + user.getCloudNumber() + "/$is$ref/{}\n";
		messageConnect += messageGetXdiAddress + "<#return><$uri>&/&/\"" + cloudCardUrl + "\"\n";
		messageConnect += messageGetXdiAddress + "$get<$deref>&/&/true\n";
		
		
		XDIAddress[] messagesXdiAddress = new XDIAddress[]{XDIAddress.create(messageConnectXdiAddress), XDIAddress.create(messageGetXdiAddress)};
		
		Graph signedMessage = XdiUtils.signMessage(messageConnect, messagesXdiAddress, signatureKeys[1]);
		log.debug("signed connect message:\n" + signedMessage.toString("XDI DISPLAY", null));
		addStatement(statements, XDIAddressUtil.concatXDIAddresses(cardXdiAddress, XDI_CARD_CONNECT_BUTTON), Base64.encodeBase64String(signedMessage.toString("XDI/JSON", null).getBytes()));

	}

	// Create shortcut $ref
	// [=]!:uuid:1111#{TAG}$card/$ref/[=]!:uuid:1111[$card]!:uuid:1234
	private void addCardShortcut(List<XDIStatement> statements, XDIAddress cardXdiAddress, Card card) {
		XDIStatement statement = XDIStatementUtil.concatXDIStatement(XDIAddress.create(getCardShortcutAddress(card) + "$card"), XDIStatement.create("/$ref/" + cardXdiAddress));
		
		statements.add(statement);
	}
	
	public XDIAddress getCardShortcutAddress (Card card) {
		CloudUser user = (CloudUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		return XDIAddress.create(user.getCloudNumber() + "#" + card.getTag());
	}

}
