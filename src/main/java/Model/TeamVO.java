package model;

import java.util.List;
import java.util.Set;

public class TeamVO {
	
    private Integer teamId;
    private String name;
    private Integer captainId;
    private Integer viceCaptainId;
    private Integer wicketKeeperId;
    private String category;
    private String captainName;
    private String viceCaptainName;
    private List<PlayerVO> players;
    private List<PlayingXIVO> playing11s;
    private Set<Integer> playersList;
    
    
   public Set<Integer> getPlayersList(){
	   return playersList;
   }
   
   public void setPlayersList(Set<Integer> playersList) {
	   this.playersList = playersList;
   }
    public List<PlayingXIVO> getPlaying11s(){
    	return this.playing11s;
    }
    
    public void setPlaying11s(List<PlayingXIVO> playing11s) {
		this.playing11s = playing11s;
	}
    
    public List<PlayerVO> getPlayers() {
        return players;
    }
    
    public void setPlayers(List<PlayerVO> players) {
        this.players = players;
    }
    
    public String getCaptainName() {
        return captainName;
    }

    public void setCaptainName(String captainName) {
        this.captainName = captainName;
    }

    public String getViceCaptainName() {
        return viceCaptainName;
    }

    public void setViceCaptainName(String viceCaptainName) {
        this.viceCaptainName = viceCaptainName;
    }

    public Integer getTeamId() {
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
    
    public boolean canPost() {
    	if(name == null || category == null || captainId == null || viceCaptainId == null || wicketKeeperId == null)
    		return false;
    	
    	return true;
    }
    
    
    
}
