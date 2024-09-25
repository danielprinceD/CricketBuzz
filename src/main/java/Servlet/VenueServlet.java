package Servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import org.omg.CORBA.Request;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import DAO.VenueDAO;
import Model.VenueModel;
import Model.VenueVO;

public class VenueServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
    private static final String DB_URL = "jdbc:mysql://localhost:3306/CricketBuzz";
    private static final String USER = "root";
    private static final String PASS = "";
    
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
        TypeToken<List<VenueModel>> typeToken = new TypeToken<List<VenueModel>>() {}; 
        List<VenueModel> venueModelList = new Gson().fromJson(jsonString.toString(), typeToken.getType());

        String pathInfoString = request.getPathInfo();		
        String[] pathArray = pathInfoString != null ?  pathInfoString.split("/") : null;

        JSONArray responseArray = new JSONArray();
        Connection conn = null;

        try {
        	
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
            conn.setAutoCommit(false); 
            int rowsAffected=0;
            for (VenueModel venueModel : venueModelList) {
                           
                String sql;
                
                if (venueModel.getVenueId() < 0 && venueModel.isValid()) {
                    sql = "INSERT INTO venue (stadium, location, pitch_condition, description, capacity, curator) VALUES (?, ?, ?, ?, ?, ?)";
                } else if (venueModel.isValid() && request.getMethod().equalsIgnoreCase("PUT")) {
                    sql = "UPDATE venue SET stadium = ?, location = ?, pitch_condition = ?, description = ?, capacity = ?, curator = ? WHERE venue_id = ?";
                } else {
                    JSONObject errorObj = new JSONObject();
                    errorObj.put("error", "Missing Parameters");
                    responseArray.put(errorObj);
                    throw new SQLException("Invalid data for Venue");
                }
                
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

                    pstmt.setString(1, venueModel.getStadium());
                    pstmt.setString(2, venueModel.getLocation());
                    pstmt.setString(3, venueModel.getPitchCondition());
                    pstmt.setString(4, venueModel.getDescription());
                    pstmt.setLong(5, venueModel.getCapacity());
                    pstmt.setString(6, venueModel.getCurator());
                    
                    if (venueModel.getVenueId() > 0)
                        pstmt.setInt(7, venueModel.getVenueId());

                    rowsAffected = pstmt.executeUpdate();
                    JSONObject resultObj = new JSONObject();



                    responseArray.put(resultObj);

                } catch (NumberFormatException e) {
                    JSONObject errorObj = new JSONObject();
                    errorObj.put("error", e.getMessage());
                    responseArray.put(errorObj);
                    throw new SQLException(e); 
                }
            }
            if (rowsAffected > 0 ) {
            	Extra.sendSuccess(response, out, "Data has been inserted / updated successfully");
            	conn.commit();
            	return;
            }
            else 
            {
            	Extra.sendError(response, out, "Not updated/inserted");
            	return;
            }
           

            
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback(); 
                    JSONObject errorObj = new JSONObject();
                    errorObj.put("error", "Transaction failed, rolled back: " + e.getMessage());
                    responseArray.put(errorObj);
                } catch (SQLException rollbackEx) {
                    JSONObject errorObj = new JSONObject();
                    errorObj.put("error", "Failed to rollback transaction: " + rollbackEx.getMessage());
                    responseArray.put(errorObj);
                }
            }
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
    	
		String teamId = pathArray[1];
	      
        if (teamId == null) {
            Extra.sendError(response , out , "Venue ID is required");
            return;
        }

        String sql = "DELETE FROM venue WHERE venue_id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, Integer.parseInt(teamId));
            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
            	Extra.sendSuccess(response, out, "Venue is deleted successfully");
            } else {
            	Extra.sendError(response, out, "No Data Found");
            }
        } catch (NumberFormatException e) {
        	Extra.sendError(response, out, e.getMessage());
        } catch (SQLException e) {
        	Extra.sendError(response, out, e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Override
    protected void doPut(HttpServletRequest request , HttpServletResponse response) throws ServletException , IOException {
    	doPost(request, response);
	}

}

