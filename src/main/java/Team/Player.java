package Team;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import Model.PlayerModel;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


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
		String[] pathArray = pathInfoString != null ? pathInfoString.split("/") : null;
		PrintWriter out = response.getWriter();

		if (pathArray == null || pathArray.length == 0) {
		    
			    StringBuilder sql = new StringBuilder("SELECT * FROM player WHERE 1=1"); 
			    List<String> filters = new ArrayList<>();

			    String role = request.getParameter("role");
			    String address = request.getParameter("address");
			    String gender = request.getParameter("gender");
			    String bowlingStyle = request.getParameter("bowling_style");
			    String name = request.getParameter("name");
			    String rating = request.getParameter("rating");
			    String battingStyle = request.getParameter("batting_style");

			    if (role != null) {
			        sql.append(" AND role = ?");
			        filters.add(role);
			    }
			    if (address != null) {
			        sql.append(" AND address = ?");
			        filters.add(address);
			    }
			    if (gender != null) {
			        sql.append(" AND gender = ?");
			        filters.add(gender);
			    }
			    if (bowlingStyle != null) {
			        sql.append(" AND bowling_style = ?");
			        filters.add(bowlingStyle);
			    }
			    if (name != null) {
			        sql.append(" AND name = ?");
			        filters.add(name);
			    }
			    if (rating != null) {
			        sql.append(" AND rating = ?");
			        filters.add(rating);
			    }
			    if (battingStyle != null) {
			        sql.append(" AND batting_style = ?");
			        filters.add(battingStyle);
			    }

			    try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
			         PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

			        for (int i = 0; i < filters.size(); i++) {
			            pstmt.setString(i + 1, filters.get(i));
			        }

			        ResultSet rs = pstmt.executeQuery();
			        JSONArray playersArray = new JSONArray();

			        while (rs.next()) {
			            JSONObject playerObject = new JSONObject();
			            playerObject.put("role", rs.getString("role"));
			            playerObject.put("address", rs.getString("address"));
			            playerObject.put("gender", rs.getString("gender"));
			            playerObject.put("bowling_style", rs.getString("bowling_style"));
			            playerObject.put("name", rs.getString("name"));
			            playerObject.put("rating", rs.getInt("rating"));
			            playerObject.put("batting_style", rs.getString("batting_style"));
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

	    java.lang.reflect.Type listType = new TypeToken<List<PlayerModel>>() {}.getType();
	    List<PlayerModel> playerModels = new Gson().fromJson(jsonString.toString(), listType);

	    if (playerModels == null || playerModels.isEmpty()) {
	        Extra.sendError(response, out, "No player data provided.");
	        return;
	    }

	    Connection conn = null;
	    PreparedStatement pstmt = null;
	    String sql = null;

	    try {
	        conn = DriverManager.getConnection(DB_URL, USER, PASS);
	        conn.setAutoCommit(false);

	        for (PlayerModel playerModel : playerModels) {
	            if (playerModel.isValid()) {
	                if (playerModel.getId() < 0) {
	                    // Insert new player
	                    sql = "INSERT INTO player (name, role, address, gender, rating, batting_style, bowling_style) "
	                            + "VALUES (?, ?, ?, ?, ?, ?, ?)";
	                    pstmt = conn.prepareStatement(sql);
	                    pstmt.setString(1, playerModel.getName());
	                    pstmt.setString(2, playerModel.getRole());
	                    pstmt.setString(3, playerModel.getAddress());
	                    pstmt.setString(4, playerModel.getGender());
	                    pstmt.setDouble(5, playerModel.getRating());
	                    pstmt.setString(6, playerModel.getBattingStyle());
	                    pstmt.setString(7, playerModel.getBowlingStyle());
	                } else {
	                    // Update existing player
	                    sql = "UPDATE player SET name = ?, role = ?, address = ?, gender = ?, rating = ?, "
	                            + "batting_style = ?, bowling_style = ? WHERE id = ?";
	                    pstmt = conn.prepareStatement(sql);
	                    pstmt.setString(1, playerModel.getName());
	                    pstmt.setString(2, playerModel.getRole());
	                    pstmt.setString(3, playerModel.getAddress());
	                    pstmt.setString(4, playerModel.getGender());
	                    pstmt.setDouble(5, playerModel.getRating());
	                    pstmt.setString(6, playerModel.getBattingStyle());
	                    pstmt.setString(7, playerModel.getBowlingStyle());
	                    pstmt.setInt(8, playerModel.getId());
	                }

	                int rowsAffected = pstmt.executeUpdate();

	                if (rowsAffected <= 0) {
	                    Extra.sendError(response, out, "Failed to process player data.");
	                    conn.rollback();
	                    return;
	                }
	            } else {
	                Extra.sendError(response, out, "Invalid player data.");
	                conn.rollback();
	                return;
	            }
	        }

	        conn.commit();
	        Extra.sendSuccess(response, out, "Players processed successfully.");

	    } catch (NumberFormatException e) {
	        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
	        out.println("Invalid player_id format.");
	    } catch (SQLException e) {
	        if (conn != null) {
	            try {
	                conn.rollback();
	            } catch (SQLException rollbackEx) {
	                rollbackEx.printStackTrace();
	            }
	        }
	        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	        out.println("Database error: " + e.getMessage());
	        e.printStackTrace();
	    } finally {
	        if (pstmt != null) {
	            try {
	                pstmt.close();
	            } catch (SQLException e) {
	                e.printStackTrace();
	            }
	        }
	        if (conn != null) {
	            try {
	                conn.close();
	            } catch (SQLException e) {
	                e.printStackTrace();
	            }
	        }
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
    
    @Override 
    protected void doPut(HttpServletRequest request , HttpServletResponse response) throws ServletException , IOException
    {
    	doPost(request, response);
    	return;
    }

}
