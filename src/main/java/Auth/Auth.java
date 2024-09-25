//package Auth;
//
//import java.io.IOException;
//import javax.servlet.Filter;
//import javax.servlet.FilterChain;
//import javax.servlet.FilterConfig;
//import javax.servlet.ServletException;
//import javax.servlet.ServletRequest;
//import javax.servlet.ServletResponse;
//import javax.servlet.annotation.WebFilter;
//import javax.servlet.http.HttpFilter;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//
//import Team.Extra;
//
//
//
//public class Auth extends HttpFilter implements Filter {
//       
//   
//	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
//		
//		HttpServletRequest req = (HttpServletRequest)request;
//		HttpServletResponse res = (HttpServletResponse)response;
//		
//		String method = req.getMethod();
//			
//			if(UserData.u1.role.equalsIgnoreCase("ADMIN"))
//			{
//				chain.doFilter(request, response);
//				return;
//			}
//			else {
//				
//				if(method.equalsIgnoreCase("GET"))
//				{
//					chain.doFilter(request, response);
//					return;
//				}
//			}
//	
//		
//		Extra.sendError(res, res.getWriter(), "Unauthorized Access");
//		
//	}
//
//	public void init(FilterConfig fConfig) throws ServletException {
//		
//	}
//
//}
//
//
////
////<filter>
////	<filter-name>AuthUser</filter-name>
////	<filter-class>Auth.AuthUser</filter-class>
////</filter>
////
////<filter>
////	<filter-name>Auth</filter-name>
////	<filter-class>Auth.Auth</filter-class>
////</filter>
////
////<filter-mapping>
////	<filter-name>AuthUser</filter-name>
////	<url-pattern>/users/*</url-pattern>
////</filter-mapping>
////
////<filter-mapping>
////	<filter-name>Auth</filter-name>
////	<url-pattern>/tournament/*</url-pattern>
////	<url-pattern>/teams/*</url-pattern>
////	<url-pattern>/fixtures/*</url-pattern>
////	<url-pattern>/players/*</url-pattern>
////	 <url-pattern>/playing-11s</url-pattern>
////	 <url-pattern>/over_summary</url-pattern>
////	 <url-pattern>/commentaries/*</url-pattern>
////	 <url-pattern>/match-details</url-pattern>
////
////</filter-mapping>
////
