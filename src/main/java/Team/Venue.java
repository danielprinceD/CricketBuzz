package Team;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import com.google.gson.Gson;
import Model.VenueModel;

@WebServlet("/venues/*")
public class Venue extends HttpServlet {
	private static final long serialVersionUID = 1L;
    private static final String DB_URL = "jdbc:mysql://localhost:3306/CricketBuzz";
    private static final String USER = "root";
    private static final String PASS = "";
    
    public static void addData(PrintWriter out , ResultSet rs , JSONObject jsonObject)
    {
    	try {

    		jsonObject.put("venue_id", rs.getInt("venue_id"));
            jsonObject.put("stadium", rs.getString("stadium"));
            jsonObject.put("location", rs.getString("location"));
            jsonObject.put("pitch_condition", rs.getString("pitch_condition"));
            jsonObject.put("description", rs.getString("description"));
            jsonObject.put("capacity", rs.getLong("capacity"));
            jsonObject.put("curator", rs.getString("curator"));
    		
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        
		String pathInfoString = request.getPathInfo();		
		String[] pathArray = pathInfoString != null ?  pathInfoString.split("/") : null;
		PrintWriter out = response.getWriter();
		
		if( pathArray == null || pathArray.length == 0 )
		{
			String sql = "SELECT * FROM venue";
			JSONArray venueArray = new JSONArray();
			
			try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
			         Statement stmt = conn.createStatement();
			         ResultSet rs = stmt.executeQuery(sql); ) {

			        while (rs.next()) {
			            JSONObject venueObject = new JSONObject();
			            addData( out , rs , venueObject);
			            venueArray.put(venueObject);
			        }


			        out.print(venueArray.toString());
			        out.flush();

			    } catch (SQLException e) {
			        e.printStackTrace();
			        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error: " + e.getMessage());
			    }
			
			return;
		}
		
		  String teamId = pathArray[1];
	      
	        if (teamId == null) {
	            Extra.sendError(response , out , "Team ID is required");
	            return;
	        }

        String query = "SELECT * FROM venue WHERE venue_id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, Integer.parseInt(teamId));
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
        		JSONObject jsonObject = new JSONObject();
                addData(out, rs , jsonObject);
                out.print(jsonObject.toString());
                return;
            } else {
            	Extra.sendError(response, out, "No Venue ID is found");
            }
        } catch (NumberFormatException e) {
        	Extra.sendError(response, out, "Invalid Venue ID is found");
        } catch (SQLException e) {
        	Extra.sendError(response, out, "Error Fetching Data");
            e.printStackTrace();
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {

    	StringBuilder jsonString = new StringBuilder();
        BufferedReader reader = request.getReader();
        String line;
        
        while((line = reader.readLine()) != null)
        		jsonString.append(line);
        
        PrintWriter out = response.getWriter();
        VenueModel venueModel = new Gson().fromJson(jsonString.toString(), VenueModel.class);
        
        String pathInfoString = request.getPathInfo();		
		String[] pathArray = pathInfoString != null ?  pathInfoString.split("/") : null;
		
		if(pathArray == null || pathArray.length <= 0)
		{
			venueModel.setVenueId(-1);
		}
		else { 
			try{
				Integer idInteger = Integer.parseInt(pathArray[1]);
				venueModel.setVenueId(idInteger);
			}
			catch (Exception e) {
				Extra.sendError(response , out , "Enter Valid ID");
				e.printStackTrace();
				return;
				
			}
		}
        
        String sql;
        
        if(venueModel.getVenueId() < 0 && venueModel.isValid())
        {
        	sql = "INSERT INTO venue (stadium, location, pitch_condition, description, capacity, curator) VALUES (?, ?, ?, ?, ?, ?)";;
        }
        else if(venueModel.isValid())
        {
        	sql = "UPDATE venue SET stadium = ?, location = ?, pitch_condition = ?, description = ?, capacity = ?, curator = ? WHERE venue_id = ?";
        }
        else {
        	Extra.sendError(response, out , "Missing Parameters");
        	return;
		}
        
        
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

               pstmt.setString(1, venueModel.getStadium());
               pstmt.setString(2, venueModel.getLocation());
               pstmt.setString(3, venueModel.getPitchCondition());
               pstmt.setString(4, venueModel.getDescription());
               pstmt.setLong(5, venueModel.getCapacity());
               pstmt.setString(6, venueModel.getCurator());
               if(venueModel.getVenueId() > 0)
            	   pstmt.setInt(7 , venueModel.getVenueId());
               
               int rowsAffected = pstmt.executeUpdate();
               if (rowsAffected > 0 && venueModel.getVenueId() > 0) {
            	   Extra.sendSuccess(response, out, "Venue updated successfully");
               }
               else if(rowsAffected > 0)
               {
            	   Extra.sendSuccess(response, out, "Venue inserted successfully");
               }
               else {
                  Extra.sendError(response, out, "No ID Found");
               }
           } catch (NumberFormatException e) {
        	   Extra.sendError(response, out, e.getMessage());
           } catch (SQLException e) {
               Extra.sendError(response, out, e.getMessage());
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
}

