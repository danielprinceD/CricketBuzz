package controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.sql.*;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import repository.*;
import model.*;

public class VenueServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
    
    private VenueDAO venueDAO;
    
    @Override
    public void init() {
    	venueDAO = new VenueDAO();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
    	String pathInfo = request.getPathInfo();
    	PrintWriter out = response.getWriter();
    	
    	try {
    		
    		if(pathInfo == null)
        	{
    			List<VenueVO> venues = venueDAO.getAllVenues();
    			if(venues == null)
    			{
    				Extra.sendSuccess(response, out, "Data is Empty");
    				return;
    			}
    			out.println(new Gson().toJson(venues));
    			return;
        	}
    		else {
    			String pathArr[] = pathInfo.split("/");
    			if(pathArr.length == 2)
    			{
    				VenueVO venue = venueDAO.getVenueById(Integer.parseInt(pathArr[1]));
    				if(venue == null)
    				{
    					Extra.sendError(response, out, "No Data Found for this ID " + pathArr[1]);
    					return;
    				}
    				out.print(new Gson().toJson(venue));
    				return;
    			}
    			else {
    				Extra.sendError(response, out, "Enter a Valid Endpoint");
    				return;
    			}
    		}
    		
    		
    	} catch (SQLException e) {
    		Extra.sendError(response, out, e.getMessage());
    		e.printStackTrace();
        
    	}
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {

        StringBuilder jsonString = new StringBuilder();
        BufferedReader reader = request.getReader();
        String line;
        
        while ((line = reader.readLine()) != null) {
            jsonString.append(line);
        }
        
        PrintWriter out = response.getWriter();
        TypeToken<List<VenueVO>> typeToken = new TypeToken<List<VenueVO>>() {}; 
        List<VenueVO> venueModelList = new Gson().fromJson(jsonString.toString(), typeToken.getType());

        JSONArray responseArray = new JSONArray();
        
        try {
            
            
            int rowsAffected = 0;

            for (VenueVO venueModel : venueModelList) {
                if (venueModel.getVenueId() < 0 && venueModel.isValid()) {
                    rowsAffected = venueDAO.insertVenue(venueModel);
                } else if (venueModel.isValid() && request.getMethod().equalsIgnoreCase("PUT")) {
                    rowsAffected = venueDAO.updateVenue(venueModel);
                } else {
                    JSONObject errorObj = new JSONObject();
                    errorObj.put("error", "Invalid data for Venue");
                    responseArray.put(errorObj);
                    throw new SQLException("Invalid data for Venue");
                }

                if (rowsAffected > 0) {
                    Extra.sendSuccess(response, out, "Data has been inserted/updated successfully");
                   
                    return;
                } else {
                    Extra.sendError(response, out, "Insert/Update failed");
                    return;
                }
            }
        } catch (SQLException e) {
            
            JSONObject errorObj = new JSONObject();
            errorObj.put("error",  e.getMessage());
            responseArray.put(errorObj);
        }
    }



    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {

    	String pathInfoString = request.getPathInfo();		
		String[] pathArray = pathInfoString != null ?  pathInfoString.split("/") : null;
		PrintWriter out = response.getWriter();
    	
		if(pathArray == null || pathArray.length <= 0 )
		{
			Extra.sendError(response, out, "ID is required");
			return;
		}
    	
		String venueId = pathArray[1];
	      
        if (venueId == null) {
            Extra.sendError(response , out , "Venue ID is required");
            return;
        }
       try {
		venueDAO.deleteVenue(Integer.parseInt(venueId));
		Extra.sendSuccess(response, out, "Venue deleted Successfully");
	} catch (NumberFormatException | SQLException e) {
		Extra.sendError(response, out, "Cannot Delete Venue");
		e.printStackTrace();
	}
    }
    
    @Override
    protected void doPut(HttpServletRequest request , HttpServletResponse response) throws ServletException , IOException {
    	doPost(request, response);
	}

}

