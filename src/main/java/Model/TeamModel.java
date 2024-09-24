package Model;

import java.util.List;

public class TeamModel {
	private Integer team_id = -1;
    private Integer captain_id = -1;
    private Integer vice_captain_id = -1;
    private Integer wicket_keeper_id = -1;
    private String category;
    public Integer team_players[];
    private String name;

    public int getTeamId() {
        return team_id;
    }

    public void setTeamId(int teamId) {
        this.team_id = teamId;
    }

    public Integer getCaptainId() {
        return  captain_id;
    }

    public void setCaptainId(int captainId) {
        this.captain_id = captainId;
    }

    public Integer getViceCaptainId() {
        return vice_captain_id;
    }

    public void setViceCaptainId(int viceCaptainId) {
        this.vice_captain_id = viceCaptainId;
    }

    public Integer getWicketKeeperId() {
        return wicket_keeper_id;
    }

    public void setWicketKeeperId(int wicketKeeperId) {
        this.wicket_keeper_id = wicketKeeperId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    
    public boolean isValid() {
        if (name == null || category == null || captain_id < 0 || wicket_keeper_id < 0 || wicket_keeper_id < 0) {
            return false;
        }
        return true;
    }

    
}
