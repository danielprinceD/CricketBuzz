package DAO;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import Model.MatchDetailVO;
import Servlet.Extra;

public class MatchDetailDAO {
	
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

    private boolean isPlayerInFixture(int playerId, int fixtureId, Connection conn) throws SQLException {
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
    
    public void insert(HttpServletRequest request , HttpServletResponse response , int fixtureId , MatchDetailVO matchDetailModel) throws Exception {

    	Boolean isPut = request.getMethod().equalsIgnoreCase("PUT");
    	
    	
    	
        String sql;
        if(isPut)
        {
        	sql = "UPDATE match_details SET toss_win = ? , man_of_the_match = ? , toss_win_decision = ? WHERE fixture_id = ?";
        }
        else {
        	sql = "INSERT INTO match_details (toss_win, man_of_the_match, toss_win_decision , fixture_id ) "
        			+ "VALUES (?, ?, ?, ?)";
        }
        PrintWriter out = response.getWriter();
        
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
        	
            pstmt.setInt(1, matchDetailModel.getToss_win());
            pstmt.setInt(2, matchDetailModel.getMan_of_the_match());
            pstmt.setString(3, matchDetailModel.getToss_win_decision());
            pstmt.setInt(4, fixtureId);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                out.println("Match details added successfully.");
            } else {
                out.println("Failed to add match details.");
            }
        } catch (SQLException e) {

        	if (e.getSQLState().equals("23000")) {
                response.setStatus(HttpServletResponse.SC_CONFLICT);
                Extra.sendError(response, out, "Fixture ID must be unique. This ID already exists.");
            }else {
            e.printStackTrace();
            response.getWriter().println("Database error: " + e.getMessage());
            }
        }
        catch (Exception e) {
        	Extra.sendError(response, response.getWriter(), e.getMessage());
		}
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
