package controller;

import java.io.BufferedReader;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import repository.UserDAO;

public class LoginServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
    
	private UserDAO userDAO;
	
	@Override
	public void init() {
		userDAO = new UserDAO();
	}
//	
//	private Cookie getCookies(Cookie cookies[]) {
//		
//		if(cookies == null)return null;
//		
//		for(Cookie cookie : cookies)
//			if(cookie.getName().equals("token"))
//				return cookie;
//		
//		return null;
//	}
//	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		StringBuilder jsonString = new StringBuilder();
        BufferedReader reader = request.getReader();
        String line;
        while ((line = reader.readLine()) != null) {
            jsonString.append(line);
        }
		
		try {
			
//			Cookie cookie = getCookies(request.getCookies());
//			
//			if(cookie != null)
//			{
//				Extra.sendError(response, response.getWriter(), "You are already logged in.");
//				return;
//			}
			
			userDAO.login(request, response, response.getWriter(), jsonString);
			
		} catch (Exception e) {
			e.printStackTrace();
			Extra.sendError(response, response.getWriter(), e.getMessage());
		}
		
	}

}
