package repository;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import controller.*;

public class OverSummaryDAO {
	private static final String DB_URL = "jdbc:mysql://localhost:3306/CricketBuzz";
    private static final String USER = "root";
    private static final String PASS = "";
    
    private Boolean valid(int value, String table, String field) {
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
	
    private boolean isPlayerInPlaying11(int playerId , int fixtureId) {
    	 
    	String sql = "SELECT COUNT(*) FROM playing_11 WHERE player_id = ? AND fixture_id = ?";
    	    
    	    try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
    	         PreparedStatement pstmt = conn.prepareStatement(sql)) {
    	        
    	        pstmt.setInt(1, playerId);
    	        pstmt.setInt(2, fixtureId);
    	        
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
    
    public Integer getTeam(int playerId, int fixtureId) {
        String teamSql = "SELECT team1_id, team2_id FROM fixture WHERE fixture_id = ?";
        String playerSql = "SELECT player_id FROM playing_11 WHERE fixture_id = ? AND team_id = ?";
        
        HashSet<Integer> team1Players = new HashSet<>();
        HashSet<Integer> team2Players = new HashSet<>();
        
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement teamStmt = conn.prepareStatement(teamSql)) {
            
            teamStmt.setInt(1, fixtureId);
            
            ResultSet teamResult = teamStmt.executeQuery();
            if (teamResult.next()) {
                int team1Id = teamResult.getInt("team1_id");
                int team2Id = teamResult.getInt("team2_id");
                
                try (PreparedStatement playerStmt1 = conn.prepareStatement(playerSql)) {
                    playerStmt1.setInt(1, fixtureId);
                    playerStmt1.setInt(2, team1Id);
                    
                    ResultSet playerResult1 = playerStmt1.executeQuery();
                    while (playerResult1.next()) {
                        team1Players.add(playerResult1.getInt("player_id"));
                    }
                }
                
                try (PreparedStatement playerStmt2 = conn.prepareStatement(playerSql)) {
                    playerStmt2.setInt(1, fixtureId);
                    playerStmt2.setInt(2, team2Id);
                    
                    ResultSet playerResult2 = playerStmt2.executeQuery();
                    while (playerResult2.next()) {
                        team2Players.add(playerResult2.getInt("player_id"));
                    }
                }
            }
            
            if (team1Players.contains(playerId)) {
                return 1;
            }
            
            if (team2Players.contains(playerId)) {
                return 2; 
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return -1; 
    }
    
    public void update(HttpServletRequest request , HttpServletResponse response , JSONArray jsonArray) throws Exception {
    	String sql = "UPDATE over_summary SET run = ?, wkt = ? WHERE fixture_id = ? AND over_count = ?";
	    Integer fixtureId = Integer.parseInt(request.getParameter("fixture_id"));
	    
	    try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
	         PreparedStatement pstmt = conn.prepareStatement(sql)) {

	        int updatedRecords = 0;

	        for (int i = 0; i < jsonArray.length(); i++) {
	            JSONObject jsonObject = jsonArray.getJSONObject(i);
	           
	            int overCount = jsonObject.getInt("over_count");
	            int run = jsonObject.getInt("run");
	            int wkt = jsonObject.getInt("wkt");

	            pstmt.setInt(1, run);
	            pstmt.setInt(2, wkt);
	            pstmt.setInt(3, fixtureId);
	            pstmt.setInt(4, overCount);

	            updatedRecords += pstmt.executeUpdate();
	        }

	        if (updatedRecords > 0) {
	            Extra.sendSuccess(response, response.getWriter(), updatedRecords + " record(s) updated successfully.");
	            
	        } else {
	            Extra.sendError(response, response.getWriter(), "No records found to update.");
	        }

	    } catch (SQLException e) {
	        e.printStackTrace();
	        Extra.sendError(response, response.getWriter(), "Database error.");
	    } catch (JSONException e) {
	        Extra.sendError(response, response.getWriter(), "Invalid JSON format.");
	    }
    }
    
    
    public void get(HttpServletRequest request , HttpServletResponse response) throws Exception {
    	StringBuilder sql = new StringBuilder("SELECT * FROM over_summary WHERE 1=1 ");
	    List<Object> parameters = new ArrayList<>();

	    String fixtureIdParam = request.getParameter("fixture_id");

	    if (fixtureIdParam != null) {
	        sql.append(" AND fixture_id = ?");
	        parameters.add(Integer.parseInt(fixtureIdParam));
	    }

	    try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
	         PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

	        for (int i = 0; i < parameters.size(); i++) {
	            pstmt.setObject(i + 1, parameters.get(i));
	        }

	        ResultSet rs = pstmt.executeQuery();
	        JSONArray jsonArray = new JSONArray();

	        while (rs.next()) {
	            JSONObject jsonObject = new JSONObject();
	            jsonObject.put("fixture_id", rs.getInt("fixture_id"));
	            jsonObject.put("over_count", rs.getInt("over_count"));
	            jsonObject.put("run", rs.getInt("run"));
	            jsonObject.put("wkt", rs.getInt("wkt"));

	            jsonArray.put(jsonObject);
	        }

	        response.setContentType("application/json");
	        response.setCharacterEncoding("UTF-8");
	        response.getWriter().print(jsonArray.toString());

	    } catch (SQLException e) {
	        e.printStackTrace();
	        Extra.sendError(response, response.getWriter(), "Database Error");
	    }
    }
    
    
    public void insert(HttpServletRequest request , HttpServletResponse response , PrintWriter out , JSONArray jsonArray) throws Exception {
    	
    	String sql = "INSERT INTO over_summary (fixture_id, over_count, run, wkt , batter1_id , batter2_id , bowler_id ) VALUES (?, ?, ?, ? , ? , ? , ? )";

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
        	conn.setAutoCommit(false);
        	int insertedRecords = 0;
            Integer fixtureId = Integer.parseInt( request.getParameter("fixture_id") );
            
            if(!valid(fixtureId, "fixture", "fixture_id"))
            	throw new Exception("Fixture ID " + fixtureId + " is not a fixture");
           
            
            

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                int overCount = jsonObject.getInt("over_count");
                int run = jsonObject.getInt("run");
                int wkt = jsonObject.getInt("wkt");
                int batter1_id = jsonObject.getInt("batter1_id");
                int batter2_id = jsonObject.getInt("batter2_id");
                int bowler_id = jsonObject.getInt("bowler_id");
                
                
                
                if(!valid(batter1_id, "player", "id"))
                	throw new Exception("Batter 1 ID " + batter1_id + " is not a player");
                
                if(!valid(batter2_id, "player", "id"))
                	throw new Exception("Batter 2 ID " + batter2_id + " is not a player");
               
                if(!valid(bowler_id, "player", "id"))
                	throw new Exception("Bowler ID " + bowler_id + " is not a player");
                
                if(!isPlayerInPlaying11(batter1_id , fixtureId))
                	throw new Exception("Batter 1 ID " + batter1_id + " is not in playing 11s");
                
                if(!isPlayerInPlaying11(batter2_id , fixtureId))
                	throw new Exception("Batter 2 ID " + batter1_id + " is not in playing 11s");
                
                if(!isPlayerInPlaying11(bowler_id , fixtureId))
                	throw new Exception("Bowler 1 ID " + batter1_id + " is not in playing 11s");
                	
                Integer batter1Team = getTeam(batter1_id, fixtureId);
                Integer batter2Team = getTeam(batter2_id, fixtureId);
                Integer bowlerTeam = getTeam(bowler_id, fixtureId);
                
                if(batter1_id == -1 || batter2_id == -1 || bowler_id == -1)
                	throw new Exception("Player should be in a playing 11");
                
                if(batter1Team != batter2Team)
                	 throw new Exception("Batter 1 and 2 should be on same team");
                
                if((batter1Team == bowlerTeam ) || ( batter2Team == bowlerTeam ) )
                		throw new Exception("Batter and Bowler cannot be on same team");
                
                
                pstmt.setInt(1, fixtureId);
                pstmt.setInt(2, overCount);
                pstmt.setInt(3, run);
                pstmt.setInt(4, wkt);
                pstmt.setInt(5, batter1_id);
                pstmt.setInt(6, batter2_id);
                pstmt.setInt(7, bowler_id);
                
                insertedRecords += pstmt.executeUpdate();
            
            }

            if (insertedRecords > 0) {
            	conn.commit();
                Extra.sendError(response, response.getWriter(), insertedRecords + " record(s) created successfully.");
            } else {
            	conn.rollback();
                Extra.sendError(response, response.getWriter(), "No records inserted.");
            }

        }catch (SQLIntegrityConstraintViolationException e) {
        	Extra.sendError(response, response.getWriter(), "Same over_count for fixture id cannot be added");
		} 
        catch (SQLException e) {
            e.printStackTrace();
            Extra.sendError(response, response.getWriter(), "Database Error");
        } catch (JSONException e) {
        	Extra.sendError(response, response.getWriter(), "Invalid JSON format.");
        }
        catch (Exception e) {
        	Extra.sendError(response, response.getWriter(), e.getMessage() );
		}
    }
    
    public void delete(HttpServletRequest request , HttpServletResponse response) throws Exception {
    	StringBuilder sql = new StringBuilder("DELETE FROM over_summary WHERE 1=1");
	    List<Object> parameters = new ArrayList<>();

	    String fixtureIdParam = request.getParameter("fixture_id");
	    String overCountParam = request.getParameter("over_count");

	    if (fixtureIdParam != null) {
	        sql.append(" AND fixture_id = ?");
	        parameters.add(Integer.parseInt(fixtureIdParam));
	    }
	    if (overCountParam != null) {
	        sql.append(" AND over_count = ?");
	        parameters.add(Integer.parseInt(overCountParam));
	    }

	    if (parameters.isEmpty()) {
	    	Extra.sendError(response, response.getWriter(), "No parameters provided for deletion.");
	        return;
	    }

	    try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
	         PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

	        for (int i = 0; i < parameters.size(); i++) {
	            pstmt.setObject(i + 1, parameters.get(i));
	        }

	        int affectedRows = pstmt.executeUpdate();

	        if (affectedRows > 0) {
	            response.setStatus(HttpServletResponse.SC_OK);
	            Extra.sendSuccess(response, response.getWriter(),"Record(s) deleted successfully." );
	            
	        } else {
	            Extra.sendError(response, response.getWriter(), "No records found to delete.");
	        }

	    } catch (SQLException e) {
	        e.printStackTrace();
	        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error.");
	    } catch (NumberFormatException e) {
	        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid parameter format.");
	    }
    }
    
    
}
