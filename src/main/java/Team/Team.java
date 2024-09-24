package Team;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import Model.TeamModel;

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
    
    private JSONArray getPlayersForTeam(Integer teamId) {
        JSONArray playerArray = new JSONArray();
        String sqlString = "SELECT TP.player_id "
                + "FROM team_player AS TP "
                + "JOIN player AS P ON TP.player_id = P.id "
                + "WHERE TP.team_id = ?";

        try (Connection personConnection = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement personPreparedStatement = personConnection.prepareStatement(sqlString)) {

            personPreparedStatement.setInt(1, teamId);
            ResultSet personRS = personPreparedStatement.executeQuery();

            while (personRS.next()) {
            	playerArray.put(personRS.getInt("player_id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return playerArray;
    }
    
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
    	
    	response.setContentType("application/json");
        
		String pathInfoString = request.getPathInfo();		
		String[] pathArray = pathInfoString != null ?  pathInfoString.split("/") : null;
		PrintWriter out = response.getWriter();
		
		if(pathArray != null && pathArray.length > 2)
			Extra.sendError(response, out, "Enter a Valid Path");
		
	    StringBuilder sql = new StringBuilder("SELECT * FROM team ");
	    List<Object> parameters = new ArrayList<>();
	
	    
	    String wicketKeeperId = request.getParameter("wicket_keeper_id");
	    String viceCaptainId = request.getParameter("vice_captain_id");
	    String captainId = request.getParameter("captain_id");
	    String name = request.getParameter("name");
	    String teamId = request.getParameter("team_id");
	    String category = request.getParameter("category");
	    
	    boolean whereAdded = false;
	    
	    if (pathArray != null && pathArray.length == 2) {
	        if (!whereAdded) {
	            sql.append(" WHERE team_id = ?");
	            whereAdded = true;
	        } else {
	            sql.append(" AND team_id = ?");
	        }
	        try {
	        	parameters.add(Integer.parseInt(pathArray[1]));
				
			} catch (Exception e) {
				Extra.sendError(response, out, "Enter Valid a ID");
			}
	    }
	    
	    if (wicketKeeperId != null) {
	        if (!whereAdded) {
	            sql.append(" WHERE wicket_keeper_id = ?");
	            whereAdded = true;
	        } else {
	            sql.append(" AND wicket_keeper_id = ?");
	        }
	        parameters.add(Integer.parseInt(wicketKeeperId));
	    }

	    if (viceCaptainId != null) {
	        if (!whereAdded) {
	            sql.append(" WHERE vice_captain_id = ?");
	            whereAdded = true;
	        } else {
	            sql.append(" AND vice_captain_id = ?");
	        }
	        parameters.add(Integer.parseInt(viceCaptainId));
	    }

	    if (captainId != null) {
	        if (!whereAdded) {
	            sql.append(" WHERE captain_id = ?");
	            whereAdded = true;
	        } else {
	            sql.append(" AND captain_id = ?");
	        }
	        parameters.add(Integer.parseInt(captainId));
	    }

	    if (name != null) {
	        if (!whereAdded) {
	            sql.append(" WHERE name = ?");
	            whereAdded = true;
	        } else {
	            sql.append(" AND name = ?");
	        }
	        parameters.add(name);
	    }

	    if (teamId != null) {
	        if (!whereAdded) {
	            sql.append(" WHERE team_id = ?");
	            whereAdded = true;
	        } else {
	            sql.append(" AND team_id = ?");
	        }
	        parameters.add(Integer.parseInt(teamId));
	    }

	    if (category != null) {
	        if (!whereAdded) {
	            sql.append(" WHERE category = ?");
	            whereAdded = true;
	        } else {
	            sql.append(" AND category = ?");
	        }
	        parameters.add(category);
	    }
	
	    JSONArray teamsArray = new JSONArray();
	
	    try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
	         PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
	
	        for (int i = 0; i < parameters.size(); i++) {
	            pstmt.setObject(i + 1, parameters.get(i));
	        }
	
	        ResultSet rs = pstmt.executeQuery();
	
	        while (rs.next()) {
	            JSONObject teamObject = new JSONObject();
	            addData(out, rs, teamObject);
	
	            Integer teamIdValue = rs.getInt("team_id");
	
	            JSONArray playerArray = getPlayersForTeam(teamIdValue);
	            teamObject.put("team_players", playerArray);
	
	            teamsArray.put(teamObject);
	        }
	
	        out.print(teamsArray.toString());
	        out.flush();
	
	    } catch (SQLException e) {
	        e.printStackTrace();
	        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error: " + e.getMessage());
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
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {

    	
    	String pathInfoString = request.getPathInfo();		
		String[] pathArray = pathInfoString != null ?  pathInfoString.split("/") : null;
		PrintWriter out = response.getWriter();
    	
		String playerId = request.getParameter("player_id");
		
		if(pathArray == null || pathArray.length == 1 || pathArray.length > 4)
		{
			Extra.sendError(response, out, "Enter a Valid Endpoint");
			return;
		}
		else if(pathArray.length == 2)
		{
			deleteOneTeam(response , out , pathArray[1]);
		}
		else if(playerId != null)
		{
				deleteOnePlayerFromTeam(response , out , pathArray[1] , playerId);
		}
		else {
			Extra.sendError(response, out, "Enter Valid Path");
		}
//		else if(pathArray.length == 3)
//		{
//			if(pathArray[2].equalsIgnoreCase("players"))
//				deleteAllPlayersFromTeam(response , out , pathArray[1]);
//		}
		
		
    }
    
    private Connection connection = null;
    private PreparedStatement preparedStatement = null;
    
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {

        PrintWriter out = response.getWriter();
        StringBuilder jsonString = new StringBuilder();
        BufferedReader reader = request.getReader();
        String line;

        while ((line = reader.readLine()) != null) {
            jsonString.append(line);
        }

        Type teamListType = new TypeToken<List<TeamModel>>() {}.getType();
        List<TeamModel> teams = new Gson().fromJson(jsonString.toString(), teamListType);

        
        
        for (TeamModel teamModel : teams) {
            Set<Integer> playerSet = new HashSet<>();
            
            if (!validatePlayers(teamModel, playerSet, response, out)) return;
           
            String sql = prepareSqlStatement(request, teamModel, response, out);
            if (sql == null) return;

            try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
                 PreparedStatement pstmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            	
                conn.setAutoCommit(false);
                setPreparedStatementValues(pstmt, teamModel);

                int rowsAffected = pstmt.executeUpdate();
                int teamId = teamModel.getTeamId();
          
                if (teamId < 0 && request.getMethod().equalsIgnoreCase("POST")) {
                    teamId = getGeneratedTeamId(pstmt);
                }

                if (teamId > 0 ){
                    addPlayersToTeam(conn, playerSet, teamId);
                }

                if (rowsAffected > 0) {
                    conn.commit();
                    Extra.sendSuccess(response, out, "Team and players inserted/updated successfully");
                } else {
                    conn.rollback();
                    Extra.sendError(response, out, "Failed to insert/update team");
                }

            } catch (SQLException e) {
                Extra.sendError(response, out, e.getMessage());
                e.printStackTrace();
            }
        }   
    }
    
    private boolean validPlayer(int playerId) {
        String sql = "SELECT COUNT(*) FROM player WHERE id = ?";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, playerId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt(1);
                    return count > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); 
        }
        
        return false;
    }


    private boolean validatePlayers(TeamModel teamModel, Set<Integer> playerSet, HttpServletResponse response, PrintWriter out) {
        
    		
    	
    	for (Integer player : teamModel.team_players) {
            if (playerSet.contains(player)) {
                Extra.sendError(response, out, "Player cannot be added more than once");
                return false;
            }
            playerSet.add(player);
        }

        if (playerSet.size() < 11) {
            Extra.sendError(response, out, "Add more than 11 Players. Add " + (11 - playerSet.size()) + " more players");
            return false;
        }
        
        
        if (!playerSet.contains(teamModel.getCaptainId())) {
            Extra.sendError(response, out, "Captain ID is not in Players List");
            return false;
        }
        if (!playerSet.contains(teamModel.getWicketKeeperId())) {
            Extra.sendError(response, out, "WicketKeeper ID is not in Players List");
            return false;
        }
        if (!playerSet.contains(teamModel.getViceCaptainId())) {
            Extra.sendError(response, out, "Vice Captain ID is not in Players List");
            return false;
        }
        if (teamModel.getCaptainId().equals(teamModel.getViceCaptainId())) {
            Extra.sendError(response, out, "Captain and Vice Captain Cannot be Same");
            return false;
        }

        if(!validPlayer(teamModel.getCaptainId()))
        {
        	 Extra.sendError(response, out, "Captain ID " + teamModel.getCaptainId() + " is not a player");
        	 return false;
        }
        
        if(!validPlayer(teamModel.getWicketKeeperId()))
        {
        	 Extra.sendError(response, out,"Wicket-Keeper ID " + teamModel.getWicketKeeperId() + " is not a player");
        	 return false;
        }
        if(!validPlayer(teamModel.getViceCaptainId()))
        {
        	 Extra.sendError(response, out, "Vice-Captain ID " + teamModel.getViceCaptainId() + " is not a player");
        	 return false;
        }
        	
        return true;
    }

    

    private String prepareSqlStatement(HttpServletRequest request, TeamModel teamModel, HttpServletResponse response, PrintWriter out) {
      
    	if (teamModel.getTeamId() < 0 && teamModel.isValid()) {
            return "INSERT INTO team (captain_id, vice_captain_id, wicket_keeper_id, category, name) VALUES (?, ?, ?, ?, ?)";
        } else if (teamModel.isValid() && request.getMethod().equalsIgnoreCase("PUT")) {
            return "UPDATE team SET captain_id = ?, vice_captain_id = ?, wicket_keeper_id = ?, category = ?, name = ? WHERE team_id = ?";
        } else if (request.getPathInfo() != null) {
            addPlayersToTeam(request, response, out, request.getPathInfo());
            return null;
        } else {
            Extra.sendError(response, out, "Missing Parameters");
            return null;
        }
    }

    private void setPreparedStatementValues(PreparedStatement pstmt, TeamModel teamModel) throws SQLException {
        pstmt.setInt(1, teamModel.getCaptainId());
        pstmt.setInt(2, teamModel.getViceCaptainId());
        pstmt.setInt(3, teamModel.getWicketKeeperId());
        pstmt.setString(4, teamModel.getCategory());
        pstmt.setString(5, teamModel.getName());

        if (teamModel.getTeamId() > 0) {
            pstmt.setInt(6, teamModel.getTeamId());
        }
    }

    private int getGeneratedTeamId(PreparedStatement pstmt) throws SQLException {
        try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
            if (generatedKeys.next()) {
                return generatedKeys.getInt(1);
            }
        }
        return -1;
    }
    
    private boolean isValidTeamPlayers(int playerId) {
    	String checkPlayerSql = "SELECT COUNT(*) FROM player WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(checkPlayerSql)) {
             
            pstmt.setInt(1, playerId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0; 
            }
        } catch (SQLException e) {
            e.printStackTrace(); 
        }
        return false;
    }
    
    private boolean isAlreadyInTeam(int playerId, int teamId, Connection conn) {
        String query = "SELECT COUNT(*) FROM team_player WHERE player_id = ? AND team_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, playerId);
            pstmt.setInt(2, teamId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0; 
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false; 
    }

    private void addPlayersToTeam(Connection conn, Set<Integer> playerSet, int teamId) throws SQLException {
    	
    	String clearQuery = "DELETE FROM team_player WHERE team_id = ?";
        try (PreparedStatement clearPstmt = conn.prepareStatement(clearQuery)) {
            clearPstmt.setInt(1, teamId);
            clearPstmt.executeUpdate();
        }
    	
    	String query = "INSERT INTO team_player (player_id, team_id) VALUES (?, ?)";
        try (PreparedStatement playerPstmt = conn.prepareStatement(query)) {
            for (Integer playerId : playerSet) {
            	
            	if(!isValidTeamPlayers(playerId))
            		throw new SQLException("Player ID " + playerId + " is not a player");
            	
            	if(isAlreadyInTeam(playerId , teamId , conn ))
            		throw new SQLException("Player ID " + playerId + " is already in this Team ID "+ teamId);
            	
            	playerPstmt.setInt(1, playerId);
                playerPstmt.setInt(2, teamId);
                playerPstmt.executeUpdate();
            }
        }
    }

    
    private void deleteOnePlayerFromTeam(HttpServletResponse response , PrintWriter out , String teamId , String playerId) {
    	
    	String countQuery = "SELECT COUNT(*) FROM team_player WHERE team_id = ?";
    	String checkPlayerRoleQuery = "SELECT captain_id, vice_captain_id, wicket_keeper_id FROM team WHERE team_id = ?";
    	String deleteQuery = "DELETE FROM team_player WHERE team_id = ? AND player_id = ?";
    	
    	try {
    	    connection = DriverManager.getConnection(DB_URL, USER, PASS);
    	    
    	    try (PreparedStatement roleStatement = connection.prepareStatement(checkPlayerRoleQuery)) {
                roleStatement.setInt(1, Integer.parseInt(teamId));
                ResultSet roleResultSet = roleStatement.executeQuery();
                
                if (roleResultSet.next()) {
                    int captainId = roleResultSet.getInt("captain_id");
                    int viceCaptainId = roleResultSet.getInt("vice_captain_id");
                    int wicketKeeperId = roleResultSet.getInt("wicket_keeper_id");
                    
                    if (Integer.parseInt(playerId) == captainId || Integer.parseInt(playerId) == viceCaptainId || Integer.parseInt(playerId) == wicketKeeperId) {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        Extra.sendError(response, out, "Cannot remove this player as they are the captain, vice-captain, or wicket-keeper.");
                        return;
                    }
                }
            }
    	    
    	    
    	    try (PreparedStatement countStatement = connection.prepareStatement(countQuery)) {
    	        countStatement.setInt(1, Integer.parseInt(teamId));
    	        ResultSet resultSet = countStatement.executeQuery();

    	        int currentPlayerCount = 0;
    	        if (resultSet.next()) {
    	            currentPlayerCount = resultSet.getInt(1);
    	        }

    	        if (currentPlayerCount > 11) {
    	            preparedStatement = connection.prepareStatement(deleteQuery);
    	            preparedStatement.setInt(1, Integer.parseInt(teamId));
    	            preparedStatement.setInt(2, Integer.parseInt(playerId));

    	            int rowsAffected = preparedStatement.executeUpdate();

    	            if (rowsAffected > 0) {
    	            	Extra.sendError(response, out, "Player with ID " + playerId + " has been removed from team " + teamId);
    	            } else {
    	                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    	                Extra.sendError(response, out, "No matching player found for deletion.");
    	            }
    	        } else {
    	            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    	            Extra.sendError(response, out, "Cannot remove player. Team must have more than 11 players.");
    	        }
    	    }
    	} catch (SQLException e) {
    	    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    	    Extra.sendError(response, out, "Database error: " + e.getMessage());
    	    e.printStackTrace();
    	} catch (NumberFormatException e) {
    	    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    	    Extra.sendError(response, out, "Invalid team or player ID format.");
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
