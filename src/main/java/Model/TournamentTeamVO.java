package model;

public class TournamentTeamVO {
    private Integer teamId;
    private Integer points;
    private double netRunRate;
    private String name;
    
    public String getName() {
    	return this.name;
    }
    
    public void setName(String name) {
    	this.name = name;
    }

    public int getTeamId() {
        return teamId;
    }

    public void setTeamId(int teamId) {
        this.teamId = teamId;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public double getNetRunRate() {
        return netRunRate;
    }

    public void setNetRunRate(double netRunRate) {
        this.netRunRate = netRunRate;
    }
}

