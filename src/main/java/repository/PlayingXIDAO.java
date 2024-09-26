package repository;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import model.*;
import utils.PlayingXIRedisUtil;
import controller.*;

public class PlayingXIDAO {
	
	private static final String DB_URL = "jdbc:mysql://localhost:3306/CricketBuzz";
    private static final String USER = "root";
    private static final String PASS = "";
    
    
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
    
    public void updatePlaying11(List<PlayingXIVO> playing11List, String fixtureId, String teamId) throws SQLException {
        
    	String sql = "UPDATE playing_11 SET role = ?, runs = ?, balls_faced = ?, fours = ?, sixes = ?, fifties = ?, hundreds = ?, wickets_taken = ? "
                   + "WHERE fixture_id = ? AND player_id = ? AND team_id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            for (PlayingXIVO model : playing11List) {
                
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
            if (rowsAffected.length > 0) {
            	System.out.println("Player data updated successfully.");
            } else {
                System.out.println("Failed to update player data.");
            }
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
	
	
	 public void getAll(HttpServletRequest request , HttpServletResponse response , PrintWriter out) throws IOException 
	    {

	    	String fixtureId = request.getParameter("fixtureId");
	        String teamId = request.getParameter("teamId");

	        if (fixtureId == null || fixtureId.isEmpty() || teamId == null || teamId.isEmpty()) {
	            Extra.sendError(response, out, "Both fixture_id and team_id are required.");
	            return;
	        }

	        StringBuilder sql = new StringBuilder("SELECT * FROM playing_11 WHERE fixture_id = ? AND team_id = ?");
	        

	        JSONArray playing11Array = new JSONArray();
	        
	        
	        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
	             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

	            pstmt.setInt(1, Integer.parseInt(fixtureId));
	            pstmt.setInt(2, Integer.parseInt(teamId));

	            try (ResultSet rs = pstmt.executeQuery()) {
	               
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
	                    
	                    PlayingXIRedisUtil.setPlayingXIByFixtureIdByTeamId(playing11Array, Integer.parseInt(fixtureId) , Integer.parseInt(teamId) );
	                    
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
	    
	    
	    public void deleteByTeam(HttpServletResponse response , int fixtureId , int teamId) throws IOException {
	    	
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
	    
	    public void deleteByPlayer(HttpServletResponse response , int fixtureId , int teamId , int playerId) throws IOException
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
	
	    
	    public void insertPlaying11(List<PlayingXIVO> playing11List, int fixtureId, int teamId) throws SQLException {
	       
	    	if (playing11List.size() > 11) {
	            throw new SQLException("Playing XI should not be more than 11 players");
	        } else if (playing11List.size() < 11) {
	            throw new SQLException("Playing XI should not be less than 11 players");
	        }

	        String sql = "INSERT INTO playing_11 (fixture_id, player_id, team_id, role) VALUES (?, ?, ?, ?)";

	        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
	             PreparedStatement pstmt = conn.prepareStatement(sql)) {

	            HashSet<Integer> playersSet = new HashSet<>();

	            for (PlayingXIVO model : playing11List) {
	                if (playersSet.contains(model.getPlayerId())) {
	                    throw new SQLException("Duplicate Player ID is present: " + model.getPlayerId());
	                }
	                playersSet.add(model.getPlayerId());
	            }
	            
	            
	            
	            if (checkPlayerOnOtherTeam(fixtureId, teamId, playersSet)) {
	                throw new SQLException("Same player cannot be on both teams for a fixture");
	            }
	            
	            if(!isValid("fixture" , "fixture_id" , fixtureId))
            		throw new SQLException("Fixture ID " + fixtureId + " is not a fixture");
            	if(!isValid("team" , "team_id" , teamId))
            		throw new SQLException("Team ID " + teamId + " is not a Team");

            	if(!checkFixtureAndTeam(fixtureId ,teamId))
        			throw new SQLException("This team ID " + teamId + " has no match for this Fixture ID " + fixtureId);
            	
            	
	            for (PlayingXIVO model : playing11List) {
	                validatePlayer(model, fixtureId, teamId);
	                
	                
	                
	                
	                pstmt.setInt(1, fixtureId);
	                pstmt.setInt(2, model.getPlayerId());
	                pstmt.setInt(3, teamId);
	                pstmt.setString(4, model.getRole());
	                pstmt.addBatch();
	            }

	            pstmt.executeBatch();
	        }
	    }
	        private void validatePlayer(PlayingXIVO model, int fixtureId, int teamId) throws SQLException {
	            if (!isValid("player", "id", model.getPlayerId())) {
	                throw new SQLException("Player ID " + model.getPlayerId() + " is not a player");
	            }

	            if (checkDuplicatePlayer(teamId, fixtureId, model.getPlayerId())) {
	                throw new SQLException("Player ID " + model.getPlayerId() + " is already at the fixture");
	            }

	            if (!canPlayerAddedToPlaying11(model.getPlayerId(), teamId)) {
	                throw new SQLException("Player " + model.getPlayerId() + " is not in the team ID " + teamId);
	            }
	    }
	    
	    
	    
}

