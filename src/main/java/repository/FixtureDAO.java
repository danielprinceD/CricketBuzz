package repository;

import java.io.PrintWriter;
import java.sql.*;
import java.util.*;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import model.FixtureVO;
import model.PlayingXIVO;
import model.TeamVO;
import model.VenueVO;
import utils.FixtureRedisUtil;
import controller.*;

public class FixtureDAO {
	
	private static final String FIXTURE_BY_ID_QUERY = "SELECT f.round , f.tour_id , f.fixture_id, f.team1_id, t1.name AS team1_name, f.team2_id, t2.name AS team2_name,  v.venue_id, v.stadium, v.location, v.pitch_condition, v.description, v.capacity, v.curator, f.winner_id, winner_team.name AS winner_name, md.toss_win AS toss_win_team_id, toss_team.name AS toss_win_team_name, md.toss_win_decision, md.man_of_the_match AS man_of_the_match_id, mom.name AS man_of_the_match_name, f.status FROM fixture f LEFT JOIN team t1 ON f.team1_id = t1.team_id LEFT JOIN team t2 ON f.team2_id = t2.team_id LEFT JOIN team winner_team ON f.winner_id = winner_team.team_id LEFT JOIN match_details md ON f.fixture_id = md.fixture_id LEFT JOIN player mom ON md.man_of_the_match = mom.id LEFT JOIN team toss_team ON md.toss_win = toss_team.team_id LEFT JOIN venue v ON f.venue_id = v.venue_id WHERE f.fixture_id = ?";
	private static final String FIXTURE_ID_TEAM_ID = "SELECT t.team_id, t.name AS team_name, t.category, p11.player_id, p.name AS player_name, p11.role, p11.runs, p11.balls_faced, p11.fours, p11.sixes, p11.fifties, p11.hundreds, p11.wickets_taken FROM team t JOIN playing_11 p11 ON t.team_id = p11.team_id JOIN player p ON p11.player_id = p.id WHERE t.team_id = ? AND p11.fixture_id = ?";
	
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

    
    public Boolean addManyFixture( List<FixtureVO> fixtureModelList, Integer tourId, String method) throws Exception {
      
    	int totalRowsAffected = 0;

        Boolean isPut = method.equalsIgnoreCase("PUT");
        
       
        
        String insertSql = "INSERT INTO fixture (team1_id, team2_id, venue_id, match_date, tour_id , round) VALUES (?, ?, ?, ?, ? , ?)";
        
        String updateSql = "UPDATE fixture SET team1_id = ?, team2_id = ?, venue_id = ?, match_date = ? , winner_id = ? , tour_id = ? , round = ? , status = ? WHERE fixture_id = ?";

        
        HashSet<String> matchVenue = new HashSet<>();

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
        	if(!isPut && ( tourId == null ))
        		throw new SQLException("Tour ID is required");
        	
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
                    
                    Integer winnerId = fixtureModel.getWinnerId();

                    if(winnerId != null && winnerId != fixtureModel.getTeam1Id() && winnerId != fixtureModel.getTeam2Id()) {
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
                
                if (totalRowsAffected > 0)
                    return true;
            }
        } 
        return false;
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

    
    public List<FixtureVO> getFixtureByTournamentId(Integer tourId) throws SQLException {
        
    	
        StringBuilder sql = new StringBuilder("SELECT fixture_id, status , round ,team1_id, team2_id, winner_id , match_date FROM fixture WHERE tour_id = ?");
        
        List<FixtureVO> fixtures = new ArrayList<>();
        
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            pstmt.setInt(1, tourId);

            try (ResultSet rs = pstmt.executeQuery()) {
            	
                while (rs.next()) {
                	FixtureVO fixture = new FixtureVO();
                    fixture.setRound( rs.getString("round") );
                    fixture.setStatus(rs.getString("status"));
                    fixture.setFixtureId( rs.getInt("fixture_id"));
                    fixture.setTeam1Id(rs.getInt("team1_id"));
                    fixture.setTeam2Id(rs.getInt("team2_id"));
                    
                    if(rs.getObject("winner_id") !=  null)
                    fixture.setWinnerId( rs.getInt("winner_id"));
                    	
                   
                    fixture.setMatchDate(rs.getString("match_date"));
                    fixtures.add(fixture);
                }
            }
            return fixtures;
        }
    }
    
    
    public TeamVO getTeamByIdTournamentId(int fixtureId , int teamId) throws SQLException {
    	
    	
    	try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
                PreparedStatement stmt = conn.prepareStatement(FIXTURE_ID_TEAM_ID)) {
    		TeamVO team = new TeamVO();

               stmt.setInt(1, teamId);
               stmt.setInt(2, fixtureId);
               ResultSet rs = stmt.executeQuery();

               
               List<PlayingXIVO> playing11s = new ArrayList<>();

               while (rs.next()) {

            	   team.setTeamId(rs.getInt("team_id"));
                   team.setName( rs.getString("team_name"));
                   team.setCategory(rs.getString("category"));
                   
            	   
            	   PlayingXIVO playing11 = new PlayingXIVO();
                   
            	   playing11.setPlayerId(rs.getInt("player_id"));
                   playing11.setName( rs.getString("player_name"));
                   playing11.setRole( rs.getString("role"));
                   playing11.setRuns( rs.getInt("runs"));
                   playing11.setBallsFaced(rs.getInt("balls_faced"));
                   playing11.setFours(rs.getInt("fours"));
                   playing11.setSixes(rs.getInt("sixes"));
                   playing11.setFifties(rs.getInt("fifties"));
                   playing11.setHundreds(rs.getInt("hundreds"));
                   playing11.setWicketsTaken(rs.getInt("wickets_taken"));
                   
                   playing11s.add(playing11);
               }
               if(playing11s.size() > 0)
            	   team.setPlaying11s(playing11s);
               
               
               return team;
    	}
    }
    
	
    public FixtureVO getFixtureById(Integer fixtureId) throws SQLException {
        
    	StringBuilder sql = new StringBuilder(FIXTURE_BY_ID_QUERY);
        FixtureVO fixture = new FixtureVO();
    	
        try (
        		Connection conn = DriverManager.getConnection(DB_URL , USER , PASS);
        		PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            	
        	pstmt.setInt(1, fixtureId);

            try (ResultSet rs = pstmt.executeQuery()) {
            	
                if (rs.next()) {
                    fixture.setRound(rs.getString("round"));
                    fixture.setStatus(rs.getString("status"));
                    fixture.setFixtureId(rs.getInt("fixture_id"));
                    fixture.setTourId(rs.getInt("tour_id"));
                    fixture.setTeam1Id(rs.getInt("team1_id"));
                    fixture.setTeam1Name(rs.getString("team1_name"));
                    fixture.setTeam2Name(rs.getString("team2_name"));
                    fixture.setTeam2Id(rs.getInt("team2_id"));
                    
                    if(rs.getObject("winner_id") != null)
                    {                    	
	                    fixture.setWinnerId(rs.getInt("winner_id"));
	                    fixture.setWinnerTeamName(rs.getString("winner_name"));
	                    fixture.setManOfTheMatch(rs.getInt("man_of_the_match_id"));
	                    fixture.setManOfTheMatchPlayerName(rs.getString("man_of_the_match_name"));
                    }
                    
                    VenueVO venue = new VenueVO();
                    
                    venue.setVenueId(rs.getInt("venue_id"));
                    venue.setStadium(rs.getString("stadium"));
                    venue.setLocation(rs.getString("location"));
                    venue.setPitchCondition(rs.getString("pitch_condition"));
                    venue.setDescription(rs.getString("description"));
                    venue.setCapacity(rs.getInt("capacity"));
                    venue.setCurator(rs.getString("curator"));
                    
                    fixture.setVenue(venue);
                    
                    if(rs.getObject("toss_win_team_id") != null)
                    {
                    	fixture.setTossWinnerId(rs.getInt("toss_win_team_id"));
                    	fixture.setTossWinnerTeamName(rs.getString("toss_win_team_name"));                    	
                    	fixture.setTossWinnerDecision(rs.getString("toss_win_decision"));
                    }
                    
                    
                    return fixture;
                }
                
                return null;
                
            }
        }
    }
}
