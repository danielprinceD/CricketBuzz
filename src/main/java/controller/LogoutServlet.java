package controller;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class LogoutServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private Cookie getCookies(Cookie []cookies) {
		if(cookies == null)
			return null;
		
		for(Cookie cookie : cookies)
			if(cookie.getName().equals("token"))
				return cookie;
		
		return null;
	}
    
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		Cookie cookie = getCookies(request.getCookies());
		System.out.println(cookie.getValue());
		if(cookie == null || cookie.getValue().isEmpty())
		{
			Extra.sendError(response, response.getWriter() , "You cannot logout");
			return;
		}
		cookie.setMaxAge(0);
		cookie.setPath("/");
		cookie.setSecure(false);
		response.addCookie(cookie);
		Extra.sendError(response, response.getWriter(), "Logged out successfully");
		
	}


}
