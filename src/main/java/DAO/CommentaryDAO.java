package DAO;

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

import Model.CommentaryVO;
import Servlet.Extra;

public class CommentaryDAO {
	
	
	private static final String DB_URL = "jdbc:mysql://localhost:3306/CricketBuzz";
    private static final String USER = "root";
    private static final String PASS = "";
    
    
    public void insert(HttpServletRequest request , HttpServletResponse response , PrintWriter out , List<CommentaryVO> commentaryList) {
    	String sql = "INSERT INTO commentary (fixture_id, over_count, ball, run_type, commentary_text, batter_id, bowler_id, catcher_id) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
     
     
     
     response.setContentType("application/json");
     response.setCharacterEncoding("UTF-8");

     try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
          PreparedStatement pstmt = conn.prepareStatement(sql)) {

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

         if (rowsAffected.length > 0) {
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

    
    public void getAllCommentaries( HttpServletRequest request , HttpServletResponse response) throws IOException , SQLException {
    		
    	PrintWriter out = response.getWriter();
    	String commentaryId = request.getParameter("commentary_id");
        String fixtureId = request.getParameter("fixture_id");

        if ((commentaryId == null || commentaryId.isEmpty()) && (fixtureId == null || fixtureId.isEmpty())) {
            throw new SQLException("Either commentary_id or fixture_id must be provided.");
        }

        StringBuilder sql = new StringBuilder("SELECT commentary_id, fixture_id,")
            .append("       over_count,")
            .append("       ball,")
            .append("       run_type,")
            .append("       commentary_text,")
            .append("       date_time,")
            .append("       batter_id,")
            .append("       bowler_id,")
            .append("       catcher_id")
            .append(" FROM commentary WHERE ");

        List<Object> params = new ArrayList<>();
        
        if (commentaryId != null && !commentaryId.isEmpty()) {
            sql.append("commentary_id = ?");
            params.add(Integer.parseInt(commentaryId));
        }

        if (fixtureId != null && !fixtureId.isEmpty()) {
            if (!params.isEmpty()) {
                sql.append(" AND ");
            }
            sql.append("fixture_id = ?");
            params.add(Integer.parseInt(fixtureId));
        }

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                JSONArray commentaryArray = new JSONArray();

                while (rs.next()) {
                    JSONObject commentary = new JSONObject();
                    commentary.put("commentary_id", rs.getInt("commentary_id"));
                    commentary.put("fixture_id", rs.getInt("fixture_id"));
                    
                    
                    commentary.put("overCount", rs.getInt("over_count"));
                    commentary.put("ball", rs.getInt("ball"));
                    commentary.put("runType", rs.getString("run_type"));
                    commentary.put("commentaryText", rs.getString("commentary_text"));
                    commentary.put("dateTime", rs.getString("date_time"));
                    commentary.put("batterId", rs.getInt("batter_id"));
                    commentary.put("bowlerId", rs.getInt("bowler_id"));
                    commentary.put("catcherId", rs.getInt("catcher_id"));

                    commentaryArray.put(commentary);
                }

                if (commentaryArray.length() > 0) {
                    response.setContentType("application/json");
                    out.print(commentaryArray.toString());
                } else {
                    Extra.sendError(response, out, "No commentary found for the provided ID(s).");
                }
            }

        } catch (SQLException e) {
            Extra.sendError(response, out, "Database error: " + e.getMessage());
            e.printStackTrace();
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
