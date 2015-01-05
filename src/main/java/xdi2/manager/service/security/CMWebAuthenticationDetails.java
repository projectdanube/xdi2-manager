package xdi2.manager.service.security;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.web.authentication.WebAuthenticationDetails;

import xdi2.manager.model.Environment;

public class CMWebAuthenticationDetails extends WebAuthenticationDetails {
	private static final long serialVersionUID = 8858818843654918856L;
	
	private final Environment env;

    public CMWebAuthenticationDetails(HttpServletRequest request) {
        super(request);
        
        if ("PROD".equals(request.getParameter("env"))) {
        	this.env = Environment.PROD;
        }
        else {
        	this.env = Environment.OTE;
        }
    }

    public Environment getEnv() {
        return this.env;
    }

	@Override
	public String toString() {
		return "CMWebAuthenticationDetails [env=" + env
				+ ", getRemoteAddress()=" + getRemoteAddress()
				+ ", getSessionId()=" + getSessionId() + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((env == null) ? 0 : env.hashCode());
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
		CMWebAuthenticationDetails other = (CMWebAuthenticationDetails) obj;
		if (env != other.env)
			return false;
		return true;
	}



    
    

}
