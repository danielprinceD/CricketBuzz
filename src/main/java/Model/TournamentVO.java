package model;

import java.util.List;

import org.json.JSONObject;

import com.google.gson.Gson;

public class TournamentVO {
    private int tourId;
    private String name;
    private String startDate;
    private String endDate;
    private String matchCategory;
    private Integer season;
    private String status = "INPROGRESS" ;
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
    
    public int getTourId() {
        return tourId;
    }

    public void setTourId(int tourId) {
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

    public int getSeason() {
        return season;
    }

    public void setSeason(int season) {
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
    
}
