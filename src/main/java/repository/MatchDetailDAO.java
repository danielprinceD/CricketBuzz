package repository;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import model.*;
import utils.FixtureRedisUtil;
import controller.*;

public class MatchDetailDAO {
	
	private static final String DB_URL = "jdbc:mysql://localhost:3306/CricketBuzz";
    private static final String USER = "root";
    private static final String PASS = "";
	
	private Boolean valid(Integer value, String table, String field) {
		
		if(value == null)
			return true;
		
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
    
	
    private boolean isTeamInFixture(int teamId, int fixtureId, Connection conn) throws SQLException {
        String sql = "SELECT team1_id, team2_id FROM fixture WHERE fixture_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, fixtureId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int team1 = rs.getInt("team1_id");
                    int team2 = rs.getInt("team2_id");
                    
                    if (team1 == teamId || team2 == teamId) {
                        return true;
                    }
                }
            }
        }
        return false; 
    }

    private boolean isPlayerInFixture(Integer playerId, int fixtureId, Connection conn) throws SQLException {
    	
    	if(playerId == null)return true;
    	
        String sql = "SELECT COUNT(*) FROM playing_11 WHERE fixture_id = ? AND player_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, fixtureId);
            pstmt.setInt(2, playerId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }
    
    
    public void get(HttpServletRequest request , HttpServletResponse response , int fixtureId) throws Exception {
    	String sql = "SELECT * FROM match_details WHERE fixture_id = ?";
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, fixtureId);
            ResultSet rs = pstmt.executeQuery();

            JSONObject matchDetailsJson = new JSONObject();
            if (rs.next()) {
                matchDetailsJson.put("fixture_id", rs.getInt("fixture_id"));
                matchDetailsJson.put("toss_win", rs.getInt("toss_win"));
                matchDetailsJson.put("man_of_the_match", rs.getInt("man_of_the_match"));
                matchDetailsJson.put("toss_win_decision", rs.getString("toss_win_decision"));
            }

            PrintWriter out = response.getWriter();
            out.print(matchDetailsJson.toString());
            out.flush();
        } catch (SQLException e) {
            e.printStackTrace();
            response.getWriter().println("Database error: " + e.getMessage());
        }
    }
    
    public Boolean insert( MatchDetailVO matchDetailModel, int fixtureId , Boolean isPut) throws Exception {

    	String sql;
        if(isPut)
        {
        	sql = "INSERT INTO match_details (fixture_id, toss_win, man_of_the_match, toss_win_decision) VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE toss_win = VALUES(toss_win), man_of_the_match = VALUES(man_of_the_match), toss_win_decision = VALUES(toss_win_decision)";
        }
        else {
        	sql = "INSERT INTO match_details (toss_win, man_of_the_match, toss_win_decision , fixture_id ) "
        			+ "VALUES (?, ?, ?, ?)";
        }
        
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
        	
        	
        	if(!valid(fixtureId , "fixture" , "fixture_id"))
        		throw new Exception("Fixture ID " + fixtureId + " is not a fixture");
        	
        	

            	if(!valid(matchDetailModel.getToss_win() , "team" , "team_id"))
            		throw new Exception("Toss Win Team ID " + matchDetailModel.getToss_win() + " is not a Team");
            	
            	if(!valid(matchDetailModel.getMan_of_the_match() , "player" , "id"))
            		throw new Exception("Man of the match ID " + matchDetailModel.getMan_of_the_match() + " is not a Player");
            	
            	if(!isTeamInFixture(matchDetailModel.getToss_win() , fixtureId , conn))
            		throw new Exception("Toss Win Team ID " + matchDetailModel.getToss_win() + " is not a team in this fixture");
            	
            	if(!isPlayerInFixture(matchDetailModel.getMan_of_the_match() , fixtureId , conn))
            		throw new Exception("Man of the match ID " + matchDetailModel.getMan_of_the_match() + " is not in playing 11 for this fixture");
            	
            	pstmt.setInt(1, fixtureId);
                pstmt.setObject(2, matchDetailModel.getToss_win());
                pstmt.setObject(3, matchDetailModel.getMan_of_the_match());
                pstmt.setObject(4, matchDetailModel.getToss_win_decision());

        	int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
            	FixtureRedisUtil.inValidateFixture(fixtureId);
            	return true;
            }
        } 
        return false;
    }
    
    public void delete(HttpServletResponse response , int fixtureId) throws Exception {
    	
    	String sql = "DELETE FROM match_details WHERE fixture_id = ?";
        
        

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, fixtureId);
            
            int rowsAffected = pstmt.executeUpdate();
            PrintWriter out = response.getWriter();
            if (rowsAffected > 0) {
                out.println("Match details deleted successfully.");
            } else {
                out.println("Failed to delete match details.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            response.getWriter().println("Database error: " + e.getMessage());
        }

	}
    
}
