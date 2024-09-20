package Tournament;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.jasper.tagplugins.jstl.core.Out;
import org.eclipse.jdt.internal.compiler.lookup.ImplicitNullAnnotationVerifier;
import org.json.JSONArray;
import org.json.JSONObject;

import com.fasterxml.jackson.databind.jsontype.impl.AsExistingPropertyTypeSerializer;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import Model.PlayerModel;
import Model.TeamModel;
import Model.TournamentModel;
import Team.Extra;

public class Tournament extends HttpServlet {
	private static final long serialVersionUID = 1L;
    private static final String DB_URL = "jdbc:mysql://localhost:3306/CricketBuzz";
    private static final String USER = "root";
    private static final String PASS = "";
    
    public static void addData(PrintWriter out , ResultSet rs , JSONObject jsonObject)
    {
    	try {
    		jsonObject.put("tour_id", rs.getInt("tour_id"));
            jsonObject.put("name", rs.getString("name"));
            jsonObject.put("start_date", rs.getDate("start_date"));
            jsonObject.put("end_date", rs.getDate("end_date"));
            jsonObject.put("match_category", rs.getString("match_category"));
            jsonObject.put("season", rs.getInt("season"));
    		
		} catch (Exception e) {
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
		if (pathArray == null || pathArray.length == 0) {
	        String sql = "SELECT * FROM tournament";
	        JSONArray tournamentArray = new JSONArray();

	        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
	             Statement stmt = conn.createStatement();
	             ResultSet rs = stmt.executeQuery(sql)) {

	            while (rs.next()) {
	                JSONObject tournamentObject = new JSONObject();
	                addData(out , rs , tournamentObject);

	                String subQuery = "SELECT T.team_id , T.name, T.captain_id, T.vice_captain_id " +
	                                  "FROM tournament_team AS TT " +
	                                  "JOIN team AS T ON T.team_id = TT.team_id " +
	                                  "WHERE TT.tour_id = ?";
	                
	                try (PreparedStatement subStmt = conn.prepareStatement(subQuery)) {
	                    subStmt.setInt(1, rs.getInt("tour_id"));
	                    ResultSet subRs = subStmt.executeQuery();

	                    JSONArray teamsArray = new JSONArray();
	                    while (subRs.next()) {
	                        JSONObject teamObject = new JSONObject();
	                        teamObject.put("team_id" , subRs.getInt("team_id"));
	                        teamObject.put("name", subRs.getString("name"));
	                        teamObject.put("captain_id", subRs.getInt("captain_id"));
	                        teamObject.put("vice_captain_id", subRs.getInt("vice_captain_id"));
	                        teamsArray.put(teamObject);
	                    }
	                    tournamentObject.put("particated_teams", teamsArray);
	                }

	                tournamentArray.put(tournamentObject);
	            }

	            response.setContentType("application/json");
	            response.getWriter().print(tournamentArray.toString());
	            response.getWriter().flush();

	        } catch (SQLException e) {
	            e.printStackTrace();
	            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error: " + e.getMessage());
	        }

	        return;
	    }
		
		String tourId = pathArray[1];

	    try {
	        Integer.parseInt(tourId); 
	    } catch (NumberFormatException e) {
	        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid Tournament ID format");
	        return;
	    }

		
		String query = "SELECT * FROM tournament WHERE tour_id = ?";

		
	    try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
	         PreparedStatement stmt = conn.prepareStatement(query)) {

	        stmt.setInt(1, Integer.parseInt(tourId));
	        ResultSet rs = stmt.executeQuery();

	        if (rs.next()) {
	            JSONObject jsonObject = new JSONObject();
	            addData(out , rs , jsonObject);

	            String teamQuery = "SELECT T.team_id, T.name, T.captain_id, T.vice_captain_id " +
	                               "FROM tournament_team AS TT " +
	                               "JOIN team AS T ON T.team_id = TT.team_id " +
	                               "WHERE TT.tour_id = ?";

	            try (PreparedStatement teamStmt = conn.prepareStatement(teamQuery)) {
	                teamStmt.setInt(1, Integer.parseInt(tourId));
	                ResultSet teamRs = teamStmt.executeQuery();

	                JSONArray teamsArray = new JSONArray();
	                while (teamRs.next()) {
	                    JSONObject teamObject = new JSONObject();
	                    teamObject.put("team_id" , teamRs.getInt("team_id"));
	                    teamObject.put("name", teamRs.getString("name"));
	                    teamObject.put("captain_id", teamRs.getInt("captain_id"));
	                    teamObject.put("vice_captain_id", teamRs.getInt("vice_captain_id"));
	                    teamsArray.put(teamObject);
	                }
	                jsonObject.put("particated_teams", teamsArray);
	            }

	            response.setContentType("application/json");
	            response.getWriter().print(jsonObject.toString());
	        } else {
	            response.sendError(HttpServletResponse.SC_NOT_FOUND, "No Tournament found with ID: " + tourId);
	        }

	    } catch (SQLException e) {
	        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error: " + e.getMessage());
	        e.printStackTrace();
	    }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {

        StringBuilder jsonString = new StringBuilder();
        BufferedReader reader = request.getReader();
        String line;

        while ((line = reader.readLine()) != null) {
            jsonString.append(line);
        }
        
        
        PrintWriter out = response.getWriter();
        TournamentModel tourModel = new Gson().fromJson(jsonString.toString(), TournamentModel.class);
        String pathInfo = request.getPathInfo();
        String[] pathArray = pathInfo != null ? pathInfo.split("/") : null;
        if (tourModel.isValid()) {
        	if(pathArray != null && pathArray.length >= 2)
        	{
        		Connection conn;
				try {
					conn = DriverManager.getConnection(DB_URL , USER  ,PASS);
					addTourTeam( request.getParameter("teamList") , conn , Integer.parseInt(pathArray[1]));
					out.print("TeamList Added Successfully");
				} catch (SQLException e) {
					
					e.printStackTrace();
					Extra.sendError(response, out, e.getMessage());
				}
        	}
        	else 
            insertTournament(request ,  response, out, tourModel);
        } else {
            Extra.sendError(response, out, "Invalid tournament data");
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        StringBuilder jsonString = new StringBuilder();
        BufferedReader reader = request.getReader();
        String line;

        while ((line = reader.readLine()) != null) {
            jsonString.append(line);
        }

        PrintWriter out = response.getWriter();
        TournamentModel tourModel = new Gson().fromJson(jsonString.toString(), TournamentModel.class);

        String pathInfoString = request.getPathInfo();
        String[] pathArray = pathInfoString != null ? pathInfoString.split("/") : null;

        if (pathArray != null && pathArray.length > 1) {
            try {
                Integer idInteger = Integer.parseInt(pathArray[1]);
                tourModel.setTourId(idInteger);
            } catch (Exception e) {
                Extra.sendError(response, out, "Enter Valid ID");
                e.printStackTrace();
                return;
            }
            
            
            if (tourModel.isValid() && tourModel.getTourId() > 0) {
                updateTournament(response, out, tourModel);
            } else {
                Extra.sendError(response, out, "Invalid tournament data or ID");
            }
        }
        else {
            Extra.sendError(response, out, "Tournament ID is required");
        }
    }

    private void insertTournament(HttpServletRequest request, HttpServletResponse response, PrintWriter out, TournamentModel tourModel) {
        String sql = "INSERT INTO tournament (name, start_date, end_date, match_category, season) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, tourModel.getName());
            pstmt.setString(2, tourModel.getStartDate());
            pstmt.setString(3, tourModel.getEndDate());
            pstmt.setString(4, tourModel.getMatchCategory());
            pstmt.setInt(5, tourModel.getSeason());

            
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                
                if (generatedKeys.next()) {
                    int tourId = generatedKeys.getInt(1);  
                    
                    String playersList = request.getParameter("teamList");
                    
                    addTourTeam(playersList, conn, tourId);
                    
                    Extra.sendSuccess(response, out, "Tournament inserted successfully");
                } else {
                    Extra.sendError(response, out, "Failed to retrieve the tournament ID");
                }
            } else {
                Extra.sendError(response, out, "Failed to insert tournament");
            }
        } catch (SQLException e) {
            Extra.sendError(response, out, e.getMessage());
        }
    }

    private void addTourTeam(String playersList , Connection conn , Integer tourId) throws SQLException {
    	if (playersList != null && !playersList.isEmpty()) {
            Set<Integer> playerSet = new HashSet<>();
            
         
            for (String it : playersList.split(",")) {
                playerSet.add(Integer.parseInt(it));
            }
            
      
            String subQuery = "INSERT INTO tournament_team (tour_id, team_id) VALUES (?, ?)";
            try (PreparedStatement subStatement = conn.prepareStatement(subQuery)) {
                for (Integer playerId : playerSet) {
                    subStatement.setInt(1, tourId);
                    subStatement.setInt(2, playerId);
                    subStatement.addBatch();  
                }
                subStatement.executeBatch();  
            }
        }
    }

    private void updateTournament( HttpServletResponse response, PrintWriter out, TournamentModel tourModel) {
        String sql = "UPDATE tournament SET name = ?, start_date = ?, end_date = ?, match_category = ?, season = ? WHERE tour_id = ?";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
        
            pstmt.setString(1, tourModel.getName());
            pstmt.setString(2, tourModel.getStartDate());
            pstmt.setString(3, tourModel.getEndDate());
            pstmt.setString(4, tourModel.getMatchCategory());
            pstmt.setInt(5, tourModel.getSeason());
            pstmt.setInt(6, tourModel.getTourId());

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                Extra.sendSuccess(response, out, "Tournament updated successfully");
            } else {
                Extra.sendError(response, out, "No records updated, check the ID");
            }
        } catch (SQLException e) {
            Extra.sendError(response, out, e.getMessage());
        }
    }

    
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String pathInfoString = request.getPathInfo();
        String[] pathArray = pathInfoString != null ? pathInfoString.split("/") : null;
        PrintWriter out = response.getWriter();

        if (pathArray == null || pathArray.length <= 1) {
            Extra.sendError(response, out, "Tournament ID is required");
            return;
        }

        String tourId = pathArray[1];

        if (pathArray.length == 2) {
            deleteTournament(response, out, tourId);
        }
        else if (pathArray.length == 3 && pathArray[2].equals("teams") && request.getParameter("teamList") == null) {
            deleteAllTeamFromTour(response, out, tourId);
        }
        else if (pathArray.length == 3 && pathArray[2].equals("teams") && request.getParameter("teamList") != null) {
           
        	String teamList = request.getParameter("teamList");
            for(String teamId : teamList.split(","))
            	deleteTeamFromTour(response, out, tourId, teamId);
            
        } else {
            Extra.sendError(response, out, "Invalid request path");
        }
    }

    private void deleteTournament(HttpServletResponse response, PrintWriter out, String tourId) {
        if (tourId == null) {
            Extra.sendError(response, out, "Tournament ID is required");
            return;
        }

        String sql = "DELETE FROM tournament WHERE tour_id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, Integer.parseInt(tourId));
            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                Extra.sendSuccess(response, out, "Tournament deleted successfully");
            } else {
                Extra.sendError(response, out, "No tournament found with the provided ID");
            }
        } catch (NumberFormatException e) {
            Extra.sendError(response, out, "Invalid Tournament ID");
        } catch (SQLException e) {
            Extra.sendError(response, out, e.getMessage());
            e.printStackTrace();
        }
    }

    private void deleteAllTeamFromTour(HttpServletResponse response, PrintWriter out, String tourId) {
        String sql = "DELETE FROM tournament_team WHERE tour_id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, Integer.parseInt(tourId));
            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                Extra.sendSuccess(response, out, "All teams deleted from the tournament successfully");
            } else {
                Extra.sendError(response, out, "No teams found for the provided tournament ID");
            }
        } catch (NumberFormatException e) {
            Extra.sendError(response, out, "Invalid Tournament ID");
        } catch (SQLException e) {
            Extra.sendError(response, out, e.getMessage());
            e.printStackTrace();
        }
    }

    private void deleteTeamFromTour(HttpServletResponse response, PrintWriter out, String tourId, String teamId) {
        String sql = "DELETE FROM tournament_team WHERE tour_id = ? AND team_id = ?";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, Integer.parseInt(tourId));
            pstmt.setInt(2, Integer.parseInt(teamId));
            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                Extra.sendSuccess(response, out, "Team deleted from the tournament successfully");
            } else {
                Extra.sendError(response, out, "No team found with the provided IDs");
            }
        } catch (NumberFormatException e) {
            Extra.sendError(response, out, "Invalid Tournament or Team ID");
        } catch (SQLException e) {
            Extra.sendError(response, out, e.getMessage());
            e.printStackTrace();
        }
    }

    
    
}

