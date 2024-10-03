package repository;

import java.lang.reflect.Type;
import java.sql.*;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import model.PlayerVO;
import model.TeamVO;
import model.UserVO;
import utils.AuthUtil;
import utils.TeamRedisUtil;

public class TeamDAO {
   
	private static final String DB_URL = "jdbc:mysql://localhost:3306/CricketBuzz";
    private static final String USER = "root";
    private static final String PASS = "";
    
    private final String GET_TEAM_BY_ID = "SELECT U.user_id , t.created_by, U.name as uname , t.date_created , U.email ,  t.team_id, t.name AS team_name, c.id AS captain_id , c.name AS captain_name, vc.id AS vice_captain_id ,vc.name AS vice_captain_name, GROUP_CONCAT(CONCAT(p.id, ':', p.name, ':' , p.rating) SEPARATOR ', ') AS players FROM team t LEFT JOIN player c ON t.captain_id = c.id LEFT JOIN player vc ON t.vice_captain_id = vc.id LEFT JOIN team_player tp ON t.team_id = tp.team_id LEFT JOIN player p ON tp.player_id = p.id LEFT JOIN user AS U ON U.user_id = t.created_by  WHERE t.team_id = ?  GROUP BY t.team_id ";
    
    public TeamVO getTeamById(int teamId) throws SQLException {
        
    	TeamVO team = TeamRedisUtil.getOne(teamId);
    	
    	if(team != null)
    		return team;
    	
    	team = new TeamVO();

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             java.sql.PreparedStatement stmt = conn.prepareStatement(GET_TEAM_BY_ID)) {
        	
        	
        	stmt.setInt(1, teamId);
        	
        	ResultSet rs = stmt.executeQuery();	

            if (rs.next()) {
                team.setTeamId(rs.getInt("team_id"));
                team.setName(rs.getString("team_name"));
                team.setCaptainName(rs.getString("captain_name"));
                team.setViceCaptainName(rs.getString("vice_captain_name"));
                team.setCaptainId(rs.getInt("captain_id"));
                team.setViceCaptainId(rs.getInt("vice_captain_id"));
                
                UserVO creator = new UserVO();
                creator.setId(rs.getInt("created_by"));
                creator.setName(rs.getString("uname"));
                creator.setDateCreated(rs.getString("date_created"));
                creator.setEmail(rs.getString("email"));
                
                team.setCreator(creator);
                
                String playersString = rs.getString("players");
                List<PlayerVO> players = new ArrayList<>();
                if (playersString != null) {
                    String[] playersArray = playersString.split(", ");
                    for (String playerData : playersArray) {
                        String[] playerInfo = playerData.split(":");
                        PlayerVO player = new PlayerVO();
                        player.setId(Integer.parseInt(playerInfo[0]));
                        player.setName(playerInfo[1]);
                        player.setRating(Double.parseDouble(playerInfo[2]));
                        players.add(player);
                    }
                }
                team.setPlayers(players);
            }
            if(team != null)
            	TeamRedisUtil.setTeamById(team, teamId);
            
            return team;
        }

    }
    
    public boolean addTeamAndPlayers(HttpServletRequest request , String json, Boolean isPut) throws SQLException, Exception {

    	
        Type type = new TypeToken<List<TeamVO>>(){}.getType();
        List<TeamVO> teams = new Gson().fromJson(json, type);

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            conn.setAutoCommit(false);

            String insertTeamSQL = "INSERT INTO team (name, category, captain_id, vice_captain_id, wicket_keeper_id , created_by ) VALUES (?, ?, ?, ?, ? , ?)";
            String updateTeamSQL = "UPDATE team SET name = ?, category = ?, captain_id = ?, vice_captain_id = ?, wicket_keeper_id = ? WHERE team_id = ?";
            String insertTeamPlayerSQL = "INSERT INTO team_player (team_id, player_id) VALUES (?, ?)";
            String deleteTeamPlayerSQL = "DELETE FROM team_player WHERE team_id = ? AND player_id = ?";
            String selectTeamPlayersSQL = "SELECT player_id FROM team_player WHERE team_id = ?";

            try (PreparedStatement teamStmt = conn.prepareStatement(isPut ? updateTeamSQL : insertTeamSQL, Statement.RETURN_GENERATED_KEYS );
                 PreparedStatement teamPlayerStmt = conn.prepareStatement(insertTeamPlayerSQL);
                 PreparedStatement deletePlayerStmt = conn.prepareStatement(deleteTeamPlayerSQL);
                 PreparedStatement selectPlayerStmt = conn.prepareStatement(selectTeamPlayersSQL)) {
            	
            	String userIdStr = AuthUtil.getUserId(request);
            	
            	if(userIdStr == null)throw new Exception("Not a Valid user");
            	
            	Integer userId = Integer.parseInt(userIdStr);
            	
                for (TeamVO team : teams) {
                	
                	
                	if(team.getTeamId() == null && isPut)
                		throw new Exception("ID is required for team update");
                	
                	if(isPut && !AuthUtil.isAuthorizedAdmin(request, "team", "team_id" , team.getTeamId() ))
                		throw new Exception("You cannot modify another's resource");
                	
                    if (!team.canPost())
                        throw new Exception("Check the input data");

                    teamStmt.setString(1, team.getName());
                    teamStmt.setString(2, team.getCategory());
                    teamStmt.setInt(3, team.getCaptainId());
                    teamStmt.setInt(4, team.getViceCaptainId());
                    teamStmt.setInt(5, team.getWicketKeeperId());

                    if (isPut) {
                        teamStmt.setInt(6, team.getTeamId());
                        teamStmt.executeUpdate();
                    } else {
                    	teamStmt.setInt(6, userId);
                        teamStmt.executeUpdate();
                        ResultSet rs = teamStmt.getGeneratedKeys();
                        if (rs.next()) {
                            team.setTeamId(rs.getInt(1)); 
                        }
                    }
                    
                    
                    List<Integer> existingPlayers = new ArrayList<>();
                    if (isPut) {
                        selectPlayerStmt.setInt(1, team.getTeamId());
                        ResultSet rs = selectPlayerStmt.executeQuery();
                        while (rs.next()) {
                            existingPlayers.add(rs.getInt("player_id"));
                        }

                        for (Integer playerId : existingPlayers) {
                            if (!team.getPlayersList().contains(playerId)) {
                                deletePlayerStmt.setInt(1, team.getTeamId());
                                deletePlayerStmt.setInt(2, playerId);
                                deletePlayerStmt.executeUpdate();
                            }
                        }
                    }

                    if (team.getPlayersList().size() < 11)
                        throw new Exception("Add at least 11 players to form a team");

                    if (!team.getPlayersList().contains(team.getCaptainId()))
                        throw new Exception("Captain ID " + team.getCaptainId() + " is not in players list");

                    if (!team.getPlayersList().contains(team.getViceCaptainId()))
                        throw new Exception("Vice Captain ID " + team.getViceCaptainId() + " is not in players list");

                    if (!team.getPlayersList().contains(team.getWicketKeeperId()))
                        throw new Exception("Wicket Keeper ID " + team.getWicketKeeperId() + " is not in players list");

                    if (team.getCaptainId() == team.getViceCaptainId())
                        throw new Exception("Captain and Vice Captain cannot be the same");

                    for (Integer playerId : team.getPlayersList()) {
                        if (isPut && existingPlayers.contains(playerId)) {
                            continue;
                        }
                        teamPlayerStmt.setInt(1, team.getTeamId());
                        teamPlayerStmt.setInt(2, playerId);
                        teamPlayerStmt.executeUpdate();
                    }
                }
                
                if(isPut)
                for(TeamVO team : teams)
                	TeamRedisUtil.inValidateTeam(team.getTeamId());
                
                conn.commit();
                System.out.println("Data inserted/updated successfully.");
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        }
        return true;
    }
    
    public boolean deleteTeamById( HttpServletRequest request , Integer teamId) throws Exception {
    	
    	if(!AuthUtil.isAuthorizedAdmin(request, "team", "team_id", teamId ))
    		throw new Exception("You cannot modify another's resource");
    	
    	String deleteSQL = "DELETE FROM team WHERE team_id = ?";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement stmt = conn.prepareStatement(deleteSQL)) {

            stmt.setInt(1, teamId);
            
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
            	TeamRedisUtil.inValidateTeam(teamId);
                System.out.println("Team with team_id " + teamId + " deleted successfully.");
                return true;
            } else {
                System.out.println("No team found with team_id " + teamId);
                return false;
            }
        }
    }
    
    public boolean deleteTeamPlayersByTeamId(HttpServletRequest request , int teamId) throws Exception {
    	
    	if(!AuthUtil.isAuthorizedAdmin(request, "team", "team_id", teamId ))
    		throw new Exception("You cannot modify another's resource");
    	
        String deleteSQL = "DELETE FROM team_player WHERE team_id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement stmt = conn.prepareStatement(deleteSQL)) {

            stmt.setInt(1, teamId);
            
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
            	TeamRedisUtil.inValidateTeam(teamId);
                System.out.println("Players associated with team_id " + teamId + " deleted successfully.");
                return true;
            } else {
                System.out.println("No players found for team_id " + teamId);
                return false;
            }
        }
    }
    
    
    public boolean deleteTeamPlayerByPlayerIdTeamId( HttpServletRequest request ,Integer teamId , Integer playerId) throws Exception {
    	
    	if(!AuthUtil.isAuthorizedAdmin(request, "team", "team_id", teamId ))
    		throw new Exception("You cannot modify another's resource");
    	
        String deleteSQL = "DELETE FROM team_player WHERE team_id = ? AND player_id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement stmt = conn.prepareStatement(deleteSQL)) {

            stmt.setInt(1, teamId);
            stmt.setInt(2, playerId);
            
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
            	TeamRedisUtil.inValidateTeam(teamId);
                System.out.println("Players associated with team_id " + teamId + " deleted successfully.");
                return true;
            } else {
                System.out.println("No players found for team_id " + teamId);
                return false;
            }
        }
    }


    

}
