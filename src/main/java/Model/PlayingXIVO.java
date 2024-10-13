package model;

import org.json.JSONObject;

import com.google.gson.Gson;

public class PlayingXIVO {
    
    private Integer fixtureId ;
    private Integer ballsBowled;
    private Integer playerId;
    private String role;
    private Integer runs;
    private Integer ballsFaced;
    private Integer fours;
    private Integer sixes;
    private Integer fifties;
    private Integer hundreds;
    private Integer wicketsTaken;
    private Integer teamId;
    private String playerName;
    
    public void setName(String name)
    {
    	this.playerName = name;
    }
    
    public String getName() {
    	return this.playerName;
    }

    public int getFixtureId() {
        return fixtureId;
    }

    public void setFixtureId(Integer fixtureId) {
        this.fixtureId = fixtureId;
    }

    public Integer getPlayerId() {
        return playerId;
    }

    public void setPlayerId(Integer playerId) {
        this.playerId = playerId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Integer getRuns() {
        return runs;
    }

    public void setRuns(Integer runs) {
        this.runs = runs;
    }

    public int getBallsFaced() {
        return ballsFaced;
    }

    public void setBallsFaced(Integer ballsFaced) {
        this.ballsFaced = ballsFaced;
    }
    public int getBallsBowled() {
        return ballsBowled;
    }

    public void setBallsBowled(Integer ballsFaced) {
        this.ballsBowled = ballsFaced;
    }

    public Integer getFours() {
        return fours;
    }

    public void setFours(Integer fours) {
        this.fours = fours;
    }

    public Integer getSixes() {
        return sixes;
    }

    public void setSixes(Integer sixes) {
        this.sixes = sixes;
    }

    public Integer getFifties() {
        return fifties;
    }

    public void setFifties(Integer fifties) {
        this.fifties = fifties;
    }

    public int getHundreds() {
        return hundreds;
    }

    public void setHundreds(Integer hundreds) {
        this.hundreds = hundreds;
    }

    public Integer getWicketsTaken() {
        return wicketsTaken;
    }

    public void setWicketsTaken(Integer wicketsTaken) {
        this.wicketsTaken = wicketsTaken;
    }

    public Integer getTeamId() {
        return teamId;
    }

    public void setTeamId(Integer teamId) {
        this.teamId = teamId;
    }

    public static PlayingXIVO fromJson(String jsonString) {
		return new Gson().fromJson(jsonString, PlayingXIVO.class);
	}
    
    public String toJson() {
		return new JSONObject(this).toString();
	}
    
}

