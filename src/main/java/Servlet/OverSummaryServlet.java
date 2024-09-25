package Servlet;

import java.io.BufferedReader;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONException;
import DAO.OverSummaryDAO;


public class OverSummaryServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private OverSummaryDAO overSummaryDAO;
    
	
	@Override
	public void init() {
		overSummaryDAO = new OverSummaryDAO();
	}
    
    

    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        StringBuilder jsonBuffer = new StringBuilder();
        String line;
        try (BufferedReader reader = request.getReader()) {
            while ((line = reader.readLine()) != null) {
                jsonBuffer.append(line);
            }
        }

        JSONArray jsonArray;
        try {
            jsonArray = new JSONArray(jsonBuffer.toString());
            
            
            
        } catch (JSONException e) {
        	
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON format.");
            
            
            return;
        }
        
        try {
        	
        	overSummaryDAO.insert(request, response, response.getWriter() , jsonArray);
			
		} catch (Exception e) {
			Extra.sendError(response, response.getWriter(), e.getMessage());
		}
        
        
    }

	
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
	        throws ServletException, IOException {
		
		try {
			overSummaryDAO.get(request, response);
		} catch (Exception e) {
			Extra.sendError(response, response.getWriter() , e.getMessage());
			e.printStackTrace();
		}
	    
	}

	
	
	@Override
	protected void doPut(HttpServletRequest request, HttpServletResponse response)
	        throws ServletException, IOException {

	    StringBuilder jsonBuffer = new StringBuilder();
	    String line;
	    try (BufferedReader reader = request.getReader()) {
	        while ((line = reader.readLine()) != null) {
	            jsonBuffer.append(line);
	        }
	    }

	    JSONArray jsonArray;
	    
	    try {
	        jsonArray = new JSONArray(jsonBuffer.toString());
	    } catch (JSONException e) {
	    	Extra.sendError(response, response.getWriter(), "Invalid JSON format.");
	        return;
	    }
	    
	    try {
			overSummaryDAO.update(request, response, jsonArray);
		} catch (Exception e) {
			Extra.sendError(response, response.getWriter() , e.getMessage());
			e.printStackTrace();
		}

	    
	}



	@Override
	protected void doDelete(HttpServletRequest request, HttpServletResponse response)
	        throws ServletException, IOException {

	    try {
			overSummaryDAO.delete(request, response);
		} catch (Exception e) {
			Extra.sendError(response, response.getWriter() , e.getMessage());
			e.printStackTrace();
		}
	}




}
