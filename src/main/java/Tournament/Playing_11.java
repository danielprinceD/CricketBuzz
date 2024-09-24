package Tournament;
import java.io.BufferedReader;
import java.io.IOException;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import Model.Playing11Model;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import Team.Extra;

public class Playing_11 extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    private static final String DB_URL = "jdbc:mysql://localhost:3306/CricketBuzz";
    private static final String USER = "root";
    private static final String PASS = "";
    
    private void getAll(HttpServletRequest request , HttpServletResponse response , PrintWriter out) throws IOException 
    {

    	String fixtureId = request.getParameter("fixture_id");
        String teamId = request.getParameter("team_id");

        if (fixtureId == null || fixtureId.isEmpty() || teamId == null || teamId.isEmpty()) {
            Extra.sendError(response, out, "Both fixture_id and team_id are required.");
            return;
        }

        StringBuilder sql = new StringBuilder("SELECT * FROM playing_11 WHERE fixture_id = ? AND team_id = ?");

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            pstmt.setInt(1, Integer.parseInt(fixtureId));
            pstmt.setInt(2, Integer.parseInt(teamId));

            try (ResultSet rs = pstmt.executeQuery()) {
                JSONArray playing11Array = new JSONArray();
               
                while (rs.next()) {
                    JSONObject playing11 = new JSONObject();
                    playing11.put("player_id", rs.getInt("player_id"));
                    playing11.put("role", rs.getString("role"));
                    playing11.put("runs" , rs.getInt("runs"));
                    playing11.put("balls_faced" , rs.getInt("balls_faced"));
                    playing11.put("4s" , rs.getInt("fours"));
                    playing11.put("6s" , rs.getInt("sixes"));
                    playing11.put("50s" , rs.getInt("fifties"));
                    playing11.put("100s" , rs.getInt("hundreds"));
                    playing11.put("wkt_taken" , rs.getInt("wickets_taken"));
                    playing11Array.put(playing11);
                }

                if (playing11Array.length() > 0) {
                    response.setContentType("application/json");
                    out.print(playing11Array.toString());
                } else {
                    Extra.sendError(response, out, "No records found for the provided fixture_id and team_id.");
                }
            }
        } catch (SQLException e) {
            Extra.sendError(response, out, "Database error: " + e.getMessage());
            e.printStackTrace();
        } catch (NumberFormatException e) {
            Extra.sendError(response, out, "Invalid parameter format: " + e.getMessage());
        }
    	
    }
    
    
    public boolean canUpdatePlayer(int fixtureId, int teamId, int playerId) {
        String sql = "SELECT COUNT(*) FROM playing_11 WHERE fixture_id = ? AND team_id = ? AND player_id = ?";
        boolean canUpdate = false;

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, fixtureId);
            pstmt.setInt(2, teamId);
            pstmt.setInt(3, playerId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt(1);
                    canUpdate = (count > 0);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return canUpdate;
    }

    
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
    	

		response.setContentType("application/json");
		
		PrintWriter out = response.getWriter();
		
		getAll(request , response , out );
		
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        StringBuilder jsonString = new StringBuilder();
        BufferedReader reader = request.getReader();
        String line;
        while ((line = reader.readLine()) != null) {
            jsonString.append(line);
        }

        Type listType = new TypeToken<List<Playing11Model>>() {}.getType();
        List<Playing11Model> playing11List = new Gson().fromJson(jsonString.toString(), listType);

        String sql = "UPDATE playing_11 SET role = ?, runs = ?, balls_faced = ?, fours = ?, sixes = ?, fifties = ?, hundreds = ?, wickets_taken = ? "
                   + "WHERE fixture_id = ? AND player_id = ? AND team_id = ?";

        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
        	
        	
            String fixtureId = request.getParameter("fixture_id");
            String teamId = request.getParameter("team_id");

            for (Playing11Model model : playing11List) {
            	
            	if(!canUpdatePlayer(Integer.parseInt(fixtureId) , Integer.parseInt(teamId) , model.getPlayerId()))
            		throw new SQLException("Updating a player only applicable if players in playing 11");
            	
            	if(fixtureId == null || teamId == null)
            		throw new SQLException("Both Fixture ID and Team ID are required");
            	
            	if(!isValid("fixture" , "fixture_id" , Integer.parseInt(fixtureId) ))
            		throw new SQLException("Fixture ID " + fixtureId + " is not a fixture");
            	if(!isValid("team" , "team_id" , Integer.parseInt(teamId)))
            		throw new SQLException("Team ID " + teamId + " is not a Team");
            	
            	if(!isValid("player" , "id" , model.getPlayerId()))
            		throw new SQLException("PLayer ID " + model.getPlayerId() + " is not a player");
            	
            	if(!checkFixtureAndTeam(Integer.parseInt(fixtureId) , Integer.parseInt(teamId)))
        			throw new SQLException("This team ID " + teamId + " has no match for this Fixture ID " + fixtureId);
            	
            		
                    pstmt.setString(1, model.getRole());
                    pstmt.setInt(2, model.getRuns());
                    pstmt.setInt(3, model.getBallsFaced());
                    pstmt.setInt(4, model.getFours());
                    pstmt.setInt(5, model.getSixes());
                    pstmt.setInt(6, model.getFifties());
                    pstmt.setInt(7, model.getHundreds());
                    pstmt.setInt(8, model.getWicketsTaken());
                    pstmt.setInt(9, Integer.parseInt(fixtureId));
                    pstmt.setInt(10, model.getPlayerId());
                    pstmt.setInt(11, Integer.parseInt(teamId));
                    
                    pstmt.addBatch();
               
            }

            int[] rowsAffected = pstmt.executeBatch();

            PrintWriter out = response.getWriter();
            if (rowsAffected.length > 0) {
                out.println("Player data updated successfully.");
            } else {
                out.println("Failed to update player data.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            response.getWriter().println("Database error: " + e.getMessage());
        } catch (NumberFormatException e) {
            response.getWriter().println("Invalid number format: " + e.getMessage());
        }
    }

    
    private void deleteByTeam(HttpServletResponse response , int fixtureId , int teamId) throws IOException {
    	
    	String sql = "DELETE FROM playing_11 WHERE fixture_id = ? AND team_id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, fixtureId);
            pstmt.setInt(2, teamId);

            int rowsAffected = pstmt.executeUpdate();
            PrintWriter out = response.getWriter();
            if (rowsAffected > 0) {
                out.println("Player record deleted successfully.");
            } else {
                out.println("Failed to delete player record.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            response.getWriter().println("Database error: " + e.getMessage());
        }
    }
    
    private void deleteByPlayer(HttpServletResponse response , int fixtureId , int teamId , int playerId) throws IOException
    {
    	String sql = "DELETE FROM playing_11 WHERE fixture_id = ? AND team_id = ? AND player_id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, fixtureId);
            pstmt.setInt(2, teamId);
            pstmt.setInt(3, playerId);

            int rowsAffected = pstmt.executeUpdate();
            PrintWriter out = response.getWriter();
            if (rowsAffected > 0) {
                out.println("Player record deleted successfully.");
            } else {
                out.println("Failed to delete player record.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            response.getWriter().println("Database error: " + e.getMessage());
        }
    }
    
    
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String fixtureIdParam = request.getParameter("fixture_id");
        String playerIdParam = request.getParameter("player_id");
        String teamIdParam = request.getParameter("team_id");

        Integer fixtureId = null;
        Integer playerId = null;
        Integer teamId = null;

        try {
            if (fixtureIdParam != null) {
                fixtureId = Integer.parseInt(fixtureIdParam);
            }
            if (playerIdParam != null) {
                playerId = Integer.parseInt(playerIdParam);
            }
            if (teamIdParam != null) {
                teamId = Integer.parseInt(teamIdParam);
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Invalid parameter format: " + e.getMessage());
            return;
        }

        if (fixtureId != null) {
            if (playerId != null && teamId != null) {
                deleteByPlayer(response, fixtureId, teamId, playerId);
            } else if (teamId != null && playerId == null) {
                deleteByTeam(response, fixtureId, teamId);
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().println("Missing parameters for deletion.");
            }
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("fixture_id is required.");
        }
    }
    
    private boolean isValid(String table  , String field , int value) throws SQLException{
		
    	String sql = "SELECT COUNT(*) FROM " + table + " WHERE " + field + " = ?";
    	
    	try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
    	         PreparedStatement pstmt = conn.prepareStatement(sql)) {
    	        
    	        pstmt.setInt(1, value);
    	        
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
    
    private boolean checkDuplicatePlayer(int teamId , int fixtureId , int playerId) {
    	
    	String sql = "SELECT COUNT(*) FROM playing_11 WHERE team_id = ? AND fixture_id = ? AND player_id = ?";
    	
    	try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
   	         PreparedStatement pstmt = conn.prepareStatement(sql)) {
   	        
   	        pstmt.setInt(1, teamId);
   	        pstmt.setInt(2, fixtureId);
   	        pstmt.setInt(3, playerId);
   	        
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
    
    public boolean canPlayerAddedToPlaying11(int playerId , int teamId) {
    	
    	String sql = "SELECT * FROM team_player WHERE player_id = ? AND team_id = ?";
    	
    	try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
      	         PreparedStatement pstmt = conn.prepareStatement(sql)) {
      	        
      	        pstmt.setInt(1, playerId);
      	        pstmt.setInt(2, teamId);
      	        
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
    
    private boolean checkFixtureAndTeam(int fixtureId , int teamId) {
    	String getTeamSql = "SELECT team1_id , team2_id FROM fixture WHERE fixture_id = ?";
    	
    	try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
     	         PreparedStatement pstmt = conn.prepareStatement(getTeamSql)) {
     	        
     	        pstmt.setInt(1, fixtureId);
     	        
     	        try (ResultSet rs = pstmt.executeQuery()) {
     	            if (rs.next()) {
     	                int team1 = rs.getInt("team1_id");
     	                int team2 = rs.getInt("team2_id");
     	                return team1 == teamId | team2 == teamId;
     	            }
     	        }
     	    } catch (SQLException e) {
     	        e.printStackTrace();
     	    }
      	
    	return false;
    }
    
    public boolean checkPlayerOnOtherTeam(int fixtureId, int teamId, HashSet<Integer> playerSet) {
        String sql = "SELECT team1_id, team2_id FROM fixture WHERE fixture_id = ?";
        boolean playerExistsInOtherTeam = false;
        
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, fixtureId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int team1 = rs.getInt("team1_id");
                    int team2 = rs.getInt("team2_id");
                    
                    int teamToCheck = (team1 == teamId) ? team2 : team1;

                    String checkSql = "SELECT player_id FROM playing_11 WHERE fixture_id = ? AND team_id = ?";
                    
                    try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                        checkStmt.setInt(1, fixtureId);
                        checkStmt.setInt(2, teamToCheck);
                        
                        try (ResultSet playerRs = checkStmt.executeQuery()) {
                            while (playerRs.next()) {
                                int playerId = playerRs.getInt("player_id");
                                if (playerSet.contains(playerId)) {
                                    playerExistsInOtherTeam = true;
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return playerExistsInOtherTeam;
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
        
        Type listType = new TypeToken<List<Playing11Model>>() {}.getType();
        List<Playing11Model> playing11List = new Gson().fromJson(jsonString.toString(), listType);
        
        
        
        
        if(playing11List.size() > 11)
        {
        	Extra.sendError(response, response.getWriter() , "Playing X1 should not be more than 11 players");
        	return;
        }
        else if(playing11List.size() < 11)
        {
        	Extra.sendError(response, response.getWriter() , "Playing X1 should not be less than 11 players");
        	return;
        }
        
        
        String sql = "INSERT INTO playing_11 (fixture_id, player_id, team_id, role ) "
                   + "VALUES (?, ?, ?, ?)";

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        
        
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

        	String fixtureId = request.getParameter("fixture_id");
        	String teamId = request.getParameter("team_id");
        	
        	if(fixtureId == null || teamId == null)
        		throw new SQLException("Both Fixture ID and Team ID are required");
        	
        	if(!isValid("fixture" , "fixture_id" , Integer.parseInt(fixtureId) ))
        		throw new SQLException("Fixture ID " + fixtureId + " is not a fixture");
        	if(!isValid("team" , "team_id" , Integer.parseInt(teamId)))
        		throw new SQLException("Team ID " + teamId + " is not a Team");
        	
        	if(!checkFixtureAndTeam(Integer.parseInt(fixtureId) , Integer.parseInt(teamId)))
    			throw new SQLException("This team ID " + teamId + " has no match for this Fixture ID " + fixtureId);
        	
        	HashSet<Integer> playersSet = new HashSet<>();
        	
        	for(Playing11Model model : playing11List)
        	{
        		if(playersSet.contains(model.getPlayerId()))
        			throw new SQLException("Duplicate Players ID is present");
        		playersSet.add(model.getPlayerId());
        	}
        	
        	if(checkPlayerOnOtherTeam(Integer.parseInt(fixtureId) , Integer.parseInt(teamId) , playersSet))
        		throw new SQLException("Same player cannot be on both the teams for a fixture");
            
        	for (Playing11Model model : playing11List) {
            		
            		
            	
            		if(!isValid("player" , "id" , model.getPlayerId() ))
            			throw new SQLException("Player ID " + model.getPlayerId() + " is not a player");
            		
            		if(checkDuplicatePlayer(Integer.parseInt(teamId) , Integer.parseInt(fixtureId)  , model.getPlayerId() ))
            			throw  new SQLException("Player ID "+ model.getPlayerId() +" is already at the fixture");
                    
            		if(!canPlayerAddedToPlaying11( model.getPlayerId() , Integer.parseInt(teamId)))
            			throw new SQLException("Player " + model.getPlayerId() + " is not in the team ID " + teamId);
            		
            		
            		
            		pstmt.setInt(1, Integer.parseInt(fixtureId));
                    pstmt.setInt(2, model.getPlayerId());
                    pstmt.setInt(3, Integer.parseInt(teamId));
                    pstmt.setString(4, model.getRole());
                    pstmt.addBatch();
             }
            

            int[] rowsAffected = pstmt.executeBatch();

            PrintWriter out = response.getWriter();
            if (rowsAffected.length > 0) {
                out.println("Player data inserted successfully.");
            } else {
                out.println("Failed to insert player data.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            response.getWriter().println("Database error: " + e.getMessage());
        }
    }

    
}
