package Team;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

import javax.security.auth.message.MessageInfo;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sound.sampled.Line;

import org.json.JSONArray;
import org.json.JSONObject;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mysql.cj.protocol.a.NativeConstants.StringLengthDataType;

import Model.PlayerModel;

import java.sql.*;
import Team.Extra;

@WebServlet("/players/*")
public class Player extends HttpServlet {

	private static final String DB_URL = "jdbc:mysql://localhost:3306/CricketBuzz";
	private static final String USER = "root";
	private static final String PASS = "";
	
	protected void addData(JSONObject playerObject , ResultSet rs) {
		
		try {
			
		playerObject.put("id", rs.getInt("id"));
        playerObject.put("name", rs.getString("name"));
        playerObject.put("role", rs.getString("role"));
        playerObject.put("address", rs.getString("address"));
        playerObject.put("gender", rs.getString("gender"));
        playerObject.put("rating", rs.getInt("rating"));
        playerObject.put("batting_style", rs.getString("batting_style"));
        playerObject.put("bowling_style", rs.getString("bowling_style"));
        
		}
		catch (Exception e) {
			e.printStackTrace();
			
		}
	}
	
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		response.setContentType("application/json");
		String pathInfoString = request.getPathInfo();		
		String[] pathArray = pathInfoString != null ?  pathInfoString.split("/") : null;
		PrintWriter out = response.getWriter();
		
		if( pathArray == null || pathArray.length == 0 )
		{
			String sql = "SELECT * FROM player";
			JSONArray playersArray = new JSONArray();
			
			try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
			         Statement stmt = conn.createStatement();
			         ResultSet rs = stmt.executeQuery(sql); ) {

			        while (rs.next()) {
			            JSONObject playerObject = new JSONObject();
			            addData(playerObject, rs);
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
		
        String playerId = pathArray[1];
      
        if (playerId == null) {
            Extra.sendError(response , out , "Player id is not found");
            return;
        }

        String query = "SELECT * FROM player WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, Integer.parseInt(playerId));
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
            	JSONObject jsonObject = new JSONObject();
            	addData(jsonObject, rs);
                out.print(jsonObject.toString());

            } else {
            Extra.sendError(response, out, "Player Not Found");
            }
        } catch (Exception e) {
            e.printStackTrace();
            
			Extra.sendError(response, out ,e.getMessage().toString());
            return;
        }
	}


    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
    	
    	StringBuilder jsonString = new StringBuilder();
        BufferedReader reader = request.getReader();
        String line;
        while((line = reader.readLine()) != null)
        		jsonString.append(line);
        
        PrintWriter out = response.getWriter();
        PlayerModel playerModel = new Gson().fromJson(jsonString.toString(), PlayerModel.class);
        
        String sql;
        
        if(playerModel.getId() < 0 && playerModel.isValid())
        {
        	sql = "INSERT INTO player (name, role, address, gender, rating, batting_style, bowling_style) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        }
        else if(playerModel.isValid())
        {
        	sql = "UPDATE player SET name = ?, role = ?, address = ?, gender = ?, rating = ?, "
                    + "batting_style = ?, bowling_style = ? WHERE id = ?";
        }
        else {
        	Extra.sendError(response, out , "Missing Parameters");
        	return;
		}
        
        
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

               pstmt.setString(1, playerModel.getName());
               pstmt.setString(2, playerModel.getRole());
               pstmt.setString(3, playerModel.getAddress());
               pstmt.setString(4, playerModel.getGender());
               pstmt.setDouble(5, playerModel.getRating());
               pstmt.setString(6, playerModel.getBattingStyle());
               pstmt.setString(7, playerModel.getBowlingStyle());
               
               if(playerModel.getId() > 0)
            	   pstmt.setInt(8 , playerModel.getId());

               int rowsAffected = pstmt.executeUpdate();
               if (rowsAffected > 0 && playerModel.getId() > 0) {
            	   Extra.sendSuccess(response, out, "Player Updated successfully");
               }
               else if(rowsAffected > 0)
               {
            	   Extra.sendSuccess(response, out, "Player inserted successfully");
               }
               else {
                   Extra.sendError(response , out , "No player found with the provided ID.");
               }
           } catch (NumberFormatException e) {
               response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
               out.println("Invalid player_id format.");
           } catch (SQLException e) {
               response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
               out.println("Database error: " + e.getMessage());
               e.printStackTrace();
           }
        
        
        
    }
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {

        response.setContentType("text/plain");
        PrintWriter out = response.getWriter();
        
        String pathInfoString = request.getPathInfo();		
		String[] pathArray = pathInfoString != null ?  pathInfoString.split("/") : null;
		
		if(pathArray == null || pathArray.length == 0)
		{
			Extra.sendError(response, out, "No ID is mentioned");
			return;
		}
		
		String sql = "DELETE FROM player where id = ?";
		
		
		try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
                PreparedStatement pstmt = conn.prepareStatement(sql); 
				) {
			Integer playerId = Integer.parseInt(pathArray[1]);
			pstmt.setInt(1, playerId);
			int affected = pstmt.executeUpdate();
			
			if(affected > 0)
			Extra.sendError(response, out,"Deleted Successfully");
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
