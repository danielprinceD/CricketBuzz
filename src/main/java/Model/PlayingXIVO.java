package Model;

public class PlayingXIVO {
    
    private int fixture_id = -1;
    private int player_id = -1;
    private String role;
    private int runs;
    private int balls_faced;
    private int fours;
    private int sixes;
    private int fifties;
    private int hundreds;
    private int wickets_taken;
    private Integer team_id = -1; 
    

    public int getFixtureId() {
        return fixture_id;
    }

    public void setFixtureId(int fixtureId) {
        this.fixture_id = fixtureId;
    }

    public int getPlayerId() {
        return player_id;
    }

    public void setPlayerId(int playerId) {
        this.player_id = playerId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public int getRuns() {
        return runs;
    }

    public void setRuns(int runs) {
        this.runs = runs;
    }

    public int getBallsFaced() {
        return balls_faced;
    }

    public void setBallsFaced(int ballsFaced) {
        this.balls_faced = ballsFaced;
    }

    public int getFours() {
        return fours;
    }

    public void setFours(int fours) {
        this.fours = fours;
    }

    public int getSixes() {
        return sixes;
    }

    public void setSixes(int sixes) {
        this.sixes = sixes;
    }

    public int getFifties() {
        return fifties;
    }

    public void setFifties(int fifties) {
        this.fifties = fifties;
    }

    public int getHundreds() {
        return hundreds;
    }

    public void setHundreds(int hundreds) {
        this.hundreds = hundreds;
    }

    public int getWicketsTaken() {
        return wickets_taken;
    }

    public void setWicketsTaken(int wicketsTaken) {
        this.wickets_taken = wicketsTaken;
    }

    public Integer getTeamId() {
        return team_id;
    }

    public void setTeamId(Integer teamId) {
        this.team_id = teamId;
    }

    
}

