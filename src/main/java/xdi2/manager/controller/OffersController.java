package xdi2.manager.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import xdi2.client.exceptions.Xdi2ClientException;
import xdi2.manager.service.Configuration;

@Controller
public class OffersController extends AbstractController {
	
	@Autowired
	private Configuration configuration;	

	@ResponseBody
	@RequestMapping(value = "/offers/", method = RequestMethod.GET)
	public int getOffers() throws Xdi2ClientException {		
		return 0;
	}
}
