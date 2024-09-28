package repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
}
