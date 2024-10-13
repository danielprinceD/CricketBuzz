package model;

public class PlayersStatsVO extends PlayingXIVO{
	private String teamName;
	private Integer tournamentCount;
	private Integer matchCount;
	private Integer year;
	private Integer tourId;
	private String tourName;
	private Integer matchesPlayed;
	private Double battingStrikeRate;
	private Double bowlingStrikeRate;
	private Integer playedAsBatter;
	private Integer playedAsBowler;
	private Double avgRun;
	
	public void setAvgRun(Double avgRun) {
		this.avgRun = avgRun;
	}
	
	public void setPlayedAsBatter(Integer count) {
		this.playedAsBatter = count;
	}
	public void setPlayedAsBowler(Integer count) {
		this.playedAsBatter = count;
	}
	
	public void setMatchPlayed(Integer matchesPlayed) {
		this.matchesPlayed = matchesPlayed;
	}
	public void setBattingSR(Double SR) {
		this.battingStrikeRate = SR;
	}
	public void setBowlingSR(Double SR) {
		this.bowlingStrikeRate = SR;
	}
	
	public void setYear(Integer year) {
		this.year = year;
	}
	public void setTourId(Integer tourId) {
		this.tourId = tourId;
	}
	public void setTourName(String tourName) {
		this.tourName = tourName;
	}
	
	public void setTeamName(String teamName) {
		this.teamName = teamName;
	}
	
	public void setTournamentCount(Integer tournamentCount) {
		this.tournamentCount = tournamentCount;
	}
	
	public void setMatchCount(Integer matchCount) {
		this.matchCount = matchCount;
	}
}
