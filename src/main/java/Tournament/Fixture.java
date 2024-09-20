package Tournament;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.io.BufferedReader;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import Model.FixtureModel;
import Team.Extra;

@WebServlet("/fixtures/*")
public class Fixture extends HttpServlet {
	private static final long serialVersionUID = 1L;
    private static final String DB_URL = "jdbc:mysql://localhost:3306/CricketBuzz";
    private static final String USER = "root";
    private static final String PASS = "";
    
    public static void addData(PrintWriter out , ResultSet rs , JSONObject fixtureJson)
    {
    	try {
    		
    		fixtureJson.put("fixture_id", rs.getInt("fixture_id"));
            fixtureJson.put("tour_id", rs.getInt("tour_id"));
            fixtureJson.put("tour_name", rs.getString("tour_name"));
            
            
            
            fixtureJson.put("winner_team", rs.getString("winner_team_name") + " "+ rs.getString("result") );
            
            fixtureJson.put("venue_id", rs.getInt("venue_id"));
            fixtureJson.put("venue_name", rs.getString("venue_name"));
            fixtureJson.put("venue_location", rs.getString("venue_location"));
            
            fixtureJson.put("match_date", rs.getDate("match_date").toString());
    		
    		
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    
    private void getPlaying11sByFixtureAndTeamID(JSONArray teamArrayObject, Integer fixtureId, Integer teamId, Integer[] teamDetails) {
      
        String sql = "SELECT player_id, role, runs, wickets_taken, balls_faced FROM playing_11 WHERE fixture_id = ? AND team_id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, fixtureId);
            pstmt.setInt(2, teamId);
            
            ResultSet rs = pstmt.executeQuery();

            teamDetails[0] = 0; 
            teamDetails[1] = 0; 
            teamDetails[2] = 0; 

            while (rs.next()) {
                JSONObject playerJson = new JSONObject();
                
                playerJson.put("player_id", rs.getInt("player_id"));
                playerJson.put("role", rs.getString("role"));
                playerJson.put("runs", rs.getInt("runs"));
                playerJson.put("wickets_taken", rs.getInt("wickets_taken"));
                playerJson.put("balls_faced", rs.getInt("balls_faced"));

                teamArrayObject.put(playerJson);

                teamDetails[0] += rs.getInt("runs");
                teamDetails[1] += rs.getInt("wickets_taken");
                teamDetails[2] += rs.getInt("balls_faced");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    
    
    private void getOneFixture(HttpServletResponse response, PrintWriter out, String[] pathArray) throws IOException {
        String fixtureId = pathArray[1];

        if (fixtureId == null) {
            Extra.sendError(response, out, "Fixture ID is required");
            return;
        }

        String query = "SELECT IFNULL(md.result , '' ) AS result, f.fixture_id, f.tour_id, tour.name AS tour_name, "
                + "t1.team_id AS team1_id, t1.name AS team1_name, p1.name AS team1_captain, "
                + "t2.team_id AS team2_id, t2.name AS team2_name, p2.name AS team2_captain, "
                + "IFNULL(v.venue_id,'NOT SET') AS venue_id, IFNULL(v.stadium , 'NOT SET') AS venue_name, IFNULL(v.location, 'NOT_SET') AS venue_location, "
                + "IFNULL(winner_team.name, 'No winner') AS winner_team_name, f.match_date "
                + "FROM fixture f "
                + "LEFT JOIN match_details md ON f.fixture_id = md.fixture_id "
                + "JOIN tournament tour ON f.tour_id = tour.tour_id "
                + "JOIN team t1 ON f.team1_id = t1.team_id "
                + "JOIN player p1 ON t1.captain_id = p1.id "
                + "JOIN team t2 ON f.team2_id = t2.team_id "
                + "JOIN player p2 ON t2.captain_id = p2.id "
                + "JOIN venue v ON f.venue_id = v.venue_id "
                + "LEFT JOIN team winner_team ON f.winner_id = winner_team.team_id WHERE f.fixture_id  = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, Integer.parseInt(fixtureId));
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                JSONObject jsonObject = new JSONObject();
                addData(out, rs, jsonObject);
                
                JSONObject team1 = new JSONObject();
                JSONObject team2 = new JSONObject();
                
                
                team1.put("id", rs.getInt("team1_id"));
                team1.put("name", rs.getString("team1_name"));
                team1.put("captain", rs.getString("team1_captain"));
                
                team2.put("id", rs.getInt("team2_id"));
                team2.put("name", rs.getString("team2_name"));
                team2.put("captain", rs.getString("team2_captain"));
                
                
                
                Integer[] team1Details = new Integer[3]; 
                Integer[] team2Details = new Integer[3]; 

                JSONArray team1PlayersArray = new JSONArray();
                JSONArray team2PlayersArray = new JSONArray();

                getPlaying11sByFixtureAndTeamID(team1PlayersArray, Integer.parseInt(fixtureId), rs.getInt("team1_id"), team1Details);
                
                
                team1.put("players", team1PlayersArray);
                team1.put("score", team1Details[0]);
                team1.put("wickets", team1Details[1]);
                team1.put("balls_faced", team1Details[2]);

                getPlaying11sByFixtureAndTeamID(team2PlayersArray, Integer.parseInt(fixtureId), rs.getInt("team2_id"), team2Details);
                
                team2.put("players", team2PlayersArray);
                team2.put("players", team2PlayersArray);
                team2.put("score", team2Details[0]);
                team2.put("wickets", team2Details[1]);
                team2.put("balls_faced", team2Details[2]);

                jsonObject.put("team1", team1);
                jsonObject.put("team2", team2);
                
                out.print(jsonObject.toString());
                return;
            } else {
                Extra.sendError(response, out, "No Fixture ID found");
            }
        } catch (NumberFormatException e) {
            Extra.sendError(response, out, "Invalid Fixture ID");
        } catch (SQLException e) {
            Extra.sendError(response, out, "Error Fetching Data");
            e.printStackTrace();
        }
    }

    private void getAllFixture(HttpServletResponse response , PrintWriter out ) throws IOException {
    	
    	String sql = "SELECT IFNULL(md.result , '' ) AS result, f.fixture_id, f.tour_id, tour.name AS tour_name, "
	            + "t1.team_id AS team1_id, t1.name AS team1_name, p1.name AS team1_captain, "
	            + "t2.team_id AS team2_id, t2.name AS team2_name, p2.name AS team2_captain, "
	            + "IFNULL(v.venue_id,'NOT SET') AS venue_id, IFNULL(v.stadium , 'NOT SET') AS venue_name, IFNULL(v.location, 'NOT_SET') AS venue_location, "
	            + "IFNULL(winner_team.name, 'No winner') AS winner_team_name, f.match_date "
	            + "FROM fixture f "
	            + "LEFT JOIN match_details md ON f.fixture_id = md.fixture_id "
	            + "JOIN tournament tour ON f.tour_id = tour.tour_id "
	            + "JOIN team t1 ON f.team1_id = t1.team_id "
	            + "JOIN player p1 ON t1.captain_id = p1.id "
	            + "JOIN team t2 ON f.team2_id = t2.team_id "
	            + "JOIN player p2 ON t2.captain_id = p2.id "
	            + "JOIN venue v ON f.venue_id = v.venue_id "
	            + "LEFT JOIN team winner_team ON f.winner_id = winner_team.team_id ";
    	
    		
    	JSONArray playersArray = new JSONArray();
		
		try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
		         Statement stmt = conn.createStatement();
		         ResultSet rs = stmt.executeQuery(sql); ) {

		        while (rs.next()) {
		            JSONObject fixtureObject = new JSONObject();
		            addData( out , rs , fixtureObject);
		            playersArray.put(fixtureObject);
		        }

		        if(playersArray.length() == 0)
		        {
		        	out.print("No Data Found");
		        }
		        else {		        	
		        out.print(playersArray.toString());
		        out.flush();
		        }

		    } catch (SQLException e) {
		        e.printStackTrace();
		        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error: " + e.getMessage());
		    }
    	
    	
    	
    }
    
    private void addManyFixture(HttpServletResponse response, PrintWriter out, List<FixtureModel> fixtureModelList, String tourIdString) {
        int totalRowsAffected = 0;

        String sql = "INSERT INTO fixture (team1_id, team2_id, venue_id, match_date, tour_id) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            conn.setAutoCommit(false);  

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                for (FixtureModel fixtureModel : fixtureModelList) {
                    pstmt.setInt(1, fixtureModel.getTeam1Id());
                    pstmt.setInt(2, fixtureModel.getTeam2Id());
                    pstmt.setInt(3, fixtureModel.getVenueId());
                    pstmt.setString(4, fixtureModel.getMatchDate());
                    pstmt.setInt(5, Integer.parseInt(tourIdString));

                    totalRowsAffected += pstmt.executeUpdate();
                }

                conn.commit();

                if (totalRowsAffected > 0) {
                    Extra.sendSuccess(response, out, totalRowsAffected + " fixtures added successfully.");
                } else {
                    Extra.sendError(response, out, "No fixtures were added.");
                }
            } catch (SQLException e) {
                conn.rollback(); 
                Extra.sendError(response, out, "Database error: " + e.getMessage());
            }
        } catch (SQLException e) {
            Extra.sendError(response, out, "Connection error: " + e.getMessage());
        }
    }

    
    private void updateOneFixture(HttpServletResponse response, PrintWriter out, FixtureModel fixtureModel, String fixtureIdString) throws ServletException {
        try {
            Integer fixtureId = Integer.parseInt(fixtureIdString);
            
            String sql = "UPDATE fixture SET team1_id = ?, team2_id = ?, venue_id = ?, winner_id = ?, match_date = ? WHERE fixture_id = ?";
            
            try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setInt(1, fixtureModel.getTeam1Id());
                pstmt.setInt(2, fixtureModel.getTeam2Id());
                pstmt.setInt(3, fixtureModel.getVenueId());
               
                
                if (fixtureModel.getWinnerId() > 0) {
                    pstmt.setInt(4, fixtureModel.getWinnerId());
                } else {
                    pstmt.setNull(4, java.sql.Types.INTEGER); 
                }
                
                pstmt.setString(5, fixtureModel.getMatchDate());
                pstmt.setInt(6, fixtureId); 
                
                int rowsAffected = pstmt.executeUpdate();

                if (rowsAffected > 0) {
                    Extra.sendSuccess(response, out, "Fixture updated successfully");
                } else {
                    Extra.sendError(response, out, "Fixture not found or update failed");
                }
            } catch (SQLException e) {
                Extra.sendError(response, out, "Database error: " + e.getMessage());
            }
        } catch (Exception e) {
            Extra.sendError(response, out, "Invalid fixture_id");
        }
    }

    
    private void deleteOneFixture(HttpServletResponse response , PrintWriter out  , String fix ) {
    	
    	String sql = "DELETE FROM fixture WHERE fixture_id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
        	
            pstmt.setInt(1, Integer.parseInt(fix));
            
            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
            	Extra.sendSuccess(response, out, "Fixture Deleted Successfully");
            } else {
            	Extra.sendError(response, out, "No Data Found");
            }
        } catch (NumberFormatException e) {
        	Extra.sendError(response, out, e.getMessage());
        } catch (SQLException e) {
        	Extra.sendError(response, out, e.getMessage());
            e.printStackTrace();
        }
    }
    private void deleteAllFixture(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
        String sql = "DELETE FROM fixture WHERE tour_id = ?";
        
        String tourId = request.getParameter("tour_id");
        
        if (tourId == null || tourId.isEmpty()) {
            Extra.sendError(response, out, "tour_id is missing or invalid");
            return;
        }
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, Integer.parseInt(tourId));

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                Extra.sendSuccess(response, out, "All Fixtures Deleted Successfully");
            } else {
                Extra.sendError(response, out, "No Data Found for the provided tour_id");
            }

        } catch (NumberFormatException e) {
            Extra.sendError(response, out, "Invalid tour_id format: " + e.getMessage());
        } catch (SQLException e) {
            Extra.sendError(response, out, "Database error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    protected void getFixtureByTour(HttpServletResponse response , PrintWriter out , String tourId) {
    	
    	String sql = "SELECT IFNULL(md.result, '') AS result, f.fixture_id, f.tour_id, tour.name AS tour_name, "
                + "t1.team_id AS team1_id, t1.name AS team1_name, p1.name AS team1_captain, "
                + "t2.team_id AS team2_id, t2.name AS team2_name, p2.name AS team2_captain, "
                + "IFNULL(v.venue_id, 'NOT SET') AS venue_id, IFNULL(v.stadium, 'NOT SET') AS venue_name, IFNULL(v.location, 'NOT_SET') AS venue_location, "
                + "IFNULL(winner_team.name, 'No winner') AS winner_team_name, f.match_date "
                + "FROM fixture f "
                + "LEFT JOIN match_details md ON f.fixture_id = md.fixture_id "
                + "JOIN tournament tour ON f.tour_id = tour.tour_id "
                + "JOIN team t1 ON f.team1_id = t1.team_id "
                + "JOIN player p1 ON t1.captain_id = p1.id "
                + "JOIN team t2 ON f.team2_id = t2.team_id "
                + "JOIN player p2 ON t2.captain_id = p2.id "
                + "JOIN venue v ON f.venue_id = v.venue_id "
                + "LEFT JOIN team winner_team ON f.winner_id = winner_team.team_id "
                + "WHERE f.tour_id = ?";

     JSONArray fixtures = new JSONArray();

     try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
          PreparedStatement pstmt = conn.prepareStatement(sql)) {

         pstmt.setInt(1, Integer.parseInt(tourId));

         try (ResultSet rs = pstmt.executeQuery()) {
             while (rs.next()) {
                 JSONObject fixture = new JSONObject();
                 fixture.put("result", rs.getString("result"));
                 fixture.put("fixture_id", rs.getInt("fixture_id"));
                 fixture.put("tour_id", rs.getInt("tour_id"));
                 fixture.put("tour_name", rs.getString("tour_name"));
                 fixture.put("team1_id", rs.getInt("team1_id"));
                 fixture.put("team1_name", rs.getString("team1_name"));
                 fixture.put("team1_captain", rs.getString("team1_captain"));
                 fixture.put("team2_id", rs.getInt("team2_id"));
                 fixture.put("team2_name", rs.getString("team2_name"));
                 fixture.put("team2_captain", rs.getString("team2_captain"));
                 fixture.put("venue_id", rs.getString("venue_id"));
                 fixture.put("venue_name", rs.getString("venue_name"));
                 fixture.put("venue_location", rs.getString("venue_location"));
                 fixture.put("winner_team_name", rs.getString("winner_team_name"));
                 fixture.put("match_date", rs.getString("match_date"));

                 fixtures.put(fixture);
             }
         }

         if (fixtures.length() > 0) {
             response.setContentType("application/json");
             out.print(fixtures.toString());
         } else {
             Extra.sendError(response, out, "No fixtures found for the provided tour_id");
         }

     } catch (NumberFormatException e) {
         Extra.sendError(response, out, "Invalid tour_id format: " + e.getMessage());
     } catch (SQLException e) {
         Extra.sendError(response, out, "Database error: " + e.getMessage());
         e.printStackTrace();
     }
	}
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
    	
        response.setContentType("application/json");
		String pathInfoString = request.getPathInfo();
		String[] pathArray = pathInfoString != null ?  pathInfoString.split("/") : null;
		PrintWriter out = response.getWriter();
		
		
		
		
		
		if(pathArray == null || pathArray.length <= 1)
		{
			if(request.getParameter("tour_id") == null)
			getAllFixture(response , out );
			else getFixtureByTour(response, out, request.getParameter("tour_id"));
			return;
		}
		else if(pathArray.length == 2)
		{
			
			getOneFixture(response , out , pathArray);
			
			return;
		}
		
		
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {

    	
    	
    	StringBuilder jsonString = new StringBuilder();
        BufferedReader reader = request.getReader();
        String line;
        
        while((line = reader.readLine()) != null)
        		jsonString.append(line);
        
        PrintWriter out = response.getWriter();
        java.lang.reflect.Type fixtureListType = new TypeToken<List<FixtureModel>>() {}.getType();
        List<FixtureModel> fixtureModelList = new Gson().fromJson( jsonString.toString() , fixtureListType );
        
        String pathInfoString = request.getPathInfo();		
        String[] pathArray = pathInfoString != null ?  pathInfoString.split("/") : null;
        
        if ((pathArray == null || pathArray.length <= 1) && request.getParameter("tour_id") != null) {
             addManyFixture(response, out, fixtureModelList, request.getParameter("tour_id"));
        } else {
            Extra.sendError(response, out, "Invalid tour_id or missing path parameters.");
        }
    }
    
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
    	
    	
    	
    	String pathInfoString = request.getPathInfo();		
		String[] pathArray = pathInfoString != null ?  pathInfoString.split("/") : null;
		
		
		PrintWriter out = response.getWriter();
    	
		if( pathArray == null || pathArray.length <= 1) {
			deleteAllFixture(request ,response , out);
		}
		else if(pathArray.length == 2) {
			deleteOneFixture(response , out  , pathArray[1]);
		}
		
        
    }
    
    protected void doPut(HttpServletRequest request, HttpServletResponse response)throws IOException , ServletException {
    	
    	StringBuilder jsonString = new StringBuilder();
        BufferedReader reader = request.getReader();
        String line;
        
        while((line = reader.readLine()) != null)
        		jsonString.append(line);
        
        
        FixtureModel fixtureModel = new Gson().fromJson(jsonString.toString(), FixtureModel.class);
        
        PrintWriter out = response.getWriter();
        
        String pathInfoString = request.getPathInfo();		
        String[] pathArray = pathInfoString != null ?  pathInfoString.split("/") : null;
        
        if ((pathArray != null && pathArray.length >= 2) ) {
             
        	updateOneFixture(response ,  out , fixtureModel , pathArray[1]);
        	
        } else {
            Extra.sendError(response, out, "Invalid tour_id or missing path parameters.");
        }
    	
    }
    
}

