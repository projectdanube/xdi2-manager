package xdi2.manager.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;


public abstract class AbstractController {
	private static final Logger log = LoggerFactory.getLogger(AbstractController.class);

	
	@ExceptionHandler(Exception.class)
	@ResponseBody
	@ResponseStatus(value = HttpStatus.BAD_REQUEST)
	public String handleException(Exception e) {
		log.error("Unexpected exception caught: " + e, e);
		
		ObjectMapper mapper = new ObjectMapper();
		
		String jsonError = null;
		try {
			jsonError = mapper.writeValueAsString(e.getMessage());
		} catch (JsonProcessingException e1) {
			log.error("Unable to convert error message to JSON format. Returning null.");
			return null;
		}
		
		return jsonError;
	}
}
