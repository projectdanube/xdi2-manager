package xdi2.manager.model;

public class PostalAddress {
	
	private String street;
	private String postalCode;
	private String locality;
	private String region;
	private String country;
	

	public String getStreet() {
		return street;
	}
	public void setStreet(String street) {
		this.street = street;
	}
	public String getPostalCode() {
		return postalCode;
	}
	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}
	public String getLocality() {
		return locality;
	}
	public void setLocality(String locality) {
		this.locality = locality;
	}
	public String getRegion() {
		return region;
	}
	public void setRegion(String region) {
		this.region = region;
	}
	public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		this.country = country;
	}
	
	@Override
	public String toString() {
		return "PostalAddress [street=" + street + ", postalCode=" + postalCode
				+ ", locality=" + locality + ", region=" + region
				+ ", country=" + country + "]";
	}

}


