package model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONObject;

import com.google.gson.Gson;

public class TournamentVO {
    private Integer tourId;
    private String name;
    private String startDate;
    private String endDate;
    private String matchCategory;
    private Integer season;
    private String status;
    private List<TournamentTeamVO> participatedTeams;
    private Integer teamCount;
    
    
    public void setTeamCount(Integer count) {
    	this.teamCount = count;
    }
    public Integer getTeamCount() {
    	return this.teamCount;
    }

	public boolean isValid() {
	    	
	        if (name == null || name.isEmpty()) {
	            return false;
	        }
	        
	        if (startDate == null || endDate == null) {
	            return false;
	        }
	        
	        if (matchCategory == null || matchCategory.isEmpty()) {
	            return false;
	        }
	        
	        if (season < 0) {
	            return false;
	        }
	        
	        
	        return true;
	    }
    
    public Integer getTourId() {
        return tourId;
    }

    public void setTourId(Integer tourId) {
        this.tourId = tourId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getMatchCategory() {
        return matchCategory;
    }

    public void setMatchCategory(String matchCategory) {
        this.matchCategory = matchCategory;
    }

    public Integer getSeason() {
        return season;
    }

    public void setSeason(Integer season) {
        this.season = season;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<TournamentTeamVO> getParticipatedTeams() {
        return participatedTeams;
    }

    public void setParticipatedTeams(List<TournamentTeamVO> participatedTeams) {
        this.participatedTeams = participatedTeams;
    }
    
    public static TournamentVO fromJson(String jsonString) {
		return new Gson().fromJson(jsonString, TournamentVO.class);
	}
    
    public String toJson() {
		return new JSONObject(this).toString();
	}
    
    public Boolean canPost() {
    	if(name == null || startDate == null || endDate == null || matchCategory == null || season == null || status == null || participatedTeams == null)
    		return false;
    	return true;
    }
    
}
