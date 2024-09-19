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
import java.util.HashSet;
import java.util.Set;

import javax.security.auth.message.callback.PrivateKeyCallback.Request;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sound.midi.SysexMessage;

import org.json.JSONArray;
import org.json.JSONObject;
import org.omg.CORBA.PRIVATE_MEMBER;

import com.fasterxml.jackson.annotation.ObjectIdGenerators.StringIdGenerator;
import com.google.gson.Gson;
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
			
			JSONArray teamsArray = new JSONArray();
			
			try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
			         Statement stmt = conn.createStatement();
			         ResultSet rs = stmt.executeQuery(sql); ) {

			        while (rs.next()) {
			            JSONObject teamObject = new JSONObject();
			            addData( out , rs , teamObject);
			            
			            Integer teamId = rs.getInt("team_id");
			            
			            Connection personConnection = null;
			            PreparedStatement personPreparedStatement = null;
			            
			            try {
			                personConnection = DriverManager.getConnection(DB_URL, USER, PASS);
			                
			                String sqlString = "SELECT TP.player_id, P.name AS PlayerName, P.role AS PlayerRole, P.rating AS PlayerRating "
		                             + "FROM team_player AS TP "
		                             + "JOIN player AS P ON TP.player_id = P.id "
		                             + "WHERE TP.team_id = ?";
			                                 
			                personPreparedStatement = personConnection.prepareStatement(sqlString);
			                personPreparedStatement.setInt(1, teamId);
			                
			                JSONArray playerArray = new JSONArray();
			                ResultSet personRS = personPreparedStatement.executeQuery();
			                
			                while (personRS.next()) {
			                	
			                    int playerId = personRS.getInt("player_id");
			                    String playerName = personRS.getString("PlayerName");
			                    String playerRole = personRS.getString("PlayerRole");
			                    int playerRating = personRS.getInt("PlayerRating");

			                    JSONObject playerObject = new JSONObject();
			                    playerObject.put("player_id", playerId);
			                    playerObject.put("name", playerName);
			                    playerObject.put("role", playerRole);
			                    playerObject.put("rating", playerRating);
			                    playerArray.put(playerObject);
			                }
			                teamObject.put("team_players", playerArray);
			                
			            } catch (SQLException e) {
			                e.printStackTrace();
			            } finally {
			                try {
			                    if (personPreparedStatement != null) personPreparedStatement.close();
			                    if (personConnection != null) personConnection.close();
			                } catch (SQLException e) {
			                    e.printStackTrace();
			                }
			            }
			            
			            teamsArray.put(teamObject);
			        }
			        out.print(teamsArray.toString());
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
                
                Connection personConnection = null;
	            PreparedStatement personPreparedStatement = null;
                
                try {
	                personConnection = DriverManager.getConnection(DB_URL, USER, PASS);
	                
	                String sqlString = "SELECT TP.player_id, P.name AS PlayerName, P.role AS PlayerRole, P.rating AS PlayerRating "
                             + "FROM team_player AS TP "
                             + "JOIN player AS P ON TP.player_id = P.id "
                             + "WHERE TP.team_id = ?";
	                                 
	                personPreparedStatement = personConnection.prepareStatement(sqlString);
	                personPreparedStatement.setInt(1, Integer.parseInt(teamId));
	                
	                JSONArray playerArray = new JSONArray();
	                ResultSet personRS = personPreparedStatement.executeQuery();
	                
	                while (personRS.next()) {
	                	
	                    int playerId = personRS.getInt("player_id");
	                    String playerName = personRS.getString("PlayerName");
	                    String playerRole = personRS.getString("PlayerRole");
	                    int playerRating = personRS.getInt("PlayerRating");

	                    JSONObject playerObject = new JSONObject();
	                    playerObject.put("player_id", playerId);
	                    playerObject.put("name", playerName);
	                    playerObject.put("role", playerRole);
	                    playerObject.put("rating", playerRating);
	                    playerArray.put(playerObject);
	                }
	                jsonObject.put("team_players", playerArray);
	                
	            } catch (SQLException e) {
	                e.printStackTrace();
	            } finally {
	                try {
	                    if (personPreparedStatement != null) personPreparedStatement.close();
	                    if (personConnection != null) personConnection.close();
	                } catch (SQLException e) {
	                    e.printStackTrace();
	                }
	            }
                
                
                
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
    
    
    public void addPlayersToTeam(HttpServletRequest request ,  HttpServletResponse response , PrintWriter out , String teamId){
    	
    		
        	String query = "INSERT INTO team_player (player_id, team_id) VALUES (?, ?)";
        	
        	Set<Integer> playerSet = new HashSet<>();
        	
        	String playerParam = request.getParameter("playersList");
        	
            Connection conn = null;
            try {
            	
            	for(String it : playerParam.split(","))
            		playerSet.add(Integer.parseInt(it));
            	
            	conn = DriverManager.getConnection(DB_URL, USER, PASS);
            	conn.setAutoCommit(false);
            	PreparedStatement playerPstmt = conn.prepareStatement(query);
                Integer team_id = Integer.parseInt(teamId);
                
            	for (Integer playerId : playerSet) {
                    playerPstmt.setInt(1, playerId);
                    playerPstmt.setInt(2, team_id);
                    playerPstmt.executeUpdate();
                }
            	conn.commit();
            	out.print("Players are added to team");
            }
            catch (Exception e) {
            	if(conn != null)
					try {
						conn.rollback();
					} catch (SQLException e1) {
						e1.printStackTrace();
					}
            	e.printStackTrace();
            	out.print("Error " + e.getMessage());
			}
    }
    

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {

        PrintWriter out = response.getWriter();
        StringBuilder jsonString = new StringBuilder();
        BufferedReader reader = request.getReader();
        String line;

        while((line = reader.readLine()) != null) {
            jsonString.append(line);
        }
        
        String playerString = request.getParameter("playersList");
        
        
        TeamModel teamModel = new Gson().fromJson(jsonString.toString(), TeamModel.class);
        Set<Integer> playerSet = new HashSet<>();
        if(request.getMethod().equalsIgnoreCase("POST")) {
        String[] playerArr = playerString.split(",");
        
        for(String itString : playerArr)
        
        playerSet.add(Integer.parseInt(itString));

        playerSet.add(teamModel.getCaptainId());
        playerSet.add(teamModel.getViceCaptainId());
        playerSet.add(teamModel.getWicketKeeperId());
        }
        
        
        
        
        String pathInfoString = request.getPathInfo();        
        String[] pathArray = pathInfoString != null ?  pathInfoString.split("/") : null;

        if (pathArray == null || pathArray.length <= 0) {
            teamModel.setTeamId(-1); 
        } else {
            try {
                Integer idInteger = Integer.parseInt(pathArray[1]);
                teamModel.setTeamId(idInteger);
            } catch (Exception e) {
                Extra.sendError(response , out , "Enter Valid ID");
                e.printStackTrace();
                return;
            }
        }

        String sql;
        if (teamModel.getTeamId() < 0 && teamModel.isValid()) {
            sql = "INSERT INTO team (captain_id, vice_captain_id, wicket_keeper_id, category, name) "
                  + "VALUES (?, ?, ?, ?, ?)";
        } else if (teamModel.isValid() && request.getMethod().equalsIgnoreCase("PUT")) {
            sql = "UPDATE team SET captain_id = ?, vice_captain_id = ?, wicket_keeper_id = ?, category = ?, name = ? "
                  + "WHERE team_id = ?";
        } else if(pathArray !=null && pathArray.length >= 2 ){
        	
        	addPlayersToTeam( request , response , out , pathArray[1]);
            return;
        }
        else {
        	Extra.sendError(response, out, "Missing Parameters");
            return;
        }

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet generatedKeys = null;

        try {
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
            conn.setAutoCommit(false);

            pstmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            pstmt.setInt(1, teamModel.getCaptainId());
            pstmt.setInt(2, teamModel.getViceCaptainId());
            pstmt.setInt(3, teamModel.getWicketKeeperId());
            pstmt.setString(4, teamModel.getCategory());
            pstmt.setString(5, teamModel.getName());

            if (teamModel.getTeamId() > 0) {
                pstmt.setInt(6, teamModel.getTeamId()); 
            }

            int rowsAffected = pstmt.executeUpdate();

            int teamId = teamModel.getTeamId();
            if (teamId < 0) {
                generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    teamId = generatedKeys.getInt(1); 
                }
            }

            if (teamId > 0 && request.getMethod().equalsIgnoreCase("POST")) {
            	String query = "INSERT INTO team_player (player_id, team_id) VALUES (?, ?)";

                	
                try (PreparedStatement playerPstmt = conn.prepareStatement(query)) {
                    for (Integer playerId : playerSet) {
                        playerPstmt.setInt(1, playerId);
                        playerPstmt.setInt(2, teamId);
                        playerPstmt.executeUpdate();
                    }
                }
            }

            if (rowsAffected > 0) {
                conn.commit();
                Extra.sendSuccess(response, out, "Team and players inserted/updated successfully");
            } else {
                conn.rollback();
                Extra.sendError(response, out, "Failed to insert/update team");
            }

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            Extra.sendError(response, out, e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (generatedKeys != null) generatedKeys.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {

    	
    	String pathInfoString = request.getPathInfo();		
		String[] pathArray = pathInfoString != null ?  pathInfoString.split("/") : null;
		PrintWriter out = response.getWriter();
    	
		if(pathArray.length == 1)
		{
			Extra.sendError(response, out, "Enter a Valid Endpoint");
			return;
		}
		else if(pathArray.length == 2)
		{
			deleteOneTeam(response , out , pathArray[1]);
		}
		else if(pathArray.length == 3)
		{
			if(pathArray[2].equalsIgnoreCase("players"))
				deleteAllPlayersFromTeam(response , out , pathArray[1]);
		}
		else if(pathArray.length == 4)
		{
			if(pathArray[2].equalsIgnoreCase("players"))
				deleteOnePlayerFromTeam(response , out , pathArray[1] , pathArray[3]);
		}
		
    }
    
    private Connection connection = null;
    private PreparedStatement preparedStatement = null;
    private Statement statement = null;
    private ResultSet resultSet = null;
    
    private void deleteOnePlayerFromTeam(HttpServletResponse response , PrintWriter out , String teamId , String playerId) {
    	
    	String query = "DELETE FROM team_player where team_id = ? AND player_id = ?";
    	
    	try {
            connection = DriverManager.getConnection(DB_URL, USER, PASS);
            
            preparedStatement = connection.prepareStatement(query);
            
            preparedStatement.setInt(1, Integer.parseInt(teamId));
            preparedStatement.setInt(2, Integer.parseInt(playerId));
            
            int rowsAffected = preparedStatement.executeUpdate();
            
            if (rowsAffected > 0) {
                out.println("Player with ID " + playerId + " has been removed from team " + teamId);
            } else {
            	
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.println("No matching player found for deletion.");
            }
        } catch (SQLException e) {
        	
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.println("Database error: " + e.getMessage());
            e.printStackTrace();
        } catch (NumberFormatException e) {
        	
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.println("Invalid team or player ID format.");
        } finally {
        	
            try {
                if (preparedStatement != null) preparedStatement.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    	
    }
    
    private void deleteAllPlayersFromTeam(HttpServletResponse response , PrintWriter out , String teamId) {
    	
    	String query = "DELETE FROM team_player where team_id = ?";
    	
    	try {
            // Establish a connection to the database
            connection = DriverManager.getConnection(DB_URL, USER, PASS);
            
            // Prepare the SQL query
            preparedStatement = connection.prepareStatement(query);
            
            // Set the parameters for the team_id and player_id
            preparedStatement.setInt(1, Integer.parseInt(teamId));
            
            // Execute the DELETE query
            int rowsAffected = preparedStatement.executeUpdate();
            
            if (rowsAffected > 0) {
                out.println("Players has been removed from team ");
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.println("No matching player found for deletion.");
            }
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.println("Database error: " + e.getMessage());
            e.printStackTrace();
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.println("Invalid team or player ID format.");
        } finally {
            try {
                if (preparedStatement != null) preparedStatement.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    	
    }
    
    private void deleteOneTeam(HttpServletResponse response , PrintWriter out , String teamId) {
    	
    	String query = "DELETE FROM team where team_id = ?";
    	
    	try {
            connection = DriverManager.getConnection(DB_URL, USER, PASS);
            
            preparedStatement = connection.prepareStatement(query);
            
            preparedStatement.setInt(1, Integer.parseInt(teamId));
            
            int rowsAffected = preparedStatement.executeUpdate();
            
            if (rowsAffected > 0) {
                out.println("Team has been removed");
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.println("No matching player found for deletion.");
            }
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.println("Database error: " + e.getMessage());
            e.printStackTrace();
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.println("Invalid team or player ID format.");
        } finally {
            try {
                if (preparedStatement != null) preparedStatement.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    	
    }
    
    
    
    @Override
    protected void doPut(HttpServletRequest request , HttpServletResponse response) throws ServletException , IOException{
    	doPost(request, response);
    }
}
