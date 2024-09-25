package DAO;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import Model.TournamentTeamVO;
import Model.TournamentVO;
import Servlet.Extra;


public class TournamentDAO {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/CricketBuzz";
    private static final String USER = "root";
    private static final String PASS = "";

    public List<TournamentVO> getAllTournaments() throws SQLException {
        List<TournamentVO> tournaments = new ArrayList<>();
        String sql = "SELECT * FROM tournament";

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                TournamentVO tournament = new TournamentVO();
                tournament.setTourId(rs.getInt("tour_id"));
                tournament.setName(rs.getString("name"));
                tournament.setStartDate(rs.getString("start_date"));
                tournament.setEndDate(rs.getString("end_date"));
                tournament.setMatchCategory(rs.getString("match_category"));
                tournament.setSeason(rs.getInt("season"));
                tournament.setStatus(rs.getString("status"));

                List<TournamentTeamVO> teams = getTeamsByTournamentId(tournament.getTourId());
                tournament.setParticipatedTeams(teams);

                tournaments.add(tournament);
            }
        }
        return tournaments;
    }

    public TournamentVO getTournamentById(int tourId) throws SQLException {
        TournamentVO tournament = null;
        String sql = "SELECT * FROM tournament WHERE tour_id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

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
                tournament.setParticipatedTeams(teams);
            }
        }
        return tournament;
    }

    private List<TournamentTeamVO> getTeamsByTournamentId(int tourId) throws SQLException {
        List<TournamentTeamVO> teams = new ArrayList<>();
        String sql = "SELECT T.team_id, TT.points, TT.net_run_rate " +
                     "FROM tournament_team TT " +
                     "JOIN team T ON TT.team_id = T.team_id " +
                     "WHERE TT.tour_id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, tourId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
            	TournamentTeamVO team = new TournamentTeamVO();
                team.setTeamId(rs.getInt("team_id"));
                team.setPoints(rs.getInt("points"));
                team.setNetRunRate(rs.getDouble("net_run_rate"));
                teams.add(team);
            }
        }
        return teams;
    }

    public void addTournament(TournamentVO tournament) throws SQLException {
        String sql = "INSERT INTO tournament (name, start_date, end_date, match_category, season, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

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
        String sql = "INSERT INTO tournament_team (tour_id, team_id, points, net_run_rate) VALUES (?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (TournamentTeamVO tournamentTeamVO : tournament.getParticipatedTeams()) {
                stmt.setInt(1, tournament.getTourId());
                stmt.setInt(2, tournamentTeamVO.getTeamId());
                stmt.setInt(3, tournamentTeamVO.getPoints());
                stmt.setDouble(4, tournamentTeamVO.getNetRunRate());
                stmt.executeUpdate();
            }
        }
    }
    
    public String prepareSqlStatement(HttpServletRequest request, TournamentVO tournamentModel, HttpServletResponse response, PrintWriter out) {

        if (tournamentModel.getTourId() < 0 && tournamentModel.isValid() ) {
            return "INSERT INTO tournament (name, start_date, end_date, match_category, season , status) VALUES (?, ?, ?, ?, ? , ? )";
        } else if (tournamentModel.isValid() && request.getMethod().equalsIgnoreCase("PUT")) {
            return "UPDATE tournament SET name = ?, start_date = ?, end_date = ?, match_category = ?, season = ? , status = ? WHERE tour_id = ?";
        } else {
            Extra.sendError(response, out, "Invalid data or missing parameters.");
            return null;
        }
    }
    
	public boolean validateTourTeam(TournamentVO teamModel, Set<Integer> teamSet, HttpServletResponse response, PrintWriter out) {
	    
			if(teamModel.getParticipatedTeams() == null)
				return true;
		
	    	for (TournamentTeamVO team : teamModel.getParticipatedTeams() ) {
	            if (teamSet.contains(team.getTeamId())) {
	                Extra.sendError(response, out, "Team cannot be added more than once");
	                return false;
	            }
	            teamSet.add(team.getTeamId());
	        }
	
	        return true;
	    }
	
	
	public void addTeamsToTour(Connection conn, Set<Integer> teamSet, int tourId , TournamentVO tourModel) throws SQLException {
    	
		String selectQuery = "SELECT team_id FROM tournament_team WHERE tour_id = ?";
	    
		Set<Integer> existingTeamIds = new HashSet<>();

	    try (PreparedStatement selectPstmt = conn.prepareStatement(selectQuery)) {
	        selectPstmt.setInt(1, tourId);
	        ResultSet resultSet = selectPstmt.executeQuery();

	        while (resultSet.next()) {
	            existingTeamIds.add(resultSet.getInt("team_id"));
	        }
	    }

	    Set<Integer> teamsToRemove = new HashSet<>(existingTeamIds);
	    teamsToRemove.removeAll(teamSet);
	    	
	    if (!teamsToRemove.isEmpty()) {
	        String deleteQuery = "DELETE FROM tournament_team WHERE tour_id = ? AND team_id = ?";
	        try (PreparedStatement deletePstmt = conn.prepareStatement(deleteQuery)) {
	            for (Integer teamId : teamsToRemove) {
	                deletePstmt.setInt(1, tourId);
	                deletePstmt.setInt(2, teamId);
	                deletePstmt.executeUpdate();
	            }
	        }
	    }
	    
	    String insertOrUpdateQuery = "INSERT INTO tournament_team (tour_id, team_id, points, net_run_rate) VALUES (?, ?, ?, ?) " +
		                    "ON DUPLICATE KEY UPDATE points = VALUES(points), net_run_rate = VALUES(net_run_rate)";
		
		try (PreparedStatement pstmt = conn.prepareStatement(insertOrUpdateQuery)) {
			
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
		    
			String query = "SELECT COUNT(*) FROM team WHERE team_id = ?";
		    boolean exists = false;
	
		    try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
		         PreparedStatement pstmt = conn.prepareStatement(query)) {
		        
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
    
    
	
	public void setPreparedStatementValues(PreparedStatement pstmt, TournamentVO tourModel) throws SQLException {
	        pstmt.setString(1, tourModel.getName());
	        pstmt.setString(2, tourModel.getStartDate());
	        pstmt.setString(3, tourModel.getEndDate());
	        pstmt.setString(4, tourModel.getMatchCategory());
	        pstmt.setInt(5, tourModel.getSeason());
	        pstmt.setString(6 , tourModel.getStatus());
	        if (tourModel.getTourId() > 0) {
	            pstmt.setInt(7, tourModel.getTourId());
	        }
    }
	
	
	public void deleteTournament(HttpServletResponse response, PrintWriter out, String tourId) throws ServletException, IOException {
        if (tourId == null) {
            Extra.sendError(response, out, "Tournament ID is required");
            return;
        }

        String sql = "DELETE FROM tournament WHERE tour_id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, Integer.parseInt(tourId));
            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                Extra.sendSuccess(response, out, "Tournament deleted successfully");
            } else {
                Extra.sendError(response, out, "No tournament found with the provided ID");
            }
        } catch (NumberFormatException e) {
            Extra.sendError(response, out, "Invalid Tournament ID");
        } catch (SQLException e) {
            Extra.sendError(response, out, e.getMessage());
            e.printStackTrace();
        }
    }

    public void deleteAllTeamFromTour(HttpServletResponse response, PrintWriter out, String tourId) {
        
    	String sql = "DELETE FROM tournament_team WHERE tour_id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, Integer.parseInt(tourId));
            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                Extra.sendSuccess(response, out, "All teams deleted from the tournament successfully");
            } else {
                Extra.sendError(response, out, "No teams found for the provided tournament ID");
            }
        } catch (NumberFormatException e) {
            Extra.sendError(response, out, "Invalid Tournament ID");
        } catch (SQLException e) {
            Extra.sendError(response, out, e.getMessage());
            e.printStackTrace();
        }
    }

    public void deleteTeamFromTour(HttpServletResponse response, PrintWriter out, String tourId, String teamId) {
        String sql = "DELETE FROM tournament_team WHERE tour_id = ? AND team_id = ?";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, Integer.parseInt(tourId));
            pstmt.setInt(2, Integer.parseInt(teamId));
            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                Extra.sendSuccess(response, out, "Team deleted from the tournament successfully");
            } else {
                Extra.sendError(response, out, "No team found with the provided IDs");
            }
        } catch (NumberFormatException e) {
            Extra.sendError(response, out, "Invalid Tournament or Team ID");
        } catch (SQLException e) {
            Extra.sendError(response, out, e.getMessage());
            e.printStackTrace();
        }
    }
	
	
}
