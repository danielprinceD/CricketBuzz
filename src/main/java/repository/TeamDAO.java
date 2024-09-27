package repository;
import java.io.IOException;
import java.io.PrintWriter;
import java.rmi.ServerException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import model.*;
import utils.TeamRedisUtil;
import controller.*;

public class TeamDAO {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/CricketBuzz";
    private static final String USER = "root";
    private static final String PASS = "";
    
    PreparedStatement preparedStatement = null;
    Connection connection = null;
    
    
    public TeamVO getTeamByID(int teamId) throws SQLException {
    	
        String sql = "SELECT * FROM team WHERE team_id = ?";
        TeamVO team = TeamRedisUtil.getOne(teamId);

        if(team != null)
        	return team;
        
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, teamId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                team = new TeamVO();
                team.setTeamId(rs.getInt("team_id"));
                team.setName(rs.getString("name"));
                team.setCaptainId(rs.getInt("captain_id"));
                team.setViceCaptainId(rs.getInt("vice_captain_id"));
                team.setWicketKeeperId(rs.getInt("wicket_keeper_id"));
                team.setCategory(rs.getString("category"));
                
                List<Integer> players = getPlayersForTeam(team.getTeamId()); 
                team.setTeamPlayers(players);
                
                if(TeamRedisUtil.isCached())
                	TeamRedisUtil.setTeamById(team , teamId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return team;
    }

    
    public List<TeamVO> getTeams() throws SQLException {
        List<TeamVO> teams = TeamRedisUtil.getAll();
        String sql = "SELECT * FROM team";
        
        if(teams.size() > 0)
        	return teams;
        
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                TeamVO team = new TeamVO();
                team.setTeamId(rs.getInt("team_id"));
                team.setName(rs.getString("name"));
                team.setCaptainId(rs.getInt("captain_id"));
                team.setViceCaptainId(rs.getInt("vice_captain_id"));
                team.setWicketKeeperId(rs.getInt("wicket_keeper_id"));
                team.setCategory(rs.getString("category"));
                team.setTeamPlayers(getPlayersForTeam(team.getTeamId()));
                teams.add(team);
            }
        }
        if(!teams.isEmpty())
        	TeamRedisUtil.setAllTeams(teams);
        return teams;
    }

    private List<Integer> getPlayersForTeam(int teamId) throws SQLException {
        List<Integer> playerIds = new ArrayList<>();
        String sql = "SELECT TP.player_id FROM team_player AS TP WHERE TP.team_id = ?";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, teamId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                playerIds.add(rs.getInt("player_id"));
            }
        }

        return playerIds;
    }
    
    private boolean isPLayer(int playerId) {
        String sql = "SELECT COUNT(*) FROM player WHERE id = ?";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, playerId);

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
    
    
    public String prepareSqlStatement(HttpServletRequest request, TeamVO teamVO, HttpServletResponse response, PrintWriter out) {
        if (teamVO.getTeamId() < 0) {
            return "INSERT INTO team (captain_id, vice_captain_id, wicket_keeper_id, category, name) VALUES (?, ?, ?, ?, ?)";
        } else if (request.getMethod().equalsIgnoreCase("PUT")) {
            return "UPDATE team SET captain_id = ?, vice_captain_id = ?, wicket_keeper_id = ?, category = ?, name = ? WHERE team_id = ?";
        } else {
            Extra.sendError(response, out, "Missing Parameters");
            return null;
        }
    }

    public void setPreparedStatementValues(PreparedStatement pstmt, TeamVO teamVO) throws SQLException {
        pstmt.setInt(1, teamVO.getCaptainId());
        pstmt.setInt(2, teamVO.getViceCaptainId());
        pstmt.setInt(3, teamVO.getWicketKeeperId());
        pstmt.setString(4, teamVO.getCategory());
        pstmt.setString(5, teamVO.getName());

        if (teamVO.getTeamId() > 0) {
            pstmt.setInt(6, teamVO.getTeamId());
        }
    }
    
    public int getGeneratedTeamId(PreparedStatement pstmt) throws SQLException {
        try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
            if (generatedKeys.next()) {
                return generatedKeys.getInt(1);
            }
        }
        return -1;
    }
    
    public void saveTeam(TeamVO teamVO, HttpServletRequest request, HttpServletResponse response, PrintWriter out) throws SQLException {
        String sql = prepareSqlStatement(request, teamVO, response, out);
        if (sql == null) return;

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            conn.setAutoCommit(false);
            setPreparedStatementValues(pstmt, teamVO);

            int rowsAffected = pstmt.executeUpdate();
            int teamId = teamVO.getTeamId();

            if (teamId < 0 && request.getMethod().equalsIgnoreCase("POST")) {
                teamId = getGeneratedTeamId(pstmt);
                teamVO.setTeamId(teamId); 
            }
            
            HashSet<Integer> playerSet = new HashSet<>(teamVO.getTeamPlayers());
            	
            addPlayersToTeam(conn, playerSet , teamVO.getTeamId());

            if (rowsAffected > 0) {
                conn.commit();
                Extra.sendSuccess(response, out, "Team and players inserted/updated successfully");
            } else {
                conn.rollback();
                Extra.sendError(response, out, "Failed to insert/update team");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            Extra.sendError(response, out, e.getMessage());
        }
    }
    
    private boolean isValidTeamPlayers(int playerId) {
    	String checkPlayerSql = "SELECT COUNT(*) FROM player WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(checkPlayerSql)) {
             
            pstmt.setInt(1, playerId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0; 
            }
        } catch (SQLException e) {
            e.printStackTrace(); 
        }
        return false;
    }
    
    public void addPlayersToTeam(Connection conn, Set<Integer> playerSet, int teamId) throws SQLException {
    	
    	String clearQuery = "DELETE FROM team_player WHERE team_id = ?";
        try (PreparedStatement clearPstmt = conn.prepareStatement(clearQuery)) {
            clearPstmt.setInt(1, teamId);
            clearPstmt.executeUpdate();
        }
    	
    	String query = "INSERT INTO team_player (player_id, team_id) VALUES (?, ?)";
        try (PreparedStatement playerPstmt = conn.prepareStatement(query)) {
            for (Integer playerId : playerSet) {
            	
            	if(!isValidTeamPlayers(playerId))
            		throw new SQLException("Player ID " + playerId + " is not a player");
            	
            	if(isAlreadyInTeam(playerId , teamId , conn ))
            		throw new SQLException("Player ID " + playerId + " is already in this Team ID "+ teamId);
            	
            	playerPstmt.setInt(1, playerId);
                playerPstmt.setInt(2, teamId);
                playerPstmt.executeUpdate();
            }
        }
    }
    
    public void addTeams(HttpServletRequest request , HttpServletResponse response ,List<TeamVO> teams) throws ServerException , IOException {
		
    	PrintWriter out = response.getWriter();
    	for (TeamVO teamVO : teams) {
		        	
		        	if(teamVO == null)
		        	{
		        		Extra.sendError(response, out, "Not a Valid JSON Body");
		        		return;
		        	}
		        	
		            Set<Integer> playerSet = new HashSet<>();
		            
		            
		            
		            if (!validatePlayers(teamVO, playerSet, response, out)) return;
		           
		            String sql =prepareSqlStatement(request, teamVO, response, out);
		            
		            if (sql == null) return;
		
		            try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
		                 PreparedStatement pstmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
		            	
		                conn.setAutoCommit(false);
		                setPreparedStatementValues(pstmt, teamVO);
		
		                int rowsAffected = pstmt.executeUpdate();
		                int teamId = teamVO.getTeamId();
		          
		                if (teamId < 0 && request.getMethod().equalsIgnoreCase("POST")) {
		                    teamId = getGeneratedTeamId(pstmt);
		                    teamVO.setTeamId(teamId);
		                }
		
		                if (teamId > 0 ){
		                    addPlayersToTeam(conn, playerSet, teamId);
		                }
		
		                if (rowsAffected > 0) {
		                    conn.commit();
		                    
		                    
		                    
		                    Extra.sendSuccess(response, out, "Team and players inserted/updated successfully");
		                } else {
		                    conn.rollback();
		                    Extra.sendError(response, out, "Failed to insert/update team");
		                }
		
		            } catch (SQLException e) {
		                Extra.sendError(response, out, e.getMessage());
		                e.printStackTrace();
		            }
		        }  
    	
    	if(TeamRedisUtil.isCached())
        	TeamRedisUtil.setAllTeams(teams);
    }
    
    
    private boolean isAlreadyInTeam(int playerId, int teamId, Connection conn) {
        
    	String query = "SELECT COUNT(*) FROM team_player WHERE player_id = ? AND team_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, playerId);
            pstmt.setInt(2, teamId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0; 
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false; 
    }

    
    public boolean validatePlayers(TeamVO teamVO, Set<Integer> playerSet, HttpServletResponse response, PrintWriter out) {
       
    	if(teamVO.getTeamPlayers() == null)
    	{
    		return true;
    	}
    	
    	for (Integer player : teamVO.getTeamPlayers()) {
            if (playerSet.contains(player)) {
                Extra.sendError(response, out, "Player cannot be added more than once");
                return false;
            }
            playerSet.add(player);
        }

        if (playerSet.size() < 11) {
            Extra.sendError(response, out, "Add more than 11 Players. Add " + (11 - playerSet.size()) + " more players");
            return false;
        }
        
        
        if (!playerSet.contains(teamVO.getCaptainId())) {
            Extra.sendError(response, out, "Captain ID is not in Players List");
            return false;
        }
        if (!playerSet.contains(teamVO.getWicketKeeperId())) {
            Extra.sendError(response, out, "WicketKeeper ID is not in Players List");
            return false;
        }
        if (!playerSet.contains(teamVO.getViceCaptainId())) {
            Extra.sendError(response, out, "Vice Captain ID is not in Players List");
            return false;
        }
        if (teamVO.getCaptainId() == teamVO.getViceCaptainId()) {
            Extra.sendError(response, out, "Captain and Vice Captain Cannot be Same");
            return false;
        }

        if(!isPLayer(teamVO.getCaptainId()))
        {
        	 Extra.sendError(response, out, "Captain ID " + teamVO.getCaptainId() + " is not a player");
        	 return false;
        }
        
        if(!isPLayer(teamVO.getWicketKeeperId()))
        {
        	 Extra.sendError(response, out,"Wicket-Keeper ID " + teamVO.getWicketKeeperId() + " is not a player");
        	 return false;
        }
        if(!isPLayer(teamVO.getViceCaptainId()))
        {
        	 Extra.sendError(response, out, "Vice-Captain ID " + teamVO.getViceCaptainId() + " is not a player");
        	 return false;
        }
        	
        return true;
    }
    
    
    public void deleteOnePlayerFromTeam(HttpServletResponse response , PrintWriter out , String teamId , String playerId) {
    	
    	String countQuery = "SELECT COUNT(*) FROM team_player WHERE team_id = ?";
    	String checkPlayerRoleQuery = "SELECT captain_id, vice_captain_id, wicket_keeper_id FROM team WHERE team_id = ?";
    	String deleteQuery = "DELETE FROM team_player WHERE team_id = ? AND player_id = ?";
    	
    	try {
    	     connection = DriverManager.getConnection(DB_URL, USER, PASS);
    	    
    	    try (PreparedStatement roleStatement = connection.prepareStatement(checkPlayerRoleQuery)) {
                roleStatement.setInt(1, Integer.parseInt(teamId));
                ResultSet roleResultSet = roleStatement.executeQuery();
                
                if (roleResultSet.next()) {
                    int captainId = roleResultSet.getInt("captain_id");
                    int viceCaptainId = roleResultSet.getInt("vice_captain_id");
                    int wicketKeeperId = roleResultSet.getInt("wicket_keeper_id");
                    
                    if (Integer.parseInt(playerId) == captainId || Integer.parseInt(playerId) == viceCaptainId || Integer.parseInt(playerId) == wicketKeeperId) {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        Extra.sendError(response, out, "Cannot remove this player as they are the captain, vice-captain, or wicket-keeper.");
                        return;
                    }
                }
            }
    	    
    	    
    	    try (PreparedStatement countStatement = connection.prepareStatement(countQuery)) {
    	        countStatement.setInt(1, Integer.parseInt(teamId));
    	        ResultSet resultSet = countStatement.executeQuery();
    	        
    	        int currentPlayerCount = 0;
    	        if (resultSet.next()) {
    	            currentPlayerCount = resultSet.getInt(1);
    	        }

    	        if (currentPlayerCount > 11) {
    	            preparedStatement = connection.prepareStatement(deleteQuery);
    	            preparedStatement.setInt(1, Integer.parseInt(teamId));
    	            preparedStatement.setInt(2, Integer.parseInt(playerId));

    	            int rowsAffected = preparedStatement.executeUpdate();

    	            if (rowsAffected > 0) {
    	            	Extra.sendError(response, out, "Player with ID " + playerId + " has been removed from team " + teamId);
    	            } else {
    	                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    	                Extra.sendError(response, out, "No matching player found for deletion.");
    	            }
    	        } else {
    	            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    	            Extra.sendError(response, out, "Cannot remove player. Team must have more than 11 players.");
    	        }
    	    }
    	} catch (SQLException e) {
    	    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    	    Extra.sendError(response, out, "Database error: " + e.getMessage());
    	    e.printStackTrace();
    	} catch (NumberFormatException e) {
    	    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    	    Extra.sendError(response, out, "Invalid team or player ID format.");
    	}

    }
    
   
    
    
    public void deleteOneTeam(HttpServletResponse response , PrintWriter out , String teamId) {
    	
    	String query = "DELETE FROM team where team_id = ?";
    	
    	try {
            connection = DriverManager.getConnection(DB_URL, USER, PASS);
            
            preparedStatement = connection.prepareStatement(query);
            
            preparedStatement.setInt(1, Integer.parseInt(teamId));
            
            int rowsAffected = preparedStatement.executeUpdate();
            
            if (rowsAffected > 0) {
            	
            	if(TeamRedisUtil.isCached())
            		TeamRedisUtil.deleteOne(Integer.parseInt(teamId));
            	
                out.println("Team has been removed");
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.println("No matching player found for deletion.");
            }
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.println("Database error: " + e.getMessage());
            e.printStackTrace();
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.println("Invalid team or player ID format.");
        } finally {
            try {
                if (preparedStatement != null) preparedStatement.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    	
    }
    
    
    
}
