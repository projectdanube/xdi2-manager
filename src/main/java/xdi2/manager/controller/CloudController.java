package xdi2.manager.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import xdi2.client.exceptions.Xdi2ClientException;
import xdi2.manager.model.Connection;
import xdi2.manager.model.ConnectionTemplate;
import xdi2.manager.model.PersonalProfile;
import xdi2.manager.service.CloudService;

@RestController
@RequestMapping("/api/1.0/cloud/")
public class CloudController extends AbstractController {

	@Autowired
	private CloudService cloudService;

	/* Personal Profile */
	@RequestMapping(value = "/profile/", method = RequestMethod.GET)
	public PersonalProfile getPersonalProfile() throws Xdi2ClientException {		
		return cloudService.getPersonalProfile();
	}

	@RequestMapping(value = "/profile/", method = RequestMethod.POST)
	public void updatePersonalProfile(@RequestBody PersonalProfile profile) throws Xdi2ClientException {
		Assert.notNull(profile);
		cloudService.updatePersonalProfile(profile);
	}

	/* Connections */
	@RequestMapping(value = "/connections/", method = RequestMethod.POST)
	public void createConnection(@RequestBody Connection connection) throws Xdi2ClientException {
		cloudService.createConnection(connection);
	}

	@RequestMapping(value = "/connections/", method = RequestMethod.GET)
	public List<Connection> getConnections() throws Xdi2ClientException {
		return cloudService.getConnections();
	}

	@RequestMapping(value = "/connections/{id}", method = RequestMethod.GET)
	public Connection getConnection(@PathVariable String id) throws Xdi2ClientException {
		return cloudService.getConnection(id);
	}

	@RequestMapping(value = "/connections/{id}", method = RequestMethod.DELETE)
	public void deleteConnection(@PathVariable String id) throws Xdi2ClientException {
		cloudService.deleteConnection(id);
	}

	@RequestMapping(value = "/connections/count/", method = RequestMethod.GET)
	public long getConnectionsCount() throws Xdi2ClientException {
		return cloudService.getConnections().size();
	}

	/* Connection Templates */
	@RequestMapping(value = "/connectionTemplates/", method = RequestMethod.POST)
	public void createConnectionTemplate(@RequestBody ConnectionTemplate connectionTemplate) throws Xdi2ClientException {
		cloudService.createConnectionTemplate(connectionTemplate);
	}

	@RequestMapping(value = "/connectionTemplates/", method = RequestMethod.GET)
	public List<ConnectionTemplate> getConnectionTemplates() throws Xdi2ClientException {
		return cloudService.getConnectionTemplates();
	}

	@RequestMapping(value = "/connectionTemplates/{id}", method = RequestMethod.GET)
	public ConnectionTemplate getConnectionTemplate(@PathVariable String id) throws Xdi2ClientException {
		return cloudService.getConnectionTemplate(id);
	}

	@RequestMapping(value = "/connectionTemplates/{id}", method = RequestMethod.DELETE)
	public void deleteConnectionTemplate(@PathVariable String id) throws Xdi2ClientException {
		cloudService.deleteConnectionTemplate(id);
	}
	
	@RequestMapping(value = "/connectionTemplates/count/", method = RequestMethod.GET)
	public long getConnectionTemplateCount() throws Xdi2ClientException {
		return cloudService.getConnectionTemplates().size();
	}

	@RequestMapping(value = "/keys/sig/", method = RequestMethod.GET)
	public String[] getSignatureKeyPair() throws Xdi2ClientException {
		return cloudService.getSignatureKeyPair();
	}

	@RequestMapping(value = "/keys/encrypt/", method = RequestMethod.GET)
	public String[] getEncryptKeyPair() throws Xdi2ClientException {
		return cloudService.getEncryptKeyPair();
	}

	@RequestMapping(value = "/keys/sig/", method = RequestMethod.POST)
	public void generateSignatureNewKeyPair() throws Xdi2ClientException {
		cloudService.generateSignatureNewKeyPair();
	}

	@RequestMapping(value = "/keys/encrypt/", method = RequestMethod.POST)
	public void generateEncryptNewKeyPair() throws Xdi2ClientException {
		cloudService.generateEncryptNewKeyPair();
	}

	@RequestMapping(value = "/keys/sig/", method = RequestMethod.DELETE)
	public void deleteSignatureKeyPair() throws Xdi2ClientException {
		cloudService.deleteSignatureKeyPair();
	}

	@RequestMapping(value = "/keys/encrypt/", method = RequestMethod.DELETE)
	public void deleteEncryptKeyPair() throws Xdi2ClientException {
		cloudService.deleteEncryptKeyPair();
	}
	
	@RequestMapping(value = "/stats/statementsCount", method = RequestMethod.GET)
	public long getStatementsCount() throws Xdi2ClientException {
		return cloudService.getAllStatementsCount();
	}

	@RequestMapping(value = "/stats/cloudNamesCount", method = RequestMethod.GET)
	public long getCloudNamesCount() throws Xdi2ClientException {
		return cloudService.getCloudNames().size();
	}

	@RequestMapping(value = "/stats/dependentsCount", method = RequestMethod.GET)
	public long getDependentsCount() throws Xdi2ClientException {
		return cloudService.getDependents().size();
	}
}
