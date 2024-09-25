package Servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import DAO.UserDAO;

public class UserServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
    private static final String DB_URL = "jdbc:mysql://localhost:3306/CricketBuzz";
    private static final String USER = "root";
    private static final String PASS = "";
    
    private UserDAO userDAO;
    
    @Override 
    public void init() {
    	userDAO = new UserDAO();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
    	
    	try {
			userDAO.get(request, response);
		} catch (Exception e) {
		}
    	
    	
    	
	}
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
    	Boolean isPut = request.getMethod().equalsIgnoreCase("PUT");

        StringBuilder jsonString = new StringBuilder();
        BufferedReader reader = request.getReader();
        String line;
        while ((line = reader.readLine()) != null) {
            jsonString.append(line);
        }

        userDAO.post(request, response, isPut, jsonString);
        
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }
    
    @Override
    protected void doDelete(HttpServletRequest request , HttpServletResponse response) throws ServletException , IOException{
    	
    	 response.setContentType("text/plain");
         PrintWriter out = response.getWriter();
         
         String pathInfoString = request.getPathInfo();		
 		String[] pathArray = pathInfoString != null ?  pathInfoString.split("/") : null;
 		
 		if(pathArray == null || pathArray.length == 0)
 		{
 			Extra.sendError(response, out, "No ID is mentioned");
 			return;
 		}
 		
 		
 		
 		String sql = "DELETE FROM user where user_id = ?";
 		
 		
 		try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
                 PreparedStatement pstmt = conn.prepareStatement(sql); 
 				) {
 			Integer userId = Integer.parseInt(pathArray[1]);
 			pstmt.setInt(1, userId);
 			int affected = pstmt.executeUpdate();
 			
 			if(affected > 0)
 			Extra.sendSuccess(response, out,"Deleted Successfully");
 			else 
 				Extra.sendError(response, out, "No Data Found in that id");
 		}
 		catch (SQLException e) {
             response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
             Extra.sendError(response, out, e.getMessage());
             e.printStackTrace();
         }
 		catch (Exception e) {
 			Extra.sendError(response, out, e.getMessage());
 		}
    	
    }

	

}
