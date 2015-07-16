package xdi2.manager.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import xdi2.client.exceptions.Xdi2ClientException;
import xdi2.core.Relation;
import xdi2.core.syntax.XDIAddress;
import xdi2.core.syntax.XDIStatement;
import xdi2.core.util.XDIAddressUtil;
import xdi2.manager.model.Card;
import xdi2.manager.model.CloudUser;
import xdi2.manager.util.CardXdiModelConverter;
import xdi2.messaging.Message;
import xdi2.messaging.MessageCollection;
import xdi2.messaging.MessageEnvelope;
import xdi2.messaging.operations.GetOperation;
import xdi2.messaging.response.MessagingResponse;

@Service
public class CardService {
	private static final Logger log = LoggerFactory.getLogger(CardService.class);

	@Autowired
	private CardXdiModelConverter cardXdiModelConverter;


	@Secured("IS_AUTHENTICATED")
	public List<Card> getCards() throws Xdi2ClientException {
		CloudUser user = (CloudUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		// Get cards
		MessageEnvelope messageEnvelope = new MessageEnvelope();
		MessageCollection messageCollection = messageEnvelope.getMessageCollection(user.getCloudNumber().getXDIAddress(), true);
		Message message = messageCollection.createMessage();
		message = user.prepareMessageToCloud(message);
		GetOperation operation = message.createGetOperation(XDIAddressUtil.concatXDIAddresses(user.getCloudNumber().getXDIAddress(), CardXdiModelConverter.XDI_CARDS));
		operation.setParameter(GetOperation.XDI_ADD_PARAMETER_DEREF, Boolean.TRUE);

		log.debug("getCard message:\n" + messageEnvelope.getGraph().toString("XDI DISPLAY", null));

		MessagingResponse messagingResponse = user.getXdiClient().send(messageEnvelope);

		System.out.println(messagingResponse.getResultGraph().toString("XDI DISPLAY", null));


		List<Card> cards = cardXdiModelConverter.convertXdiToCards(messagingResponse.getResultGraph());

		// Get default card
		XDIAddress defaultCardXdiAddress = getDefaultCardXdiAddress();

		if (defaultCardXdiAddress != null) {			
			for (Card c : cards) {
				if (c.getXdiAddress().equals(defaultCardXdiAddress.toString())) {
					c.setDefault(true);
				}
			}
		}

		return cards;
	}

	@Secured("IS_AUTHENTICATED")
	public Card getCard(XDIAddress cardXdiAddress, boolean edit) throws Xdi2ClientException {
		Assert.notNull(cardXdiAddress);

		CloudUser user = (CloudUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		MessageEnvelope messageEnvelope = new MessageEnvelope();
		MessageCollection messageCollection = messageEnvelope.getMessageCollection(user.getCloudNumber().getXDIAddress(), true);
		Message message = messageCollection.createMessage();
		message = user.prepareMessageToCloud(message);
		GetOperation operation = message.createGetOperation(cardXdiAddress);
		if (edit == false) operation.setParameter(GetOperation.XDI_ADD_PARAMETER_DEREF, Boolean.TRUE);

		log.debug("getCard message:\n" + messageEnvelope.getGraph().toString("XDI DISPLAY", null));

		MessagingResponse messagingResponse = user.getXdiClient().send(messageEnvelope);

		List<Card> cards = cardXdiModelConverter.convertXdiToCards(messagingResponse.getResultGraph());
		if (cards == null) return null;
		if (cards.size() > 1) {
			throw new RuntimeException("Got " + cards.size() + " cards for the address " + cardXdiAddress);
		}

		Card card = cards.size() == 1 ? cards.get(0) : null;
		if (card != null) {
			XDIAddress defaultCardXdiAddress = getDefaultCardXdiAddress();

			if (defaultCardXdiAddress != null && card.getXdiAddress().equals(defaultCardXdiAddress.toString()))
				card.setDefault(true);
		}

		return card;		
	}

	@Secured("IS_AUTHENTICATED")
	public void createCard(Card card) throws Xdi2ClientException {
		Assert.notNull(card);
		CloudUser user = (CloudUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		MessageEnvelope messageEnvelope = new MessageEnvelope();
		MessageCollection messageCollection = messageEnvelope.getMessageCollection(user.getCloudNumber().getXDIAddress(), true);
		Message message = messageCollection.createMessage();
		message = user.prepareMessageToCloud(message);

		message.createSetOperation(cardXdiModelConverter.convertCardToXdi(card).iterator());

		log.debug("createCard message:\n" + messageEnvelope.getGraph().toString("XDI DISPLAY", null));

		user.getXdiClient().send(messageEnvelope);
	}

	@Secured("IS_AUTHENTICATED")
	public void deleteCard(XDIAddress cardXdiAddress) throws Xdi2ClientException {
		Assert.notNull(cardXdiAddress);

		CloudUser user = (CloudUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Card card = getCard(cardXdiAddress, false);

		MessageEnvelope messageEnvelope = new MessageEnvelope();
		MessageCollection messageCollection = messageEnvelope.getMessageCollection(user.getCloudNumber().getXDIAddress(), true);
		Message message = messageCollection.createMessage();
		message = user.prepareMessageToCloud(message);

		// Delete card
		message.createDelOperation(XDIAddress.create(card.getXdiAddress()));

		// Delete card in Public LC
		message.createDelOperation(cardXdiModelConverter.getCardInPublicLCStatement(XDIAddress.create(card.getXdiAddress()), card).iterator());

		// Delete card shortcut
		message.createDelOperation(cardXdiModelConverter.getCardShortcutAddress(card));

		log.debug("deleteCard message:\n" + messageEnvelope.getGraph().toString("XDI DISPLAY", null));

		user.getXdiClient().send(messageEnvelope);
	}

	@Secured("IS_AUTHENTICATED")
	public void editCard(Card card) throws Xdi2ClientException {
		Assert.notNull(card);

		deleteCard(XDIAddress.create(card.getXdiAddress()));
		createCard(card);
	}

	@Secured("IS_AUTHENTICATED")
	public void setDefaultCard(XDIAddress cardXdiAddress) throws Xdi2ClientException {
		Assert.notNull(cardXdiAddress);

		CloudUser user = (CloudUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		MessageEnvelope messageEnvelope = new MessageEnvelope();
		MessageCollection messageCollection = messageEnvelope.getMessageCollection(user.getCloudNumber().getXDIAddress(), true);

		long messageIndex = 0;

		// if there is a default card, we must delete it first
		Message message = messageCollection.createMessage(messageIndex++);
		message = user.prepareMessageToCloud(message);
		message.createDelOperation(XDIStatement.create(user.getCloudNumber() + "$card/$ref/{}"));

		// create a new $ref
		message = messageCollection.createMessage(messageIndex);
		message = user.prepareMessageToCloud(message);

		message.createSetOperation(XDIStatement.create(user.getCloudNumber() + "$card/$ref/" + cardXdiAddress));

		log.debug("setDefaultCard message:\n" + messageEnvelope.getGraph().toString("XDI DISPLAY", null));

		user.getXdiClient().send(messageEnvelope);
	}

	private XDIAddress getDefaultCardXdiAddress() throws Xdi2ClientException {
		CloudUser user = (CloudUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		MessageEnvelope messageEnvelope = new MessageEnvelope();
		MessageCollection messageCollection = messageEnvelope.getMessageCollection(user.getCloudNumber().getXDIAddress(), true);
		Message message = messageCollection.createMessage();
		message = user.prepareMessageToCloud(message);

		message.createGetOperation(XDIAddressUtil.concatXDIAddresses(user.getCloudNumber().getXDIAddress(), CardXdiModelConverter.XDI_CARD_DEFAULT));

		log.debug("getDefaultCardXdiAddress message:\n" + messageEnvelope.getGraph().toString("XDI DISPLAY", null));

		MessagingResponse messagingResponse = user.getXdiClient().send(messageEnvelope);

		System.out.println(messagingResponse.getResultGraph().toString("XDI DISPLAY", null));

		Relation relation = messagingResponse.getResultGraph().getDeepRelation(XDIAddressUtil.concatXDIAddresses(user.getCloudNumber().getXDIAddress(), CardXdiModelConverter.XDI_CARD_DEFAULT), XDIAddress.create("$ref"));
		if (relation == null) return null;

		return relation.getTargetXDIAddress();
	}

	@Secured("IS_AUTHENTICATED")
	public XDIAddress getCardAddressByShortcut(String cardShortcut) throws Xdi2ClientException {
		Assert.hasLength(cardShortcut);

		cardShortcut += "$card";

		CloudUser user = (CloudUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		MessageEnvelope messageEnvelope = new MessageEnvelope();
		MessageCollection messageCollection = messageEnvelope.getMessageCollection(user.getCloudNumber().getXDIAddress(), true);
		Message message = messageCollection.createMessage();
		message = user.prepareMessageToCloud(message);

		message.createGetOperation(XDIAddress.create(cardShortcut));

		log.debug("getCardAddressByShortcut message:\n" + messageEnvelope.getGraph().toString("XDI DISPLAY", null));

		MessagingResponse messagingResponse = user.getXdiClient().send(messageEnvelope);

		Relation relation = messagingResponse.getResultGraph().getDeepRelation(XDIAddress.create(cardShortcut), XDIAddress.create("$ref"));
		if (relation == null) return null;

		return relation.getTargetXDIAddress();
	}
}
