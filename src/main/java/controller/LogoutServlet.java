package controller;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import utils.JWTRedisUtil;

public class LogoutServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	
    
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String header = request.getHeader("Authorization");
		
		if(header != null && header.length() > 7)
		{
			header = header.substring(7);
			JWTRedisUtil.deactivateToken(header);
			Extra.sendSuccess(response, response.getWriter(), "Logged out successfully");
			return;
		}
		Extra.sendError(response, response.getWriter(), "Logout failed");
		
	}


}
