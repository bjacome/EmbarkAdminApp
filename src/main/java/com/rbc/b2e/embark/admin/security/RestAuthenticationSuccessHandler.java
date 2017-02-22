package com.rbc.b2e.embark.admin.security;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.internal.LinkedTreeMap;
import com.rbc.b2e.embark.admin.model.AdminUser;
import com.rbc.b2e.embark.admin.rest.Response;
import com.rbc.b2e.embark.admin.util.SystemConstants;


public class RestAuthenticationSuccessHandler  extends
SavedRequestAwareAuthenticationSuccessHandler {

	private static final Logger logger = LoggerFactory.getLogger(RestAuthenticationSuccessHandler.class);

	final private ObjectMapper mapper = new ObjectMapper();
	final private ConcurrentHashMap<String, LinkedTreeMap<?,?>> securityRoles = new ConcurrentHashMap<String, LinkedTreeMap<?,?>>();

	private RequestCache requestCache = new HttpSessionRequestCache();
	
    @Override
    public void onAuthenticationSuccess(final HttpServletRequest request, final HttpServletResponse response, final Authentication authentication) throws ServletException, IOException {
    	logger.debug("RestAuthenticationSuccessHandler - onAuthenticationSuccess - "+request.getLocalAddr());
    	
    	final AdminUser loggedUser;
    	if (authentication != null) {   		
    		loggedUser = (AdminUser)authentication.getPrincipal();
    		logger.debug("loggedUser is: "+loggedUser.getEmail());
    		request.getSession().setAttribute(SystemConstants.LOGGED_USER, loggedUser);
    	} else {
    		loggedUser = new AdminUser();
    	}
    	
    	final Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
    	final Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
		final GrantedAuthority ga = iterator.next();
		final String role = ga.getAuthority();
		
		final SavedRequest savedRequest = requestCache.getRequest(request, response);

        if (savedRequest == null) {
            clearAuthenticationAttributes(request);
        } else {
	        final String targetUrlParameter = getTargetUrlParameter();
	        if (isAlwaysUseDefaultTargetUrl() || (targetUrlParameter != null && StringUtils.hasText(request.getParameter(targetUrlParameter)))) {
	            requestCache.removeRequest(request, response);
	        }
        } 
        clearAuthenticationAttributes(request);
		
    	this.buildUserProfileResponse(request,response, loggedUser, role);
     }

    public void setRequestCache(final RequestCache requestCache) {
        this.requestCache = requestCache;
    }
    
    
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void buildUserProfileResponse(final HttpServletRequest request, final HttpServletResponse response,AdminUser loggedUser , String role) {
		try {
			logger.debug("buildUserProfileResponse");
			if (securityRoles.isEmpty()) {
				
				this.initSecurityRoles();
			}
			final Response responseObj = new Response();
			final LinkedTreeMap user = securityRoles.get(role);
			user.put("isPasswordValid", loggedUser.isPasswordValid());
			user.put("name",loggedUser.getFullName());
			responseObj.setData(user);
			final String jsonInString = mapper.writeValueAsString(responseObj);
			response.setHeader(SystemConstants.CONTENT_TYPE,SystemConstants.CONTENT_TYPE_APPLICATION_JSON);
			response.setStatus(HttpServletResponse.SC_OK);
			response.getWriter().print(jsonInString);
			response.getWriter().flush();
		} catch (Exception ex) {
			logger.error(ex.getLocalizedMessage(), ex);
		}
	}
	
	public void initSecurityRoles(){
		logger.debug("SECURITY_ROLES");
		String SECURITY_ROLES = System.getenv("SECURITY_ROLES");
		if (SECURITY_ROLES != null) {
        	logger.debug("SECURITY_ROLES: "+ SECURITY_ROLES);
            // parse the SECURITY_ROLES JSON structure
        	Gson gson = new Gson();
            JsonArray securityRolesJsonArray = (JsonArray) new JsonParser().parse(SECURITY_ROLES);
            for (int index = 0; index< securityRolesJsonArray.size(); index++) {
            	JsonObject role = (JsonObject) securityRolesJsonArray.get(index);
            	JsonPrimitive key = role.getAsJsonPrimitive("securityRole");
            	JsonObject value = (JsonObject) role.get("profile");          	
				securityRoles.put(key.getAsString(), gson.fromJson(value, LinkedTreeMap.class) );
            }
		} else {
			logger.error("SECURITY_ROLES not found");
		}
	}
}
