package Servlet;

import java.io.*;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.gson.Gson;
import DAO.MatchDetailDAO;
import Model.MatchDetailVO;

public class MatchDetailServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    
   private MatchDetailDAO matchDetailDAO;
    
   @Override
   public void init() {
	   matchDetailDAO = new MatchDetailDAO();
   }
    

    	
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
       
    	if(request.getParameter("fixture_id") == null)
    	{
    		Extra.sendError(response, response.getWriter(), "Fixture ID is required");
    		return;
    	}
    	
    	int fixtureId = Integer.parseInt(request.getParameter("fixture_id"));
    	
    	StringBuilder jsonString = new StringBuilder();
    	BufferedReader reader = request.getReader();
    	String line;
    	
    	while((line = reader.readLine())!= null)
    		jsonString.append(line);
    	
    	MatchDetailVO matchDetailModel = new Gson().fromJson(jsonString.toString(), MatchDetailVO.class);
    	
    	
    	try {
			matchDetailDAO.insert(request, response, fixtureId , matchDetailModel);
		} catch (Exception e) {
			Extra.sendError(response, response.getWriter(), e.getMessage());
		}
    	
    
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        int fixtureId = Integer.parseInt(request.getParameter("fixture_id"));

        try {
			matchDetailDAO.get(request, response, fixtureId);
		} catch (Exception e) {
			Extra.sendError(response, response.getWriter(), e.getMessage());
		}
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        doPost(request, response);
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        int fixtureId = Integer.parseInt(request.getParameter("fixture_id"));

        try {
			matchDetailDAO.delete( response , fixtureId);
		} catch (Exception e) {
			Extra.sendError(response, response.getWriter(), e.getMessage());
		}
    }
}

