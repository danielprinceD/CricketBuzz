package repository;

import java.beans.Statement;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import com.google.gson.Gson;

import model.CommentaryVO;
import utils.CommentaryRedisUtil;
import controller.*;

public class CommentaryDAO {
	
	
	private static final String DB_URL = "jdbc:mysql://localhost:3306/CricketBuzz";
    private static final String USER = "root";
    private static final String PASS = "";
    
    protected final String COMMENTARIES_GET_QUERY = "SELECT commentary_id, fixture_id, over_count, ball, run_type, commentary_text, date_time, batter_id, bowler_id, catcher_id FROM commentary WHERE fixture_id = ?";
    
    
    public void insert(HttpServletRequest request , HttpServletResponse response , PrintWriter out , List<CommentaryVO> commentaryList) {
    	String sql = "INSERT INTO commentary (fixture_id, over_count, ball, run_type, commentary_text, batter_id, bowler_id, catcher_id) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
     
     
     
     response.setContentType("application/json");
     response.setCharacterEncoding("UTF-8");

     try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
          ) {
    	 PreparedStatement pstmt = conn.prepareStatement(sql , java.sql.Statement.RETURN_GENERATED_KEYS);
     	String fixtureId = request.getParameter("fixture_id");
     	
     	
     
     	
     	String teamSql = "SELECT team1_id, team2_id FROM fixture WHERE fixture_id = ?";
         
         try (PreparedStatement teamPstmt = conn.prepareStatement(teamSql)) {
             teamPstmt.setInt(1, Integer.parseInt(fixtureId));
             
             try (ResultSet rs = teamPstmt.executeQuery()) {
                 if (rs.next()) {
                     
                 	int team1Id = rs.getInt("team1_id");
                     int team2Id = rs.getInt("team2_id");
                     
                     
         HashSet<Integer> team1Players = new HashSet<>();
         HashSet<Integer> team2Players = new HashSet<>();
         
         
         String team1sql  = "SELECT player_id from playing_11 where team_id = ? AND fixture_id = ?";
         String team2sql = "SELECT player_id from playing_11 where team_id = ? AND fixture_id = ?";
                     

			try (PreparedStatement team1Pstmt = conn.prepareStatement(team1sql);
			     PreparedStatement team2Pstmt = conn.prepareStatement(team2sql)) {
			
			    team1Pstmt.setInt(1, team1Id);
			    team1Pstmt.setInt(2, Integer.parseInt(fixtureId)); 
			    try (ResultSet team1Rs = team1Pstmt.executeQuery()) {
			        while (team1Rs.next()) {
			            team1Players.add(team1Rs.getInt("player_id"));
			        }
			    }
			
			   
			    team2Pstmt.setInt(1, team2Id);
			    team2Pstmt.setInt(2, Integer.parseInt(fixtureId)); 
			    try (ResultSet team2Rs = team2Pstmt.executeQuery()) {
			        while (team2Rs.next()) {
			            team2Players.add(team2Rs.getInt("player_id"));
			        }
			    }
			} catch (SQLException e) {
			    e.printStackTrace();
			   
			}

     	
         for (CommentaryVO tourModel : commentaryList) {
         	
         	Integer batterTeam , bowlerTeam , catcherTeam;
         	
         	if(team1Players.contains(tourModel.getBatterId()))
         		batterTeam = 1;
         	else if(team2Players.contains(tourModel.getBatterId()))
         		batterTeam = 2;
         	else {
         		Extra.sendError(response, out, "Batter ID is not a playing 11 in team ID " + team1Id + " for this fixture ID " + fixtureId);
         		return;
         	}
         	
         	if(team1Players.contains(tourModel.getBowlerId()))
         		bowlerTeam = 1;
         	else if(team2Players.contains(tourModel.getBowlerId()))
         		bowlerTeam = 2;
         	else {
         		Extra.sendError(response, out, "Bowler ID is not a playing 11 in team ID " + team1Id + " for this fixture ID " + fixtureId);
         		return;
         	}
         	
         	if(tourModel.getCatcherId() > 0 )
         	{            		
	            	if(team1Players.contains(tourModel.getCatcherId()))
	            		catcherTeam = 1;
	            	else  if(team2Players.contains(tourModel.getCatcherId())) 
	            		catcherTeam = 2;
	            	else {
	            		Extra.sendError(response, out, "Catcher ID is not a playing 11 in team ID " + team1Id + " for this fixture ID " + fixtureId);
	            		return;
	            	}
	            	
	            	if(catcherTeam == batterTeam)
	            		throw new SQLException("Catcher and Batter cannot be on same team");
         	}
         	
         	if(batterTeam == bowlerTeam)
         		throw new SQLException("Batter and Bowler cannot be same team " + batterTeam);
         	
         	int[] overAndBall = getNextBallAndOver(Integer.parseInt(fixtureId), conn);
             
             Integer overCount = overAndBall[0];
             Integer ballCount = overAndBall[1];
             
         	
             pstmt.setInt(1, Integer.parseInt(fixtureId));
             tourModel.setFixtureId(Integer.parseInt(fixtureId));
             tourModel.setBall(ballCount);
             tourModel.setOverCount(overCount);
             pstmt.setInt(2, overCount );
             pstmt.setInt(3, ballCount );
             pstmt.setString(4, tourModel.getRunType());
             pstmt.setString(5, tourModel.getCommentaryText());
             pstmt.setObject(6, tourModel.getBatterId() , java.sql.Types.INTEGER);
             pstmt.setObject(7, tourModel.getBowlerId() , java.sql.Types.INTEGER);
             Object catcherId = tourModel.getCatcherId() < 0 ? JSONObject.NULL : tourModel.getCatcherId();
             pstmt.setObject(8, catcherId);
             pstmt.addBatch();
         }

         int[] rowsAffected = pstmt.executeBatch(); 

         try(ResultSet generatedKeys = pstmt.getGeneratedKeys())
         {
        	 if(generatedKeys.next()) {
        		 System.out.println(generatedKeys.getInt(1));
        		 for(CommentaryVO commentary : commentaryList)
        			 commentary.setCommentaryId(generatedKeys.getInt(1));            		 
        	 }
         }
         if (rowsAffected.length > 0) {
        	 CommentaryRedisUtil.setCommentaryByTourId(Integer.parseInt(fixtureId), commentaryList);
             out.println("Commentary data inserted successfully.");
         } else {
             out.println("Failed to insert commentary data.");
         }
         
                 } else {
                     Extra.sendError(response, out, "No fixture found with the given fixture_id");
                     return;
                 }
             }
         }
         catch (SQLException e) {
             e.printStackTrace();
             Extra.sendError(response, out, "Database error: " + e.getMessage());
             return;
         }

     } catch (SQLException e) {
         e.printStackTrace();
         Extra.sendError(response , out , "Database error: " + e.getMessage());
     }
    }
    
	
	public int[] getNextBallAndOver(int fixtureId, Connection conn) throws SQLException {
        String sql = "SELECT over_count, ball FROM commentary WHERE fixture_id = ? ORDER BY date_time DESC LIMIT 1";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, fixtureId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                int overCount = rs.getInt("over_count");
                int ball = rs.getInt("ball");
                
                ball++;
                if (ball > 6) { 
                    ball = 1;
                    overCount++;
                }
                return new int[]{overCount, ball};
            }
        }
        return new int[]{1, 1};
    }

    
    public List<CommentaryVO> getCommentariesByFixtureId(Integer fixtureId) throws IOException , SQLException {
    		
    	List<CommentaryVO> commentaries = new ArrayList<>();

    	


        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(COMMENTARIES_GET_QUERY)) {

        	pstmt.setInt(1, fixtureId);
        	

            try (ResultSet rs = pstmt.executeQuery()) {

                while (rs.next()) {
                	CommentaryVO commentary = new CommentaryVO();
                    commentary.setCommentaryId(rs.getInt("commentary_id"));
                    
                    commentary.setFixtureId(rs.getInt("fixture_id"));
                    commentary.setOverCount( rs.getInt("over_count"));
                    commentary.setBall(rs.getInt("ball"));
                    commentary.setRunType(rs.getString("run_type"));
                    commentary.setCommentaryText( rs.getString("commentary_text"));
                    commentary.setDateTime(rs.getString("date_time"));
                    commentary.setBatterId(rs.getInt("batter_id"));
                    commentary.setBowlerId(rs.getInt("bowler_id"));
                    Object catcher = rs.getObject("catcher_id");
                    if(catcher != null)
                    commentary.setCatcherId((int) catcher);

                    commentaries.add(commentary);
                }
                
            }
            return commentaries;
        } 
	}
    
    	public void deleteByFixtureId(HttpServletResponse response  , String fixtureIdParam) throws IOException {
        

        String sql = "DELETE FROM commentary WHERE fixture_id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, Integer.parseInt(fixtureIdParam));
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write("Deleted " + affectedRows + " rows.");
            } else {
            	Extra.sendError(response, response.getWriter(), "No Data Found for that Fixture ID");
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error: " + e.getMessage());
        }
    }
    
    public void deleteByCommentaryId(HttpServletResponse response , String commentaryId) throws IOException{
    	 String sql = "DELETE FROM commentary WHERE commentary_id = ?";

         try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
              PreparedStatement pstmt = conn.prepareStatement(sql)) {

             pstmt.setInt(1, Integer.parseInt(commentaryId));
             int affectedRows = pstmt.executeUpdate();

             if (affectedRows > 0) {
                 response.setStatus(HttpServletResponse.SC_OK);
                 response.getWriter().write("Deleted " + affectedRows + " rows.");
             } else {
            	 	Extra.sendError(response, response.getWriter(), "No Data Found for that Commentary ID");
             }

         } catch (Exception e) {
             e.printStackTrace();
             response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error: " + e.getMessage());
         }
    }
}
