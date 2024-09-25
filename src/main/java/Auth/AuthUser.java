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
//public class AuthUser extends HttpFilter implements Filter {
//       
//    
//	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
//
//		HttpServletRequest req = (HttpServletRequest)request;
//		HttpServletResponse res = (HttpServletResponse)response;
//		String method = req.getMethod();
//		String pathInfo = req.getPathInfo();
//		
//		
//		if(pathInfo != null && (pathInfo.contains("/login") || pathInfo.contains("/register")) || pathInfo.contains("/logout") )
//		{
//			chain.doFilter(request, response);
//			return;
//		}
//		
//		if(!UserData.u1.role.equalsIgnoreCase("ADMIN"))
//		{
//			if(method.equalsIgnoreCase("PUT") || method.equalsIgnoreCase("GET") || method.equalsIgnoreCase("DELETE"))
//			{
//				if(UserData.u1.id < 0 )
//				{
//					System.out.println(UserData.u1.email);
//					Extra.sendError(res, res.getWriter(), "Your are a Guest. You cannot make any change to profile");
//					return;
//				}
//				
//				if(pathInfo != null)
//				{
//					String[] arr = pathInfo.split("/");
//					if(arr.length == 2 &&  UserData.u1.id == Integer.parseInt(arr[1]))
//					{
//						chain.doFilter(request, response);
//						return;
//					}
//					else {
//						Extra.sendError(res, res.getWriter() , "You Cannot Access others data");
//						return;
//					}
//				}
//			}
//				
//			
//			
//		}
//		
//		
//		
//		chain.doFilter(request, response);
//	}
//
//}
