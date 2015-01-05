package xdi2.manager.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Configuration {
	
	@Value("${cloud.card.app.url}")
	private String cloudCardAppUrl;

	public String getCloudCardAppUrl() {
		return cloudCardAppUrl;
	}

}
