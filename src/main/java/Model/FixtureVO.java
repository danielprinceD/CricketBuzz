package model;

public class FixtureVO {
	
	private Integer fixtureId;
    private Integer tourId;
    private Integer team1Id;
    private Integer team2Id;
    private Integer winnerId;
    private Integer venueId;
    private String matchDate;
    private String round;
    private String status;
    private VenueVO venue;
    private String winnerTeamName;
    private Integer tossWinnerId;
    private String tossWinnerTeamName;
    private String tossWinnerDecision;
    private Integer manOfTheMatch;
    private String manOfTheMatchPlayerName;
    private String team1Name;
    private String team2Name;
    
    public void setTeam1Name(String team1Name) {
    	this.team1Name = team1Name;
    }
    public String getTeam1Name() {
    	return team1Name;
    }
    
    public void setTeam2Name(String team2Name) {
    	this.team2Name = team2Name;
    }
    public String getTeam2Name() {
    	return team2Name;
    }
    
    public String getManOfTheMatchPlayerName() {
    	return manOfTheMatchPlayerName;
    }
    
    public void setManOfTheMatchPlayerName(String manOfTheMatchPlayerName) {
    	this.manOfTheMatchPlayerName = manOfTheMatchPlayerName;
    }
    
    
    public Integer getManOfTheMatch() {
    	return manOfTheMatch;
    }
    
    public void setManOfTheMatch(Integer manOfTheMatch) {
    	this.manOfTheMatch = manOfTheMatch;
    }
     
    
    public String getTossWinnerDecision() {
    	return tossWinnerDecision;
    }
    
    public void setTossWinnerDecision(String tossWinnerDecision) {
    	this.tossWinnerDecision = tossWinnerDecision;
    }
    
    
    public String getTossWinnerTeamName() {
    	return tossWinnerTeamName;
    }
    
    public void setTossWinnerTeamName(String tossWinnerTeamName) {
    	this.tossWinnerTeamName = tossWinnerTeamName;
    }
    
    public String getWinnerTeamName() {
    	return winnerTeamName;
    }
    
    public void setWinnerTeamName(String winnerTeamName) {
    	this.winnerTeamName = winnerTeamName;
    }
    
    public Integer getTossWinnerId() {
    	return tossWinnerId;
    }
    
    public void setTossWinnerId(Integer tossWinnerId) {
    	this.tossWinnerId = tossWinnerId;
    }
    
    
    public VenueVO getVenue() {
    	return this.venue;
    }
    
    public void setVenue(VenueVO venue) {
    	this.venue = venue;
    }
    
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    
    
    public String getRound() {
        return round;
    }

    public void setRound(String round) {
        this.round = round;
    }

    public int getFixtureId() {
        return fixtureId;
    }

    public void setFixtureId(int fixtureId) {
        this.fixtureId = fixtureId;
    }

    public int getTourId() {
        return tourId;
    }

    public void setTourId(int tourId) {
        this.tourId = tourId;
    }

    public int getTeam1Id() {
        return team1Id;
    }

    public void setTeam1Id(int team1Id) {
        this.team1Id = team1Id;
    }

    public int getTeam2Id() {
        return team2Id;
    }

    public void setTeam2Id(int team2Id) {
        this.team2Id = team2Id;
    }

    public int getWinnerId() {
        return winnerId;
    }

    public void setWinnerId(int winnerId) {
        this.winnerId = winnerId;
    }

    public int getVenueId() {
        return venueId;
    }

    public void setVenueId(int venueId) {
        this.venueId = venueId;
    }

    public String getMatchDate() {
        return matchDate;
    }

    public void setMatchDate(String matchDate) {
        this.matchDate = matchDate;
    }
    
    public boolean isValid() {
        if (team1Id <= 0 || team2Id <= 0 || venueId <= 0 || matchDate == null) {
            return false; 
        }
        
        if (matchDate == null || matchDate.isEmpty()) {
            return false; 
        }
        return true;
    }
}
