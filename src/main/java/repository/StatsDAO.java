package repository;

import java.util.ArrayList;
import java.util.List;

import Builder.TopPlayerBuilder;
import config.SQLConfig;
import java.sql.*;
import model.PlayersStatsVO;

class PlayerStatsBuilder{
	protected PlayersStatsVO playerStatsVO;
	protected ResultSet rs;
	public PlayerStatsBuilder addPlayerStats() throws SQLException {
		playerStatsVO.setPlayerId(rs.getInt("player_id"));
		playerStatsVO.setName(rs.getString("Player_name"));
		playerStatsVO.setRuns(rs.getInt("total_runs"));
		playerStatsVO.setBallsFaced(rs.getInt("BF"));
		playerStatsVO.setFours(rs.getInt("4s"));
		playerStatsVO.setSixes(rs.getInt("6s"));
		playerStatsVO.setFifties(rs.getInt("50s"));
		playerStatsVO.setHundreds(rs.getInt("100s"));
		playerStatsVO.setWicketsTaken(rs.getInt("WKTS"));
		playerStatsVO.setBallsBowled(rs.getInt("BB"));
		return this;
	}
	public PlayerStatsBuilder addTeamStats() throws SQLException{
		playerStatsVO.setTeamId(rs.getInt("team_id"));
		playerStatsVO.setTeamName(rs.getString("team_name"));
		return this;
	}
	
	public PlayerStatsBuilder addTournamentDetails() throws SQLException {
		playerStatsVO.setTourId(rs.getInt("tour_id"));
		playerStatsVO.setTourName(rs.getString("tour_name"));
		playerStatsVO.setYear(rs.getInt("year"));
		return this;
	}
	
	public PlayerStatsBuilder addTournamentCount() throws SQLException{
		playerStatsVO.setTournamentCount(rs.getInt("tournaments"));
		return this;
	}
	
	public PlayerStatsBuilder addMatchCount() throws SQLException{
		playerStatsVO.setMatchCount(rs.getInt("matches"));
		return this;
	}
	
	public PlayerStatsBuilder addStrikeRate()throws SQLException {
		playerStatsVO.setAvgRun(rs.getDouble("avg_run"));
		playerStatsVO.setBattingSR(rs.getDouble("batting_strike_rate"));
		playerStatsVO.setBowlingSR(rs.getDouble("bowling_strike_rate"));
		return this;
	}
	
	public PlayerStatsBuilder addPlayedRole() throws SQLException{
		playerStatsVO.setPlayedAsBatter(rs.getInt("played_batter"));
		playerStatsVO.setPlayedAsBowler(rs.getInt("played_bowler"));
		return this;
	}
	
	public PlayersStatsVO build() {
		return this.playerStatsVO;
	}
	public PlayerStatsBuilder(ResultSet rs) {
		playerStatsVO = new PlayersStatsVO();
		this.rs = rs;
	}
}

public class StatsDAO {
	
	protected final String ALL_PLAYER_STATS = "SELECT TP.player_id , P.name player_name , (SELECT COUNT(*) FROM tournament TC " +
			"INNER JOIN tournament_team TTC ON TTC.tour_id = TC.tour_id " +
			"INNER JOIN team_player TPC ON TPC.team_id = TTC.team_id WHERE TPC.player_id = TP.player_id) AS Tournaments, " +
			"(SELECT COUNT(*) FROM playing_11 PE WHERE PE.player_id = TP.player_id) Matches, " +
			"SUM(P11.runs) AS Total_Runs, SUM(balls_faced) BF, SUM(fours) 4s, SUM(sixes) 6s, SUM(fifties) 50s, SUM(hundreds) 100s, " +
			"SUM(wickets_taken) WKTS, SUM(balls_bowled) BB FROM team_player TP " +
			"LEFT JOIN playing_11 P11 ON P11.player_id = TP.player_id " +
			"INNER JOIN player P ON TP.player_id = p.id INNER JOIN fixture F ON F.fixture_id = P11.fixture_id " +
			"INNER JOIN tournament T ON T.tour_id = F.tour_id GROUP BY TP.player_id ORDER BY player_id;";
	
	
	protected final String TOP_PLAYER_BATTING_QUERY = "SELECT id As player_id , player_name , tour_id, tour_name , total_runs , BF FROM ( " +
		    "SELECT P.id, P.name as Player_Name , T.tour_id , T.name as Tour_Name , SUM(runs) AS Total_RUNS , SUM(balls_faced) AS BF " +
		    "FROM playing_11 INNER JOIN fixture ON fixture.fixture_id = playing_11.fixture_id " +
		    "INNER JOIN tournament as T ON T.tour_id = fixture.tour_id " +
		    "INNER JOIN player as P ON player_id = P.id " +
		    "GROUP BY tour_id , player_id ORDER BY tour_id , total_runs , BF ) AS RESULT " +
		    "WHERE (tour_id , Total_RUNS) in ( SELECT tour_id , MAX(TOP_SCORE) FROM ( " +
		    "SELECT T.tour_id , player_id , SUM(runs) AS TOP_SCORE FROM playing_11 " +
		    "INNER JOIN fixture ON fixture.fixture_id = playing_11.fixture_id " +
		    "INNER JOIN tournament as T ON T.tour_id = fixture.tour_id " +
		    "GROUP BY tour_id , player_id ) AS TOPPER GROUP BY tour_id );";

	protected final String TOP_PLAYER_BOWLING_QUERY = "SELECT id As player_id , player_name , tour_id, tour_name , total_wkts , BB FROM ( " +
		    "SELECT P.id, P.name as Player_Name , T.tour_id , T.name as Tour_Name , SUM(wickets_taken) AS total_wkts , SUM(balls_bowled) AS BB " +
		    "FROM playing_11 INNER JOIN fixture ON fixture.fixture_id = playing_11.fixture_id " +
		    "INNER JOIN tournament as T ON T.tour_id = fixture.tour_id " +
		    "INNER JOIN player as P ON player_id = P.id " +
		    "GROUP BY tour_id , player_id ORDER BY tour_id , total_wkts , BB ) AS RESULT " +
		    "WHERE (tour_id , total_wkts) in ( SELECT tour_id , MAX(TOP_SCORE) FROM ( " +
		    "SELECT T.tour_id , player_id , SUM(wickets_taken) AS TOP_SCORE FROM playing_11 " +
		    "INNER JOIN fixture ON fixture.fixture_id = playing_11.fixture_id " +
		    "INNER JOIN tournament as T ON T.tour_id = fixture.tour_id " +
		    "GROUP BY tour_id , player_id ) AS TOPPER GROUP BY tour_id );";
	
	
	protected final String TOP_PLAYER_BATTER_BY_TOUR_ID = "SELECT T.tour_id, T.name as tour_name , P.id AS player_id ,P.name as Player_Name , " +
            "SUM(runs) as Total_Runs , SUM(P11.balls_faced) AS BF " +
            "FROM tournament T " +
            "INNER JOIN fixture F ON F.tour_id = T.tour_id " +
            "INNER JOIN playing_11 P11 ON P11.fixture_id = F.fixture_id " +
            "INNER JOIN player P ON P.id = P11.player_id " +
            "WHERE T.tour_id = ? " +
            "GROUP BY player_id , T.tour_id " +
            "ORDER BY Total_Runs DESC , BF " +
            "LIMIT 1;";

	protected String TOP_PLAYER_BATTER_BY_YEAR = "SELECT YEAR(T.date_created) as Year, P.id AS player_id ,P.name as Player_Name, " +
            "SUM(runs) as Total_Runs, SUM(P11.balls_faced) AS BF " +
            "FROM tournament T " +
            "INNER JOIN fixture F ON F.tour_id = T.tour_id " +
            "INNER JOIN playing_11 P11 ON P11.fixture_id = F.fixture_id " +
            "INNER JOIN player P ON P.id = P11.player_id " +
            "WHERE YEAR(T.date_created) = ? " +
            "GROUP BY player_id, YEAR(T.date_created) " +
            "ORDER BY Total_Runs DESC, BF LIMIT 1;";
	
	
	protected String TOP_PLAYER_BOWLER_BY_TOUR_ID = "SELECT T.tour_id, T.name as tour_name, P.id as player_id ,P.name as Player_Name, " +
		               "SUM(wickets_taken) as total_wkts, SUM(P11.balls_bowled) AS BB " +
		               "FROM tournament T " +
		               "INNER JOIN fixture F ON F.tour_id = T.tour_id " +
		               "INNER JOIN playing_11 P11 ON P11.fixture_id = F.fixture_id " +
		               "INNER JOIN player P ON P.id = P11.player_id " +
		               "WHERE T.tour_id = ? " +
		               "GROUP BY player_id, T.tour_id " +
		               "ORDER BY total_wkts DESC, BB LIMIT 1;";

	
	protected String TOP_PLAYER_BOWLER_BY_YEAR = "SELECT YEAR(T.date_created) as Year, P.id as player_id, P.name as Player_Name, " +
		               "SUM(wickets_taken) as total_wkts, SUM(P11.balls_bowled) AS BB " +
		               "FROM tournament T " +
		               "INNER JOIN fixture F ON F.tour_id = T.tour_id " +
		               "INNER JOIN playing_11 P11 ON P11.fixture_id = F.fixture_id " +
		               "INNER JOIN player P ON P.id = P11.player_id " +
		               "WHERE YEAR(T.date_created) = ? " +
		               "GROUP BY player_id, YEAR(T.date_created) " +
		               "ORDER BY total_wkts DESC, BB LIMIT 1;";

	protected String PLAYER_MATCH_HISTORY = "SELECT T.tour_id, T.name AS tour_name , YEAR(T.date_created) AS Year, " +
            "P11.player_id, PL.name as Player_Name, team.team_id, team.name as Team_Name, " +
            "COUNT(DISTINCT(F.fixture_id)) AS matches, " +
            "SUM(P11.role LIKE 'Bat%') AS Played_Batter, SUM(P11.role LIKE 'Bowl%') AS Played_Bowler, " +
            "ROUND(AVG(runs), 2) AS Avg_Run, SUM(runs) AS TOTAL_RUNS, SUM(balls_faced) AS BF, " +
            "ROUND(((SUM(runs) / SUM(balls_faced)) * 100), 2) AS Batting_Strike_Rate, " +
            "SUM(wickets_taken) AS wkts, SUM(balls_bowled) AS BB, " +
            "ROUND((SUM(wickets_taken) / SUM(balls_bowled) * 100), 2) AS Bowling_Strike_Rate, " +
            "SUM(fours) AS 4s, SUM(sixes) AS 6s, SUM(P11.fifties) AS 50s, SUM(P11.hundreds) AS 100s " +
            "FROM playing_11 AS P11 " +
            "JOIN fixture as F ON F.fixture_id = P11.fixture_id " +
            "JOIN tournament AS T ON F.tour_id = T.tour_id " +
            "JOIN player as PL ON PL.id = P11.player_id " +
            "JOIN team ON team.team_id = P11.team_id " +
            "WHERE player_id = ? " +
            "GROUP BY F.tour_id, player_id, team.team_id " +
            "ORDER BY YEAR;";

	
	
	protected final String ALL_PLAYER_TEAM_STATS = "SELECT TP.player_id, P.name Player_name, TP.team_id, T.name Team_name, (SELECT DISTINCT COUNT(TTC.tour_id) FROM tournament_team TTC INNER JOIN team_player TPC ON TPC.team_id = TP.team_id WHERE TTC.team_id = TP.team_id AND TPC.player_id = TP.player_id) TOURNAMENTS, (SELECT COUNT(*) FROM playing_11 PE WHERE PE.player_id = TP.player_id) Matches, SUM(P11.runs) AS Total_Runs, SUM(balls_faced) BF, SUM(fours) 4s, SUM(sixes) 6s, SUM(fifties) 50s, SUM(hundreds) 100s, SUM(wickets_taken) WKTS, SUM(balls_bowled) BB FROM team_player TP LEFT JOIN playing_11 P11 ON P11.player_id = TP.player_id INNER JOIN player P ON TP.player_id = P.id INNER JOIN team T ON TP.team_id = T.team_id GROUP BY TP.player_id, team_id;";

	
	public List<PlayersStatsVO> allPlayerStats() throws SQLException{
		List<PlayersStatsVO> result = new ArrayList<>();
		
		if(SQLConfig.connection == null)
			throw new SQLException("Connection Failed");
		
		PreparedStatement pstmt = SQLConfig.connection.prepareStatement(ALL_PLAYER_STATS);
		
		ResultSet rs = pstmt.executeQuery();
		
		while(rs.next()) {
			PlayerStatsBuilder playerStatsBuilder = new PlayerStatsBuilder(rs).addPlayerStats().addMatchCount().addTournamentCount();
			PlayersStatsVO playerStatsVO = playerStatsBuilder.build();
			result.add(playerStatsVO);
		}
		
		return result;
	}
	
	
	public List<PlayersStatsVO> allTeamPlayerStats() throws SQLException{
		List<PlayersStatsVO> players = new ArrayList<>();
		if(SQLConfig.connection == null)
			throw new SQLException("Connection Failed");
		
		PreparedStatement pstmt = SQLConfig.connection.prepareStatement(ALL_PLAYER_TEAM_STATS);
		
		ResultSet rs = pstmt.executeQuery();
		
		while(rs.next()) {
			PlayerStatsBuilder playerStatsBuilder = new PlayerStatsBuilder(rs).addPlayerStats().addMatchCount().addTournamentCount().addTeamStats();
			PlayersStatsVO playerStatsVO = playerStatsBuilder.build();
			players.add(playerStatsVO);
		}
		
		return players;
	}
	
	public List<PlayersStatsVO> topPlayersInBatting() throws SQLException{
		List<PlayersStatsVO> playersStatsVOs = new ArrayList<>();
		if(SQLConfig.connection == null)
			throw new SQLException("Connection Failed");
		
		PreparedStatement pstmt = SQLConfig.connection.prepareStatement(TOP_PLAYER_BATTING_QUERY);
		ResultSet rs = pstmt.executeQuery();
		
		while(rs.next()) {
			PlayersStatsVO playersStatsVO = new TopPlayerBuilder(rs).addPlayerdata().addTourdata().addBattingStats().build();
			playersStatsVOs.add(playersStatsVO);
		}
		
		return playersStatsVOs;
	}
	
	public PlayersStatsVO topPlayerInBattingByTourId(Integer tourId) throws SQLException{
		PlayersStatsVO playersStatsVO = null;
		if(SQLConfig.connection == null)
			throw new SQLException("Connection Failed");
		
		PreparedStatement pstmt = SQLConfig.connection.prepareStatement(TOP_PLAYER_BATTER_BY_TOUR_ID);
		pstmt.setInt(1, tourId);
		ResultSet rs = pstmt.executeQuery();
		
		if(rs.next())
			playersStatsVO = new TopPlayerBuilder(rs).addPlayerdata().addTourdata().addBattingStats().build();
		
		return playersStatsVO;
	}
	
	public PlayersStatsVO topPlayerInBattingByYear(Integer year) throws SQLException{
		PlayersStatsVO playersStatsVO = null;
		if(SQLConfig.connection == null)
			throw new SQLException("Connection Failed");
		
		PreparedStatement pstmt = SQLConfig.connection.prepareStatement(TOP_PLAYER_BATTER_BY_YEAR);
		pstmt.setInt(1, year);
		ResultSet rs = pstmt.executeQuery();
		
		if(rs.next())
			playersStatsVO = new TopPlayerBuilder(rs).addPlayerdata().addYearData().addBattingStats().build();
		
		return playersStatsVO;
	}
	
	
	public List<PlayersStatsVO> topPlayersInBowling() throws SQLException{
		List<PlayersStatsVO> playersStatsVOs = new ArrayList<>();
		if(SQLConfig.connection == null)
			throw new SQLException("Connection Failed");
		
		PreparedStatement pstmt = SQLConfig.connection.prepareStatement(TOP_PLAYER_BOWLING_QUERY);
		ResultSet rs = pstmt.executeQuery();
		
		while(rs.next()) {
			PlayersStatsVO playersStatsVO = new TopPlayerBuilder(rs).addPlayerdata().addTourdata().addBowlingStats().build();
			playersStatsVOs.add(playersStatsVO);
		}
		
		return playersStatsVOs;
	}
	
	public PlayersStatsVO topPlayerInBowlingByTourId(Integer tourId) throws SQLException{
		PlayersStatsVO playersStatsVO = null;
		if(SQLConfig.connection == null)
			throw new SQLException("Connection Failed");
		
		PreparedStatement pstmt = SQLConfig.connection.prepareStatement(TOP_PLAYER_BOWLER_BY_TOUR_ID);
		pstmt.setInt(1, tourId);
		ResultSet rs = pstmt.executeQuery();
		
		if(rs.next())
			playersStatsVO = new TopPlayerBuilder(rs).addPlayerdata().addTourdata().addBowlingStats().build();
		
		return playersStatsVO;
	}
	
	public PlayersStatsVO topPlayerInBowlingByYear(Integer year) throws SQLException{
		PlayersStatsVO playersStatsVO = null;
		if(SQLConfig.connection == null)
			throw new SQLException("Connection Failed");
		
		PreparedStatement pstmt = SQLConfig.connection.prepareStatement(TOP_PLAYER_BOWLER_BY_YEAR);
		pstmt.setInt(1, year);
		ResultSet rs = pstmt.executeQuery();
		
		if(rs.next())
			playersStatsVO = new TopPlayerBuilder(rs).addPlayerdata().addYearData().addBowlingStats().build();
		
		return playersStatsVO;
	}
	
	public List<PlayersStatsVO> playerHistoryById(Integer playerId) throws SQLException{
		List<PlayersStatsVO> playersStatsVOs = new ArrayList<>();
		
		if(SQLConfig.connection == null)
			throw new SQLException("Connection Failed");
		
		PreparedStatement pstmt = SQLConfig.connection.prepareStatement(PLAYER_MATCH_HISTORY);
		pstmt.setInt(1,playerId);
		ResultSet rs = pstmt.executeQuery();
		
		while(rs.next()) {
			PlayersStatsVO playersStatsVO = new PlayerStatsBuilder(rs).addTournamentDetails().addPlayerStats().addTeamStats().addMatchCount()
					.addStrikeRate().build();
			playersStatsVOs.add(playersStatsVO);
		}
		
		return playersStatsVOs;
	}
	
}
