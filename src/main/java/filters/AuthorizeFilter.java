package filters;

import java.io.IOException;
import java.util.Map;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.auth0.jwt.exceptions.JWTVerificationException;

import controller.Extra;
import utils.AuthUtil;

public class AuthorizeFilter extends HttpFilter implements Filter {
	private static final long serialVersionUID = 1L;

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		
		HttpServletRequest req = (HttpServletRequest)request;
		HttpServletResponse res = (HttpServletResponse)response;
		
		String method = req.getMethod();

		try {
		
		Map<String, String> details = AuthUtil.getDetails(req);
		
		if(details == null)
			throw new Exception("You're not authorized");
		
		String id = details.get("userId");
		String role = details.get("role");
		
        if(role.equalsIgnoreCase("ADMIN") || role.equals("SUPERADMIN"))
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
		catch (JWTVerificationException e) {
			Extra.sendError(res, res.getWriter(), "Token is Invalid");
			return;
		}
		catch (Exception e) {
			e.printStackTrace();
			Extra.sendError(res, res.getWriter(), e.getMessage());
			return;
		}
		
		Extra.sendError(res, res.getWriter() , "Unauthorized Access");
		
	}


}

