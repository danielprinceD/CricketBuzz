package repository;

import java.lang.reflect.Type;
import java.sql.*;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import jakarta.validation.Path.ReturnValueNode;
import model.PlayerVO;
import model.TeamVO;

public class TeamDAO {
   
	private static final String DB_URL = "jdbc:mysql://localhost:3306/CricketBuzz";
    private static final String USER = "root";
    private static final String PASS = "";
    
    private final String GET_TEAM_BY_ID = "SELECT t.team_id, t.name AS team_name, c.id AS captain_id , c.name AS captain_name, vc.id AS vice_captain_id ,vc.name AS vice_captain_name, GROUP_CONCAT(CONCAT(p.id, ':', p.name, ':' , p.rating) SEPARATOR ', ') AS players FROM team t LEFT JOIN player c ON t.captain_id = c.id LEFT JOIN player vc ON t.vice_captain_id = vc.id LEFT JOIN team_player tp ON t.team_id = tp.team_id LEFT JOIN player p ON tp.player_id = p.id WHERE t.team_id = ?  GROUP BY t.team_id ";
    
    public TeamVO getTeamById(int teamId) throws SQLException {
        


        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             java.sql.PreparedStatement stmt = conn.prepareStatement(GET_TEAM_BY_ID);
             ) {
        	
        	stmt.setInt(1, teamId);
        	
        	ResultSet rs = stmt.executeQuery();	

        	TeamVO team = new TeamVO();
            if (rs.next()) {
                team.setTeamId(rs.getInt("team_id"));
                team.setName(rs.getString("team_name"));
                team.setCaptainName(rs.getString("captain_name"));
                team.setViceCaptainName(rs.getString("vice_captain_name"));
                team.setCaptainId(rs.getInt("captain_id"));
                team.setViceCaptainId(rs.getInt("vice_captain_id"));

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
            return team;
        }

    }
    
    
    public boolean addTeamAndPlayers(String json) throws SQLException , Exception {
    	
    	Type type = new TypeToken<List<TeamVO>>(){}.getType();
    	
    	List<TeamVO> teams = new Gson().fromJson( json , type);

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            conn.setAutoCommit(false);
            
            String insertTeamSQL = "INSERT INTO team (name, category , captain_id , vice_captain_id , wicket_keeper_id ) VALUES (?, ? , ? , ? , ?)";
           
            String insertTeamPlayerSQL = "INSERT INTO team_player (team_id, player_id) VALUES (?, ?)";
            
            try (PreparedStatement teamStmt = conn.prepareStatement(insertTeamSQL, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement teamPlayerStmt = conn.prepareStatement(insertTeamPlayerSQL)) {

                for ( TeamVO team : teams) {
                    
                	if(!team.canPost())
                		throw new Exception("Check the input data");
                	
                	
                    teamStmt.setString(1, team.getName());
                    teamStmt.setString(2, team.getCategory());
                    teamStmt.setInt(3, team.getCaptainId());
                    teamStmt.setInt(4, team.getViceCaptainId());
                    teamStmt.setInt(5, team.getWicketKeeperId());
                    teamStmt.executeUpdate();
                    
                    
                    ResultSet rs = teamStmt.getGeneratedKeys();
                    if (rs.next()) {
                         team.setTeamId(rs.getInt(1));
                    }
                    
                    if(team.getPlayersList().size() < 11)
                    	throw new Exception("Add atleast 11 players to form a team");
                    
                    if(!team.getPlayersList().contains(team.getCaptainId()))
                    	throw new Exception("Captain ID " + team.getCaptainId() + " is not in players list");
                    
                	if(!team.getPlayersList().contains(team.getViceCaptainId()))
                		throw new Exception("Captain ID " + team.getViceCaptainId() + " is not in players list");
                	
            		if(!team.getPlayersList().contains(team.getWicketKeeperId()))
            			throw new Exception("Captain ID " + team.getWicketKeeperId() + " is not in players list");
                    
            		if(team.getCaptainId() == team.getViceCaptainId())
            			throw new Exception("Captain and Vice Captain cannot be same");
                    
                    for (Integer players : team.getPlayersList()) {
                        
                        teamPlayerStmt.setInt(1, team.getTeamId());
                        teamPlayerStmt.setInt(2, players);
                        teamPlayerStmt.executeUpdate();
                    }
                }

                conn.commit();
                System.out.println("Data inserted successfully.");
            }
    	
        }
        return true;
    }
}
