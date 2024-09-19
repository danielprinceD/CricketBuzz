package Model;

import java.util.List;

public class TeamModel {
	private Integer teamId = -1;
    private Integer captainId = -1;
    private Integer viceCaptainId = -1;
    private Integer wicketKeeperId = -1;
    private String category;
    private String name;

    public int getTeamId() {
        return teamId;
    }

    public void setTeamId(int teamId) {
        this.teamId = teamId;
    }

    public Integer getCaptainId() {
        return captainId;
    }

    public void setCaptainId(int captainId) {
        this.captainId = captainId;
    }

    public Integer getViceCaptainId() {
        return viceCaptainId;
    }

    public void setViceCaptainId(int viceCaptainId) {
        this.viceCaptainId = viceCaptainId;
    }

    public Integer getWicketKeeperId() {
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    
    public boolean isValid() {
        if (name == null || category == null || captainId < 0 || viceCaptainId < 0 || wicketKeeperId < 0) {
            return false;
        }
        return true;
    }

    
}
