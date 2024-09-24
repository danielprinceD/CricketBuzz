package Model;

import java.util.List;

import org.json.JSONObject;



public class PlayerModel {
	
	
	
    private int id = -1;
    private String name;
    private String role;
    private AddressModel address;
    private String gender;
    private double rating;
    private String battingStyle;
    private String bowlingStyle;

    public AddressModel getAddress(){
		return address;
	}
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }    
    
    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

 
    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public String getBattingStyle() {
        return battingStyle;
    }

    public void setBattingStyle(String battingStyle) {
        this.battingStyle = battingStyle;
    }

    public String getBowlingStyle() {
        return bowlingStyle;
    }

    public void setBowlingStyle(String bowlingStyle) {
        this.bowlingStyle = bowlingStyle;
    }
    
    public boolean isValid() {
    	if(name == null || role == null || address == null || gender == null || battingStyle == null || bowlingStyle == null)
    			return false;
    	return true;
    }
}