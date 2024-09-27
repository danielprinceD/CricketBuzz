package model;

public class FixtureVO {
	
	private int fixtureId = -1;
    private int tourId = -1;
    private int team1Id = -1;
    private int team2Id = -1;
    private Integer winnerId = -1;
    private int venueId = -1;
    private String matchDate;
    private String round;
    private String status;

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
