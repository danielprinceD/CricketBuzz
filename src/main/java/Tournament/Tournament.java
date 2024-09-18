package Tournament;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jdt.internal.compiler.lookup.ImplicitNullAnnotationVerifier;
import org.json.JSONArray;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import Model.PlayerModel;
import Model.TeamModel;
import Model.TournamentModel;
import Team.Extra;

@WebServlet("/tournaments/*")
public class Tournament extends HttpServlet {
	private static final long serialVersionUID = 1L;
    private static final String DB_URL = "jdbc:mysql://localhost:3306/CricketBuzz";
    private static final String USER = "root";
    private static final String PASS = "";
    
    public static void addData(PrintWriter out , ResultSet rs , JSONObject jsonObject)
    {
    	try {
    		jsonObject.put("tour_id", rs.getInt("tour_id"));
            jsonObject.put("name", rs.getString("name"));
            jsonObject.put("start_date", rs.getDate("start_date"));
            jsonObject.put("end_date", rs.getDate("end_date"));
            jsonObject.put("match_category", rs.getString("match_category"));
            jsonObject.put("season", rs.getInt("season"));
    		
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
			String sql = "SELECT * FROM tournament";
			JSONArray playersArray = new JSONArray();
			
			try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
			         Statement stmt = conn.createStatement();
			         ResultSet rs = stmt.executeQuery(sql); ) {

			        while (rs.next()) {
			            JSONObject playerObject = new JSONObject();
			            addData( out , rs , playerObject);
			            playersArray.put(playerObject);
			        }


			        out.print(playersArray.toString());
			        out.flush();

			    } catch (SQLException e) {
			        e.printStackTrace();
			        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error: " + e.getMessage());
			    }
			
			return;
		}
		
		  String tourId = pathArray[1];
	      
	        if (tourId == null) {
	            Extra.sendError(response , out , "Tournament ID is required");
	            return;
	        }

        String query = "SELECT * FROM tournament WHERE tour_id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, Integer.parseInt(tourId));
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
        		JSONObject jsonObject = new JSONObject();
                addData(out, rs , jsonObject);
                out.print(jsonObject.toString());
                return;
            } else {
            	Extra.sendError(response, out, "No Team ID is found");
            }
        } catch (NumberFormatException e) {
        	Extra.sendError(response, out, "Invalid Team ID is found");
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
        TournamentModel tourModel = new Gson().fromJson(jsonString.toString(), TournamentModel.class);
        
        String sql;
        
        if(tourModel.getTourId() < 0 && tourModel.isValid())
        {
        	sql = "INSERT INTO tournament (name, start_date, end_date, match_category, season) "
                    + "VALUES (?, ?, ?, ?, ?)";
        }
        else if(tourModel.isValid())
        {
        	sql = "UPDATE tournament SET name = ?, start_date = ?, end_date = ?, match_category = ?, season = ? "
                    + "WHERE tour_id = ?";
        }
        else {
        	Extra.sendError(response, out , "Missing Parameters");
        	return;
		}
        
        
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

               pstmt.setString(1, tourModel.getName());
               pstmt.setString(2, tourModel.getStartDate());
               pstmt.setString(3, tourModel.getEndDate());
               pstmt.setString(4, tourModel.getMatchCategory());
               pstmt.setInt(5, tourModel.getSeason());
               
               if(tourModel.getTourId() > 0)
            	   pstmt.setInt(6 , tourModel.getTourId());
               
               int rowsAffected = pstmt.executeUpdate();
               if (rowsAffected > 0 && tourModel.getTourId() > 0) {
            	   Extra.sendSuccess(response, out, "Tournament updated successfully");
               }
               else if(rowsAffected > 0)
               {
            	   Extra.sendSuccess(response, out, "Tournament inserted successfully");
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
    	
		String tourId = pathArray[1];
	      
        if (tourId == null) {
            Extra.sendError(response , out , "Tournament ID is required");
            return;
        }

        String sql = "DELETE FROM tournament WHERE tour_id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, Integer.parseInt(tourId));
            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
            	Extra.sendSuccess(response, out, "Tournament Deleted Successfully");
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

