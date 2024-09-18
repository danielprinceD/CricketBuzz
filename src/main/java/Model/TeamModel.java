package Model;

public class TeamModel {
	private int teamId = -1;
    private int captainId = -1;
    private int viceCaptainId = -1;
    private int wicketKeeperId = -1;
    private String category;
    private String name;

    public int getTeamId() {
        return teamId;
    }

    public void setTeamId(int teamId) {
        this.teamId = teamId;
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
