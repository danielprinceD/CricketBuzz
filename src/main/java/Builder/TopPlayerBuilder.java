package Builder;

import java.sql.ResultSet;
import java.sql.SQLException;

import model.PlayersStatsVO;

public class TopPlayerBuilder {
	
	protected PlayersStatsVO playersStatsVO = new PlayersStatsVO();
	ResultSet rs;
	
	public TopPlayerBuilder(ResultSet rs) {
		this.rs = rs;
	}
	public TopPlayerBuilder addPlayerdata() throws SQLException {
		playersStatsVO.setPlayerId(rs.getInt("player_id"));
		playersStatsVO.setName(rs.getString("player_name"));
		return this;
	}
	public TopPlayerBuilder addYearData() throws SQLException{
		playersStatsVO.setYear(rs.getInt("year"));
		return this;
	}
	public TopPlayerBuilder addTourdata() throws SQLException {
		playersStatsVO.setTourId(rs.getInt("tour_id"));
		playersStatsVO.setTourName(rs.getString("tour_name"));
		return this;
	}
	
	public TopPlayerBuilder addBattingStats() throws SQLException {
		playersStatsVO.setRuns(rs.getInt("total_runs"));
		playersStatsVO.setBallsFaced(rs.getInt("BF"));
		return this;
	}
	
	public TopPlayerBuilder addBowlingStats() throws SQLException {
		playersStatsVO.setWicketsTaken(rs.getInt("total_wkts"));
		playersStatsVO.setBallsBowled(rs.getInt("BB"));
		return this;
	}
	
	public PlayersStatsVO build() {
		return playersStatsVO;
	}
}
