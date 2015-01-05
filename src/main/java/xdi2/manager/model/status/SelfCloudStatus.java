package xdi2.manager.model.status;

import java.util.List;

public class SelfCloudStatus {

	private String cloudName;

	private String cloudNumber;
	private List<String> cloudNames;
	private List<String> dependents;
	private List<String> guardians;

	public SelfCloudStatus(String cloudName) {
		super();
		this.cloudName = cloudName;
	}

	public String getCloudName() {
		return cloudName;
	}

	public void setCloudName(String cloudName) {
		this.cloudName = cloudName;
	}

	public String getCloudNumber() {
		return cloudNumber;
	}

	public void setCloudNumber(String cloudNumber) {
		this.cloudNumber = cloudNumber;
	}

	public List<String> getCloudNames() {
		return cloudNames;
	}

	public void setCloudNames(List<String> cloudNames) {
		this.cloudNames = cloudNames;
	}
	public List<String> getDependents() {
		return dependents;
	}

	public void setDependents(List<String> dependents) {
		this.dependents = dependents;
	}

	public List<String> getGuardians() {
		return guardians;
	}

	public void setGuardians(List<String> guardians) {
		this.guardians = guardians;
	}


}
