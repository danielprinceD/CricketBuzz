package Model;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class TournamentModel {
    private int tourId = -1;
    private String name;
    private String startDate; 
    private String endDate;  
    private String matchCategory;
    private int season;


    public int getTourId() {
        return tourId;
    }
    public String getName() {
        return name;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }


    public String getMatchCategory() {
        return matchCategory;
    }


    public int getSeason() {
        return season;
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
    
}
