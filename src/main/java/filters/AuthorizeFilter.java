package filters;

import java.io.IOException;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;

import controller.Extra;
import utils.AuthUtil;

public class AuthorizeFilter extends HttpFilter implements Filter {
   
	private Cookie getCookies(Cookie []cookies) {
		if(cookies == null)
			return null;
		
		for(Cookie cookie : cookies)
			if(cookie.getName().equals("token"))
				return cookie;
		
		return null;
	}
   
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		
		HttpServletRequest req = (HttpServletRequest)request;
		HttpServletResponse res = (HttpServletResponse)response;
		
		String method = req.getMethod();
		
		
		
		Cookie cookie = getCookies(req.getCookies());
		
		
		if(cookie == null)
		{
			Extra.sendError(res, res.getWriter(), "Login to Continue");
			return;
		}
		
		String headerToken = cookie.getValue();
		
		try {
			
		DecodedJWT decoded = AuthUtil.verifyToken(headerToken);
		
		
		String id = decoded.getSubject();
		String role = decoded.getClaim("role").asString();
		
		
        if(role.equalsIgnoreCase("ADMIN"))
        {
        	chain.doFilter(request, response);
        	return;
        }
        
		if(role.equalsIgnoreCase("USER"))
		{
			String pathInfo = req.getPathInfo();
			if(pathInfo != null)
			{
				if(pathInfo.startsWith("/users"))
				{
					String[] pathArr = pathInfo.split("/");
					if(pathArr.length == 2 && pathArr[1].equals(id))
					{
						chain.doFilter(request, response);
						return;
					}
					else {
						Extra.sendError(res, res.getWriter(), "You don't have access");
						return;
					}
				}
			}
			if(method.equalsIgnoreCase("GET"))
			{
				chain.doFilter(request, response);
				return;
			}
		}
		
		}
		catch (Exception e) {
			e.printStackTrace();
			Extra.sendError(res, res.getWriter(), e.getMessage());
		}
		
		
		Extra.sendError(res, res.getWriter(), "Unauthorized Access");
		
	}


}


/*   <filter>
			<filter-name>AuthorizeFilter</filter-name>
			<filter-class>filters.AuthorizeFilter</filter-class>
		</filter>
		
		<filter-mapping>
			
			<filter-name>AuthorizeFilter</filter-name>
			<url-pattern>/tournaments/*</url-pattern>
			<url-pattern>/teams/*</url-pattern>
			<url-pattern>/fixtures/*</url-pattern>
			<url-pattern>/players/*</url-pattern>
			 <url-pattern>/playing-11s</url-pattern>
			 <url-pattern>/over_summary</url-pattern>
			 <url-pattern>/commentaries/*</url-pattern>
			 <url-pattern>/match-details</url-pattern>
			  <url-pattern>/user/logout</url-pattern>
			 
		
		</filter-mapping>  */
