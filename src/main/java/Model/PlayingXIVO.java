package model;

import org.json.JSONObject;

import com.google.gson.Gson;

public class PlayingXIVO {
    
    private Integer fixture_id ;
    private Integer balls_bowled;
    private Integer player_id;
    private String role;
    private Integer runs;
    private Integer balls_faced;
    private Integer fours;
    private Integer sixes;
    private Integer fifties;
    private Integer hundreds;
    private Integer wickets_taken;
    private Integer team_id;
    private String playerName;
    
    public void setName(String name)
    {
    	this.playerName = name;
    }
    
    public String getName() {
    	return this.playerName;
    }

    public int getFixtureId() {
        return fixture_id;
    }

    public void setFixtureId(Integer fixtureId) {
        this.fixture_id = fixtureId;
    }

    public Integer getPlayerId() {
        return player_id;
    }

    public void setPlayerId(Integer playerId) {
        this.player_id = playerId;
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
        return balls_faced;
    }

    public void setBallsFaced(Integer ballsFaced) {
        this.balls_faced = ballsFaced;
    }
    public int getBallsBowled() {
        return balls_bowled;
    }

    public void setBallsBowled(Integer ballsFaced) {
        this.balls_bowled = ballsFaced;
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
        return wickets_taken;
    }

    public void setWicketsTaken(Integer wicketsTaken) {
        this.wickets_taken = wicketsTaken;
    }

    public Integer getTeamId() {
        return team_id;
    }

    public void setTeamId(Integer teamId) {
        this.team_id = teamId;
    }

    public static PlayingXIVO fromJson(String jsonString) {
		return new Gson().fromJson(jsonString, PlayingXIVO.class);
	}
    
    public String toJson() {
		return new JSONObject(this).toString();
	}
    
}

