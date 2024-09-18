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

@WebServlet("/teams/*")
public class Team extends HttpServlet {
	private static final long serialVersionUID = 1L;
    private static final String DB_URL = "jdbc:mysql://localhost:3306/CricketBuzz";
    private static final String USER = "root";
    private static final String PASS = "";
    
    public static void addData(PrintWriter out , ResultSet rs , JSONObject jsonObject)
    {
    	try {
    		jsonObject.put("team_id", rs.getInt("team_id"));
    		jsonObject.put("name", rs.getString("name"));
    		jsonObject.put("captain_id", rs.getInt("captain_id"));
    		jsonObject.put("vice_captain_id", rs.getInt("vice_captain_id"));
    		jsonObject.put("wicket_keeper_id", rs.getInt("wicket_keeper_id"));
    		jsonObject.put("category", rs.getString("category"));
    		
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
			String sql = "SELECT * FROM team";
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
		
		  String teamId = pathArray[1];
	      
	        if (teamId == null) {
	            Extra.sendError(response , out , "Team ID is required");
	            return;
	        }

        String query = "SELECT * FROM team WHERE team_id = ?";

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
        TeamModel teamModel = new Gson().fromJson(jsonString.toString(), TeamModel.class);
        
        String sql;
        
        if(teamModel.getTeamId() < 0 && teamModel.isValid())
        {
        	 sql = "INSERT INTO team (captain_id, vice_captain_id, wicket_keeper_id, category , name) "
                     + "VALUES (?, ?, ?, ? , ? )";
        }
        else if(teamModel.isValid())
        {
        	sql = "UPDATE team SET captain_id = ?, vice_captain_id = ?, wicket_keeper_id = ?, category = ? , name = ? "
                    + "WHERE team_id = ?";
        }
        else {
        	Extra.sendError(response, out , "Missing Parameters");
        	return;
		}
        
        
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

               pstmt.setInt(1, teamModel.getCaptainId());
               pstmt.setInt(2, teamModel.getViceCaptainId());
               pstmt.setInt(3, teamModel.getWicketKeeperId());
               pstmt.setString(4, teamModel.getCategory());
               pstmt.setString(5, teamModel.getName());
               
               if(teamModel.getTeamId() > 0)
            	   pstmt.setInt(6 , teamModel.getTeamId());
               
               int rowsAffected = pstmt.executeUpdate();
               if (rowsAffected > 0 && teamModel.getTeamId() > 0) {
            	   Extra.sendSuccess(response, out, "Player updated successfully");
               }
               else if(rowsAffected > 0)
               {
            	   Extra.sendSuccess(response, out, "Team inserted successfully");
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
            Extra.sendError(response , out , "Team ID is required");
            return;
        }

        String sql = "DELETE FROM team WHERE team_id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, Integer.parseInt(teamId));
            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
            	Extra.sendSuccess(response, out, "Team Deleted Successfully");
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
