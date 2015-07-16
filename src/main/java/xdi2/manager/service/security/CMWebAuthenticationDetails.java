package xdi2.manager.service.security;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.web.authentication.WebAuthenticationDetails;

public class CMWebAuthenticationDetails extends WebAuthenticationDetails {
	private static final long serialVersionUID = 8858818843654918856L;

    public CMWebAuthenticationDetails(HttpServletRequest request) {
        super(request);
    }

	@Override
	public String toString() {
		return "CMWebAuthenticationDetails ["
				+ "getRemoteAddress()=" + getRemoteAddress()
				+ ", getSessionId()=" + getSessionId() + "]";
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		return true;
	}



    
    

}
