package filters;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import controller.Extra;

public class AuthorizeFilter extends HttpFilter implements Filter {
       
   
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		
		HttpServletRequest req = (HttpServletRequest)request;
		HttpServletResponse res = (HttpServletResponse)response;
		
		String method = req.getMethod();
		
		HttpSession session = req.getSession(false);
		
		if(session == null)
		{
			Extra.sendError(res, res.getWriter() , "No session found. Please Login");
			return;
		}
		
		
		String email = (String) session.getAttribute("email");
		Integer id = (Integer) session.getAttribute("user_id");
        String role = (String) session.getAttribute("role");
		
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
					if(pathArr.length == 2 && Integer.parseInt(pathArr[1]) == id)
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
		
		Extra.sendError(res, res.getWriter() , "Unauthorized Access");
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
