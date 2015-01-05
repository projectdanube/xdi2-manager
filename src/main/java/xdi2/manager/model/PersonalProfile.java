package xdi2.manager.model;


public class PersonalProfile {

	private String firstName;
	private String lastName;
	private String nickname;
	private String gender;
	private String birthDate;
	private String nationality;
	private String phone;
	private String mobilePhone;
	private String workPhone;
	private PostalAddress address;
	private String email;
	private String website;
	
	private String xdiData;
	
	public PersonalProfile() {
		super();
		this.address = new PostalAddress();
	}
	
	@Override
	public String toString() {
		return "PersonalProfile [firstName=" + firstName + ", lastName="
				+ lastName + ", nickname=" + nickname + ", gender=" + gender
				+ ", birthDate=" + birthDate + ", nationality=" + nationality
				+ ", phone=" + phone + ", mobilePhone=" + mobilePhone
				+ ", workPhone=" + workPhone + ", address=" + address
				+ ", email=" + email + ", website=" + website + ", xdiData="
				+ xdiData + "]";
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getBirthDate() {
		return birthDate;
	}

	public void setBirthDate(String birthDate) {
		this.birthDate = birthDate;
	}

	public String getNationality() {
		return nationality;
	}

	public void setNationality(String nationality) {
		this.nationality = nationality;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getMobilePhone() {
		return mobilePhone;
	}

	public void setMobilePhone(String mobilePhone) {
		this.mobilePhone = mobilePhone;
	}

	public String getWorkPhone() {
		return workPhone;
	}

	public void setWorkPhone(String workPhone) {
		this.workPhone = workPhone;
	}

	public PostalAddress getAddress() {
		return address;
	}

	public void setAddress(PostalAddress address) {
		this.address = address;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getWebsite() {
		return website;
	}

	public void setWebsite(String website) {
		this.website = website;
	}

	public String getXdiData() {
		return xdiData;
	}

	public void setXdiData(String xdiData) {
		this.xdiData = xdiData;
	}
	
	
}
