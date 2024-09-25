package controller;

import java.io.BufferedReader;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import repository.UserDAO;

public class RegisterServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
      
	private UserDAO userDAO;
			
	@Override
	public void init() {
		userDAO = new UserDAO();
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		StringBuilder jsonString = new StringBuilder();
        BufferedReader reader = request.getReader();
        String line;
        while ((line = reader.readLine()) != null) {
            jsonString.append(line);
        }
        
        try {
			userDAO.register(request, response, false , jsonString);
		} catch (Exception e) {
			Extra.sendError(response, response.getWriter(), e.getMessage());
			e.printStackTrace();
		}
	}

}
