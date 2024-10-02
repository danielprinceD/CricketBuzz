package repository;

import java.io.PrintWriter;
import java.sql.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.*;
import utils.AuthUtil;
import utils.TeamRedisUtil;
import utils.TournamentRedisUtil;
import controller.*;


public class TournamentDAO {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/CricketBuzz";
    private static final String USER = "root";
    private static final String PASS = "";
    
    
    private static String INSERT_OR_UPDATE = "INSERT INTO tournament_team (tour_id, team_id, points, net_run_rate) VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE points = VALUES(points), net_run_rate = VALUES(net_run_rate)";
    private static String DELETE_SQL = "DELETE FROM tournament_team WHERE tour_id = ? AND team_id = ?";
    private static String ADD_TEAM_TO_TOUR = "SELECT team_id FROM tournament_team WHERE tour_id = ?";
    private static String GET_ALL_TOURNAMENT = "SELECT * FROM tournament";
    private static String GET_TOURNAMENT_BY_ID = "SELECT * FROM tournament WHERE tour_id = ?";
    private static String TEAMS_BY_TOURNAMENT_ID = "SELECT T.team_id,T.name , TT.points, TT.net_run_rate FROM tournament_team TT JOIN team T ON TT.team_id = T.team_id WHERE TT.tour_id = ?";
    private static String ADD_TOURNAMENT_SQL = "INSERT INTO tournament (name, start_date, end_date, match_category, season, status) VALUES (?, ?, ?, ?, ?, ?)";
    private static String INSERT_SQL = "INSERT INTO tournament (name, start_date, end_date, match_category, season , status , created_by) VALUES (?, ?, ?, ?, ? , ? , ? )";
    private static String UPDATE_SQL = "UPDATE tournament SET name = ?, start_date = ?, end_date = ?, match_category = ?, season = ? , status = ? WHERE tour_id = ?";
    private static String ADD_TEAMS_TO_TOURNAMENT = "INSERT INTO tournament_team (tour_id, team_id, points, net_run_rate) VALUES (?, ?, ?, ?)";
    private static String COUNT_BY_TEAM = "SELECT COUNT(*) FROM team WHERE team_id = ?";
    private static String DELETE_BY_TOUR = "DELETE FROM tournament WHERE tour_id = ?";
    private static String DELETE_TOUR_TEAM = "DELETE FROM tournament_team WHERE tour_id = ?";
    private static String DELETE_TOURTEAM_BY_TEAMID = "DELETE FROM tournament_team WHERE tour_id = ? AND team_id = ?";
    
    public List<TournamentVO> getAllTournaments() throws SQLException {
    	
        List<TournamentVO> tournaments = TournamentRedisUtil.getTournaments();
        
        if(tournaments != null && tournaments.size() > 0)
        	return tournaments;

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement stmt = conn.prepareStatement(GET_ALL_TOURNAMENT);
             ResultSet rs = stmt.executeQuery()) {
        	
            while (rs.next()) {
                TournamentVO tournament = new TournamentVO();
                tournament.setTourId(rs.getInt("tour_id"));
                tournament.setName(rs.getString("name"));
                tournament.setMatchCategory(rs.getString("match_category"));
                tournament.setStatus(rs.getString("status"));
                tournaments.add(tournament);
            }
            
            if(tournaments.size() > 0)
            	TournamentRedisUtil.setTournaments(tournaments);
        }
        return tournaments;
    }

    public TournamentVO getTournamentById(Integer tourId) throws SQLException {
        
    	TournamentVO tournament = TournamentRedisUtil.getTournamentById(tourId);
    	
    	if(tournament != null)
    		return tournament;
    	
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement stmt = conn.prepareStatement(GET_TOURNAMENT_BY_ID)) {

            stmt.setInt(1, tourId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                tournament = new TournamentVO();
                tournament.setTourId(rs.getInt("tour_id"));
                tournament.setName(rs.getString("name"));
                tournament.setStartDate(rs.getString("start_date"));
                tournament.setEndDate(rs.getString("end_date"));
                tournament.setMatchCategory(rs.getString("match_category"));
                tournament.setSeason(rs.getInt("season"));
                tournament.setStatus(rs.getString("status"));
                List<TournamentTeamVO> teams = getTeamsByTournamentId(tournament.getTourId());                
                tournament.setTeamCount(teams.size());
            }
            
            if(tournament != null)
            	TournamentRedisUtil.setTournamentsById(tournament, tourId);
        }
        return tournament;
    }

    public List<TournamentTeamVO> getTeamsByTournamentId(Integer tourId) throws SQLException {
        
    	List<TournamentTeamVO> teams = TeamRedisUtil.getAll(tourId);
    	
    	if(teams != null && teams.size() > 0)
    		return teams;
    	
    	try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement stmt = conn.prepareStatement(TEAMS_BY_TOURNAMENT_ID)) {

            stmt.setInt(1, tourId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
            	TournamentTeamVO team = new TournamentTeamVO();
                team.setTeamId(rs.getInt("team_id"));
                team.setName(rs.getString("name"));
                team.setPoints(rs.getInt("points"));
                team.setNetRunRate(rs.getDouble("net_run_rate"));
                teams.add(team);
            }
            if(teams.size() > 0)
            	TeamRedisUtil.setAllTeams(teams, tourId);
        }
        return teams;
    }

    public void addTournament(TournamentVO tournament) throws SQLException {

    	
    	try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement stmt = conn.prepareStatement(ADD_TOURNAMENT_SQL, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, tournament.getName());
            stmt.setString(2, tournament.getStartDate());
            stmt.setString(3, tournament.getEndDate());
            stmt.setString(4, tournament.getMatchCategory());
            stmt.setInt(5, tournament.getSeason());
            stmt.setString(6, tournament.getStatus());

            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();

            if (rs.next()) {
                tournament.setTourId(rs.getInt(1));
            }
            addTeamsToTournament(tournament);
        }
    }

    private void addTeamsToTournament(TournamentVO tournament) throws SQLException {
        

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement stmt = conn.prepareStatement(ADD_TEAMS_TO_TOURNAMENT)) {

            for (TournamentTeamVO tournamentTeamVO : tournament.getParticipatedTeams()) {
                stmt.setInt(1, tournament.getTourId());
                stmt.setInt(2, tournamentTeamVO.getTeamId());
                stmt.setInt(3, tournamentTeamVO.getPoints());
                stmt.setDouble(4, tournamentTeamVO.getNetRunRate());
                stmt.executeUpdate();
            }
        }
    }
    
    private String prepareSqlStatement(Boolean isPut, TournamentVO tournamentModel ) throws Exception {

        if (tournamentModel.getTourId() == null && tournamentModel.isValid() ) {
            return INSERT_SQL;
        } else if (tournamentModel.isValid() && isPut) {
            return UPDATE_SQL;
        } 
    	throw new Exception("Invalid data or missing parameters.");
    }
    
	private boolean validateTourTeam(TournamentVO teamModel, Set<Integer> teamSet) throws Exception {
	    
			if(teamModel.getParticipatedTeams() == null)
				return true;
		
	    	for (TournamentTeamVO team : teamModel.getParticipatedTeams() ) {
	            if (teamSet.contains(team.getTeamId())) 
	                throw new Exception("Team cannot be added more than once");
	            teamSet.add(team.getTeamId());
	        }
	
	        return true;
	    }
	
	
	private void addTeamsToTour(Connection conn, Set<Integer> teamSet, int tourId , TournamentVO tourModel) throws SQLException {
    	
		Set<Integer> existingTeamIds = new HashSet<>();

	    try (PreparedStatement selectPstmt = conn.prepareStatement(ADD_TEAM_TO_TOUR)) {
	        selectPstmt.setInt(1, tourId);
	        ResultSet resultSet = selectPstmt.executeQuery();

	        while (resultSet.next()) {
	            existingTeamIds.add(resultSet.getInt("team_id"));
	        }
	    }

	    Set<Integer> teamsToRemove = new HashSet<>(existingTeamIds);
	    teamsToRemove.removeAll(teamSet);
	    	
	    if (!teamsToRemove.isEmpty()) {
	        
	        try (PreparedStatement deletePstmt = conn.prepareStatement(DELETE_SQL)) {
	            for (Integer teamId : teamsToRemove) {
	                deletePstmt.setInt(1, tourId);
	                deletePstmt.setInt(2, teamId);
	                deletePstmt.executeUpdate();
	            }
	        }
	    }
	    
	    
		
		try (PreparedStatement pstmt = conn.prepareStatement(INSERT_OR_UPDATE)) {
			
			if(tourModel.getParticipatedTeams() == null)
				return;
		
			for (TournamentTeamVO tourTeam : tourModel.getParticipatedTeams()) {
			
				if (!isValidTeam(tourTeam.getTeamId())) {
				  throw new SQLException("Team ID " + tourTeam.getTeamId() + " is not a team "+ tourId + "."  );
				}
				
				pstmt.setInt(1, tourId);
				pstmt.setInt(2, tourTeam.getTeamId());     
				
				
				
				pstmt.setObject(3, tourTeam.getPoints() , java.sql.Types.INTEGER);
				pstmt.setObject(4, tourTeam.getNetRunRate() , java.sql.Types.DECIMAL);
				
				pstmt.executeUpdate();
				
			}
		}
		
    }

	
	private boolean isValidTeam(int teamId) {
		    
			
		    boolean exists = false;
	
		    try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
		         PreparedStatement pstmt = conn.prepareStatement(COUNT_BY_TEAM)) {
		        
		        pstmt.setInt(1, teamId);
	
		        ResultSet resultSet = pstmt.executeQuery();
		        if (resultSet.next()) {
		            exists = resultSet.getInt(1) > 0; 
		        }
		    } catch (SQLException e) {
		        e.printStackTrace();
		    }
		    
		    return exists; 
		}

	
	
	public int getGeneratedTourId(PreparedStatement pstmt) throws SQLException {
        try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
            if (generatedKeys.next()) {
                return generatedKeys.getInt(1);
            }
        }
        return -1;
    }
    
    
	
	private void setPreparedStatementValues(PreparedStatement pstmt, TournamentVO tourModel , Integer userId) throws SQLException {
	        pstmt.setString(1, tourModel.getName());
	        pstmt.setString(2, tourModel.getStartDate());
	        pstmt.setString(3, tourModel.getEndDate());
	        pstmt.setString(4, tourModel.getMatchCategory());
	        pstmt.setInt(5, tourModel.getSeason());
	        pstmt.setString(6 , tourModel.getStatus());
	        if (tourModel.getTourId() != null) {
	            pstmt.setInt(7, tourModel.getTourId());
	        }else pstmt.setInt(7, userId);
    }
	
	
	public Boolean deleteTournament( HttpServletRequest request , PrintWriter out, Integer tourId) throws Exception {
        if (tourId == null) 
            throw new Exception("Tournament ID is required");

        if(!AuthUtil.isAuthorizedAdmin( request , "tournament", "tour_id", tourId))
        	throw new Exception("You cannot modify other's resouce");

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(DELETE_BY_TOUR)) {

            pstmt.setInt(1, tourId);
            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
            	TournamentRedisUtil.inValidateTournament(tourId);
            	TournamentRedisUtil.invalidateAll();
                return true;
            }
        }
        return false;
    }

    public Boolean deleteAllTeamFromTour( HttpServletRequest request , Integer tourId) throws Exception {
        
    	 if(!AuthUtil.isAuthorizedAdmin( request , "tournament", "tour_id", tourId))
         	throw new Exception("You cannot modify other's resouce");

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(DELETE_TOUR_TEAM)) {

            pstmt.setInt(1, tourId);
            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
            	TournamentRedisUtil.inValidateTournament(tourId);
            	TournamentRedisUtil.invalidateAll();
            	return true;   
            }
        }
        return false;
    }

    public Boolean deleteTeamFromTour( HttpServletRequest request,Integer tourId, Integer teamId) throws Exception {
    	
    	 if(!AuthUtil.isAuthorizedAdmin( request , "tournament", "tour_id", tourId))
         	throw new Exception("You cannot modify other's resouce");
        
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(DELETE_TOURTEAM_BY_TEAMID)) {

            pstmt.setInt(1, tourId);
            pstmt.setInt(2, teamId);
            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
            	TournamentRedisUtil.inValidateTournament(tourId);
            	TournamentRedisUtil.invalidateAll();
            	return true;
            } 
        }
        return false;
    }
    
    
    public boolean insertOrUpdateData ( HttpServletRequest request ,List<TournamentVO> tournamentsVO , Boolean isPut)throws Exception {
    	
    Integer userId = Integer.parseInt( AuthUtil.getUserId(request) );
    	
	for (TournamentVO tournamentVO : tournamentsVO) {
	            
			if(!tournamentVO.canPost())
				throw new Exception("Missing Parameter");
		
	        	if(isPut)
	        	{
	        		if(tournamentVO.getTourId() == null)
	        			throw new Exception("TourId is required to update");
	        		
	        		if(!AuthUtil.isAuthorizedAdmin(request, "tournament", "tour_id", tournamentVO.getTourId()))
	        			throw new Exception("You cannot modify other's resource");
	        	}
	        	
	        	Set<Integer> teamSet = new HashSet<>();
	            
	            if (!validateTourTeam(tournamentVO, teamSet))
	                return false;
	            
	            String sql = prepareSqlStatement(isPut, tournamentVO );
	            if (sql == null) 
	                return false;
	
	            try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
	                 PreparedStatement pstmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
	                
	                conn.setAutoCommit(false); 
	                
	                setPreparedStatementValues(pstmt, tournamentVO , userId);
	
	                int rowsAffected = pstmt.executeUpdate();
	                Integer tourId = tournamentVO.getTourId();
	              
	                if (!isPut) {
	                    tourId = getGeneratedTourId(pstmt);
	                }
	                
	                
	
	                if (tourId != null) {
	                    addTeamsToTour(conn, teamSet, tourId , tournamentVO);
	                }
	
	                if (rowsAffected > 0) {
	                	
	                	TournamentRedisUtil.invalidateAll();
	                	
	                	conn.commit();
	                    return true;
	                    
	                } else {
	                    conn.rollback();
	                    return false;
	                }
	
	            } 
	        }
    	
    	return false;
    }
    
	
	
}
