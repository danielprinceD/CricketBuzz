package repository;

import java.io.PrintWriter;
import java.sql.*;
import java.util.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import model.FixtureVO;
import utils.FixtureRedisUtil;
import controller.*;

public class FixtureDAO {
	
	private static final String BASE_SELECT_QUERY = "SELECT round, status, fixture_id, tour_id, team1_id, team2_id, winner_id, venue_id, match_date FROM fixture";

	private static final String DB_URL = "jdbc:mysql://localhost:3306/CricketBuzz";
    private static final String USER = "root";
    private static final String PASS = "";
    
    
 private boolean isVenuePresent(FixtureVO fixtureModel) {
    	
        String venuePresentQuery = "SELECT COUNT(*) FROM venue WHERE venue_id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(venuePresentQuery)) {

            pstmt.setInt(1, fixtureModel.getVenueId());

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                int count = rs.getInt(1);
                return count > 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


    private boolean isValidVenue(FixtureVO fixtureModel , int tourId) throws SQLException{
       
    	if(!isVenuePresent(fixtureModel))
    		throw new SQLException("Venue ID " + fixtureModel.getVenueId() + " is not a venue");
    			
    	
    	String sql = "SELECT COUNT(*) FROM fixture WHERE venue_id = ? AND match_date = ? AND tour_id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
             	
        	
        	 	pstmt.setInt(1, fixtureModel.getVenueId());
        	    pstmt.setString(2, fixtureModel.getMatchDate());
        	    pstmt.setInt(3, tourId);

            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                int count = rs.getInt(1);
                
                return count == 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            
        }
        
        return false;
    }

    private boolean isValidTeam(int teamId) {
        
    	String sql = "SELECT COUNT(*) FROM team WHERE team_id = ?";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, teamId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                int count = rs.getInt(1);
                return count > 0;  
            }

        } catch (SQLException e) {
            e.printStackTrace(); 
        }

        return false; 
    }

    private boolean isValidTournament(int tourId) {
        String checkTourIDSql = "SELECT COUNT(*) FROM tournament WHERE tour_id = ?";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(checkTourIDSql)) {

            pstmt.setInt(1, tourId); 
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                int count = rs.getInt(1); 
                return count > 0;
            }

        } catch (SQLException e) {
            e.printStackTrace(); 
        }

        return false; 
    }
    
    private boolean canUpdate(FixtureVO fm, Connection connection) {
        String sql = "SELECT COUNT(*) " +
                     "FROM fixture " +
                     "WHERE venue_id = ? " +
                     "  AND match_date = ? " +
                     "  AND fixture_id != ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
        	
            stmt.setInt(1, fm.getVenueId());
            stmt.setDate(2, java.sql.Date.valueOf(fm.getMatchDate()));
            stmt.setInt(3, fm.getFixtureId());

            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                int count = rs.getInt(1);
                return count == 0;  
            }
        } catch (SQLException e) {
            e.printStackTrace(); 
        }

        return false;
    }
    
    private boolean isValidFixtureID(FixtureVO fm , Connection connection) {
    	 String sql = "SELECT COUNT(*) FROM fixture WHERE fixture_id = ?";

    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
    	
        stmt.setInt(1, fm.getFixtureId());

        ResultSet rs = stmt.executeQuery();
        
        if (rs.next()) {
            int count = rs.getInt(1);
            return count > 0;  
        }
    } catch (SQLException e) {
        e.printStackTrace(); 
    }

    return false;
	}

    private boolean checkTeamInTournament(int teamId, int tourId) {
        String sql = "SELECT COUNT(*) FROM tournament_team WHERE tour_id = ? AND team_id = ?";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
             
            pstmt.setInt(1, tourId);
            pstmt.setInt(2, teamId);
            
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                int count = rs.getInt(1);
                return count > 0; 
            }
        } catch (SQLException e) {
            e.printStackTrace(); 
        }
        return false;
    }

    
    public void addManyFixture(HttpServletResponse response, PrintWriter out, List<FixtureVO> fixtureModelList, String tourIdString, String method) throws ServletException , SQLException {
      
    	int totalRowsAffected = 0;

        Boolean isPut = method.equalsIgnoreCase("PUT");
        
       
        
        String insertSql = "INSERT INTO fixture (team1_id, team2_id, venue_id, match_date, tour_id , round) VALUES (?, ?, ?, ?, ? , ?)";
        
        String updateSql = "UPDATE fixture SET team1_id = ?, team2_id = ?, venue_id = ?, match_date = ? , winner_id = ? , tour_id = ? , round = ? , status = ? WHERE fixture_id = ?";

        
        HashSet<String> matchVenue = new HashSet<>();

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
        	if(!isPut && ( tourIdString == null ))
        		throw new SQLException("Tour ID is required");
        	int tourId =  Integer.parseInt(tourIdString);
        	
        		if (!isPut && !isValidTournament(tourId))
        			throw new SQLException("Tournament ID " + tourId + " is not found");
        		
            

            for (FixtureVO fm : fixtureModelList) {
            	if(isPut)
            	{
            		if(!isValidFixtureID(fm , conn))
            			throw new SQLException("Fixture ID " + fm.getFixtureId() + " is not a fixture");
            		if(!canUpdate(fm , conn))
            			throw new SQLException("Venue ID " + fm.getVenueId() +  " is already occupied on date " + fm.getMatchDate());
            	}
                String venueMatch = fm.getVenueId() + " " + fm.getMatchDate();
                if (matchVenue.contains(venueMatch))
                    throw new SQLException("Venue ID " +fm.getVenueId() +" cannot be fixed on the same match date");
                matchVenue.add(venueMatch);
            }

            conn.setAutoCommit(false);
            
            String sql = (isPut ) ? updateSql : insertSql;

            try (PreparedStatement pstmt = conn.prepareStatement(sql ,Statement.RETURN_GENERATED_KEYS)) {
            	
                for (FixtureVO fixtureModel : fixtureModelList) {
                	
                	if(!checkTeamInTournament(fixtureModel.getTeam1Id() , tourId))
                		throw new SQLException("Team 1 ID " + fixtureModel.getTeam1Id() + " is not in tournament");
                	
                	if(!checkTeamInTournament(fixtureModel.getTeam2Id() , tourId))
                		throw new SQLException("Team 2 ID " + fixtureModel.getTeam2Id() + " is not in tournament");
                	
                	if( !fixtureModel.isValid() )
                		throw new SQLException("Team1 ID , Team2ID , Venue ID , Mathdate is required");
                	
                    if (fixtureModel.getTeam1Id() == fixtureModel.getTeam2Id() )
                        throw new SQLException("Team 1 and Team 2 cannot be the same");
                    
                    if(isPut && !isValidTournament(tourId))
                    	throw new SQLException("Tour ID " + fixtureModel.getTourId() + " is not a tournament");
                    
                    if (!isPut && !isValidVenue(fixtureModel, tourId))
                        throw new SQLException("Venue ID "+ fixtureModel.getVenueId() +" is already occupied");
                    

                    if (!isValidTeam(fixtureModel.getTeam1Id()))
                        throw new SQLException("Team " + fixtureModel.getTeam1Id() + " is not a Team");

                    if (!isValidTeam(fixtureModel.getTeam2Id()))
                        throw new SQLException("Team " + fixtureModel.getTeam2Id() + " is not a Team");
                    
                    Integer winnerId = (fixtureModel.getWinnerId() < 0) ? (Integer) fixtureModel.getWinnerId()  : -1;

                    if(winnerId != -1 && winnerId != fixtureModel.getTeam1Id() && winnerId != fixtureModel.getTeam2Id()) {
                        throw new SQLException("Winner cannot be apart from team1 or team2");
                    }
                    

                   
                    pstmt.setInt(1, fixtureModel.getTeam1Id());
                    pstmt.setInt(2, fixtureModel.getTeam2Id());
                    pstmt.setInt(3, fixtureModel.getVenueId());
                    
                    pstmt.setString(4, fixtureModel.getMatchDate());
                    if (method.equalsIgnoreCase("PUT")) {
                    	pstmt.setObject(5,  (fixtureModel.getWinnerId() < 0) ? JSONObject.NULL : (int)fixtureModel.getWinnerId());
                        pstmt.setInt(6, tourId);
                        
                        pstmt.setObject(7 , (fixtureModel.getRound() == null) ? JSONObject.NULL : fixtureModel.getRound());
                        pstmt.setString(8, fixtureModel.getStatus());
                        pstmt.setInt(9, fixtureModel.getFixtureId());
                    } else {
                        pstmt.setInt(5, tourId);
                        pstmt.setObject(6, fixtureModel.getRound() == null ? JSONObject.NULL : fixtureModel.getRound());
                        
                        try (ResultSet generatedKey = pstmt.getGeneratedKeys()){
                        	if(generatedKey.next())
                        		fixtureModel.setFixtureId(generatedKey.getInt(1));
                        }
                    }
                    
                    
                    totalRowsAffected += pstmt.executeUpdate();
                    
                }
                
                conn.commit();
                
                if( FixtureRedisUtil.isCached() )
                	FixtureRedisUtil.setFixtureByTourID(fixtureModelList, tourId);
                
                if (totalRowsAffected > 0) {
                	
                    Extra.sendSuccess(response, out, totalRowsAffected + " fixtures added/updated successfully.");
                } else {
                    Extra.sendError(response, out, "No fixtures were added/updated.");
                }
            } catch (SQLException e) {
                conn.rollback();
                Extra.sendError(response, out, e.getMessage());
            }
        } catch (SQLException e) {
            Extra.sendError(response, out, e.getMessage());
        }catch (Exception e) {
        	Extra.sendError(response, out,e.getMessage()  );
        	e.printStackTrace();
		}
    }


    
    
    public void deleteAllFixture(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
       
    	StringBuilder sql = new StringBuilder("DELETE FROM fixture WHERE");
        
    	List<Object> params = new ArrayList<>();

        String tourId = request.getParameter("tourId");
        String fixtureId = request.getParameter("fixtureId");

        boolean hasCondition = false;

        if (tourId != null && !tourId.isEmpty()) {
            sql.append(" tour_id = ?");
            params.add(Integer.parseInt(tourId));
            hasCondition = true;
        }

        if (fixtureId != null && !fixtureId.isEmpty()) {
            if (hasCondition) {
                sql.append(" AND");
            }
            sql.append(" fixture_id = ?");
            params.add(Integer.parseInt(fixtureId));
            hasCondition = true;
        }

        if (!hasCondition) {
            Extra.sendError(response, out, "No valid parameters provided. At least one of tour_id or fixture_id is required.");
            return;
        }

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
            	
            	if(fixtureId != null && FixtureRedisUtil.isCached())
            		FixtureRedisUtil.deleteFixtureById(Integer.parseInt(fixtureId));
            	
            	if(tourId != null && FixtureRedisUtil.isCached())
            		FixtureRedisUtil.deleteFixturesByTournamentId(Integer.parseInt(tourId));
            	
                Extra.sendSuccess(response, out, "Fixtures Deleted Successfully");
            } else {
                Extra.sendError(response, out, "No Data Found for the provided parameters");
            }

        } catch (NumberFormatException e) {
            Extra.sendError(response, out, "Invalid parameter format: " + e.getMessage());
        } catch (SQLException e) {
            Extra.sendError(response, out, "Database error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    
    protected void getFixtureByTour(HttpServletResponse response, PrintWriter out, HttpServletRequest request) {
        String tourId = request.getParameter("tour_id");
        String fixtureId = request.getParameter("fixture_id");

        StringBuilder sql = new StringBuilder("SELECT round , status, fixture_id, tour_id, team1_id, team2_id, winner_id, venue_id, match_date FROM fixture");
        
      
        boolean hasConditions = false;
        List<Object> params = new ArrayList<>();

        if (tourId != null && !tourId.isEmpty()) {
            sql.append(" WHERE tour_id = ?");
            params.add(Integer.parseInt(tourId));
            hasConditions = true; 
        }
        

        if (fixtureId != null && !fixtureId.isEmpty()) {
            sql.append(hasConditions ? " AND fixture_id = ?" : " WHERE fixture_id = ?");
            params.add(Integer.parseInt(fixtureId));
        }

        JSONArray fixtures = new JSONArray();

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    JSONObject fixture = new JSONObject();
                    fixture.put("round", rs.getObject("round"));
                    fixture.put("status", rs.getString("status"));
                    fixture.put("fixture_id", rs.getInt("fixture_id"));
                    fixture.put("tour_id", rs.getInt("tour_id"));
                    fixture.put("team1_id", rs.getInt("team1_id"));
                    fixture.put("team2_id", rs.getInt("team2_id"));
                    fixture.put("winner_id", rs.getObject("winner_id") != null ? rs.getInt("winner_id") : JSONObject.NULL);
                    fixture.put("venue_id", rs.getInt("venue_id"));
                    fixture.put("match_date", rs.getString("match_date"));

                    fixtures.put(fixture);
                }
            }

            if (fixtures.length() > 0) {
                response.setContentType("application/json");
                out.print(fixtures.toString());
            } else {
                Extra.sendError(response, out, "No fixtures found for the provided tour ID " + tourId);
            }

        } catch (NumberFormatException e) {
            Extra.sendError(response, out, "Invalid parameter format: " + e.getMessage());
        } catch (SQLException e) {
            Extra.sendError(response, out, "Database error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
	
    public List<FixtureVO> getFixturesByTour(Integer tourId, Integer fixtureId) throws SQLException {
        
    	StringBuilder sql = new StringBuilder(BASE_SELECT_QUERY);
        List<Object> params = new ArrayList<>();
        
        boolean hasConditions = false;

        if (tourId != null) {
            sql.append(" WHERE tour_id = ?");
            params.add(tourId);
            hasConditions = true;
        }

        if (fixtureId != null) {
            sql.append( hasConditions ? " AND fixture_id = ?" : " WHERE fixture_id = ?");
            params.add(fixtureId);
        }
        List<FixtureVO> fixtures = new ArrayList<>();
        
        if(tourId != null)
        {
        	fixtures = FixtureRedisUtil.getFixturesByTourId(tourId);
        	if(fixtures.size() > 0)
        		return fixtures;
        }
        
        if(fixtureId != null)
        {
        	fixtures = FixtureRedisUtil.getFixtureById(fixtureId);
        	if(fixtures.size() > 0)
        		return fixtures;
        }
        
        
        try (
        		Connection conn = DriverManager.getConnection(DB_URL , USER , PASS);
        		PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    FixtureVO fixture = new FixtureVO();
                    fixture.setRound(rs.getString("round"));
                    fixture.setStatus(rs.getString("status"));
                    fixture.setFixtureId(rs.getInt("fixture_id"));
                    fixture.setTourId(rs.getInt("tour_id"));
                    fixture.setTeam1Id(rs.getInt("team1_id"));
                    fixture.setTeam2Id(rs.getInt("team2_id"));
                    fixture.setWinnerId(rs.getObject("winner_id") != null ? rs.getInt("winner_id") : -1);
                    fixture.setVenueId(rs.getInt("venue_id"));
                    fixture.setMatchDate(rs.getString("match_date"));
                    
                    fixtures.add(fixture);
                }
                
                if(fixtures.size() > 0 && tourId != null)
                	FixtureRedisUtil.setFixtureByTourID(fixtures, tourId);
                
                if(fixtures.size() > 0 && fixtureId != null)
                {
                	
                }
                
                return fixtures;
            }
        }
    }
}
