package xdi2.manager.controller;

import java.io.IOException;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import xdi2.client.exceptions.Xdi2ClientException;
import xdi2.core.syntax.XDIAddress;
import xdi2.manager.model.Card;
import xdi2.manager.model.CloudUser;
import xdi2.manager.service.CardService;
import xdi2.manager.util.XdiUtils;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/1.0/cloud/cards/")
public class CardController extends AbstractController {
	private static final Logger log = LoggerFactory.getLogger(CardController.class);

	
	@Autowired
	CardService cardService;

	@RequestMapping(value = "/", method = RequestMethod.GET)
	public List<Card> getCards() throws Xdi2ClientException {
		return cardService.getCards();
	}
	
	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
	public Card getCard(@PathVariable String id, @RequestParam(value="edit", required=false, defaultValue="false") boolean edit) throws Xdi2ClientException {
		return cardService.getCard(XdiUtils.convertIdToXdiAddress(id), edit);
	}
	
	@RequestMapping(value = "/", method = RequestMethod.POST)
	public void createCard(@RequestParam("card") String jsonCard, @RequestParam(value="file", required=false) MultipartFile file) throws Xdi2ClientException, JsonParseException, JsonMappingException, IOException {
		saveCard(jsonCard, file, false);
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
	public void deleteCard(@PathVariable String id) throws Xdi2ClientException {
		cardService.deleteCard(XdiUtils.convertIdToXdiAddress(id));
	}
	
	@RequestMapping(value = "/{id}", method = RequestMethod.POST)
	public void editCard(@PathVariable String id, @RequestParam("card") String jsonCard, @RequestParam(value="file", required=false) MultipartFile file) throws Xdi2ClientException, JsonParseException, JsonMappingException, IOException {
		saveCard(jsonCard, file, true);
	}
	
	private void saveCard(String jsonCard, MultipartFile file, boolean edit) throws JsonParseException, JsonMappingException, IOException, Xdi2ClientException {
		
		ObjectMapper mapper = new ObjectMapper();
		Card card = mapper.readValue((String) jsonCard, Card.class);
		
		if (file != null) {
			log.debug("file: [" + file.getOriginalFilename() + " : " + file.getSize() + " bytes]");
			String backgroundImage = Base64.encodeBase64String(file.getBytes());
			card.setBackgroundImage(backgroundImage);
		}
		
		if (edit) {
			cardService.editCard(card);
		}
		else {
			cardService.createCard(card);
		}
	}
	
	@RequestMapping(value = "/count/", method = RequestMethod.GET)
	public long getCardsCount() throws Xdi2ClientException {
		return cardService.getCards().size();
	}
	
	@RequestMapping(value = "/{id}/default/", method = RequestMethod.POST)
	public void setDefaultCard(@PathVariable String id) throws Xdi2ClientException {
		cardService.setDefaultCard(XdiUtils.convertIdToXdiAddress(id));
	}
	
	@RequestMapping(value = "/tag/{tag}", method = RequestMethod.GET, produces="application/json")
	public String checkTag(@PathVariable String tag) throws Xdi2ClientException {
		CloudUser user = (CloudUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		
		XDIAddress cardAddress = cardService.getCardAddressByShortcut(user.getCloudNumber() + "#" + tag);
		
		return cardAddress == null ? null : "\"" + cardAddress.toString() + "\"";
	}
}
