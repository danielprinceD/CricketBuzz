package Model;

public class VenueModel {
    private int venueId = -1;
    private String stadium;
    private String location;
    private String pitchCondition;
    private String description;
    private long capacity;
    private String curator;

    public int getVenueId() {
        return venueId;
    }

    public void setVenueId(int venueId) {
        this.venueId = venueId;
    }

    public String getStadium() {
        return stadium;
    }

    public void setStadium(String stadium) {
        this.stadium = stadium;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getPitchCondition() {
        return pitchCondition;
    }

    public void setPitchCondition(String pitchCondition) {
        this.pitchCondition = pitchCondition;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getCapacity() {
        return capacity;
    }

    public void setCapacity(long capacity) {
        this.capacity = capacity;
    }

    public String getCurator() {
        return curator;
    }

    public void setCurator(String curator) {
        this.curator = curator;
    }

    public boolean isValid() {
        if (stadium == null || stadium.isEmpty() || location == null || pitchCondition == null || description == null || location.isEmpty() || description.isEmpty() || description.isEmpty() || curator == null || curator.isEmpty() ) {
            return false; 
        }
        
        if (capacity <= 0) {
            return false; 
        }
        
        return true;
    }

}

