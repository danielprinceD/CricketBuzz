package Model;

import java.util.List;

public class TeamVO {
    private int teamId = -1;
    private String name;
    private int captainId = -1;
    private int viceCaptainId = -1;
    private int wicketKeeperId = -1;
    private String category;
    private List<Integer> teamPlayers;

    public int getTeamId() {
        return teamId;
    }

    public void setTeamId(int teamId) {
        this.teamId = teamId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCaptainId() {
        return captainId;
    }

    public void setCaptainId(int captainId) {
        this.captainId = captainId;
    }

    public int getViceCaptainId() {
        return viceCaptainId;
    }

    public void setViceCaptainId(int viceCaptainId) {
        this.viceCaptainId = viceCaptainId;
    }

    public int getWicketKeeperId() {
        return wicketKeeperId;
    }

    public void setWicketKeeperId(int wicketKeeperId) {
        this.wicketKeeperId = wicketKeeperId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public List<Integer> getTeamPlayers() {
        return teamPlayers;
    }

    public void setTeamPlayers(List<Integer> teamPlayers) {
        this.teamPlayers = teamPlayers;
    }
}
