package Tournament;
import java.io.BufferedReader;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import com.fasterxml.jackson.databind.ext.Java7Handlers;
import com.google.gson.Gson;

import Model.TeamModel;
import Model.TourTeam;
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
            jsonObject.put("status", rs.getString("status"));
    		
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
       
		StringBuilder sql = new StringBuilder("SELECT * FROM tournament");
		List<Object> parameters = new ArrayList<>();

		boolean firstCondition = true;

		String name = request.getParameter("name");
		String startDate = request.getParameter("start_date");
		String endDate = request.getParameter("end_date");
		String matchCategory = request.getParameter("match_category");
		String season = request.getParameter("season");
		String status = request.getParameter("status");
		
		if(pathArray != null && pathArray.length == 2)
		{
			if (firstCondition) {
		        sql.append(" WHERE");
		        firstCondition = false;
		    } else {
		        sql.append(" AND");
		    }
		    sql.append(" tour_id = ?");
		    parameters.add(pathArray[1]);
		}
		
		if (status != null && !status.trim().isEmpty()) {
		    if (firstCondition) {
		        sql.append(" WHERE");
		        firstCondition = false;
		    } else {
		        sql.append(" AND");
		    }
		    sql.append(" status LIKE ?");
		    parameters.add("%" + status + "%");
		}
		
		if (name != null && !name.trim().isEmpty()) {
		    if (firstCondition) {
		        sql.append(" WHERE");
		        firstCondition = false;
		    } else {
		        sql.append(" AND");
		    }
		    sql.append(" name LIKE ?");
		    parameters.add("%" + name + "%");
		}
		if (startDate != null && !startDate.trim().isEmpty()) {
		    if (firstCondition) {
		        sql.append(" WHERE");
		        firstCondition = false;
		    } else {
		        sql.append(" AND");
		    }
		    sql.append(" start_date >= ?");
		    parameters.add(startDate);
		}
		if (endDate != null && !endDate.trim().isEmpty()) {
		    if (firstCondition) {
		        sql.append(" WHERE");
		        firstCondition = false;
		    } else {
		        sql.append(" AND");
		    }
		    sql.append(" end_date <= ?");
		    parameters.add(endDate);
		}
		if (matchCategory != null && !matchCategory.trim().isEmpty()) {
		    if (firstCondition) {
		        sql.append(" WHERE");
		        firstCondition = false;
		    } else {
		        sql.append(" AND");
		    }
		    sql.append(" match_category = ?");
		    parameters.add(matchCategory);
		}
		if (season != null && !season.trim().isEmpty()) {
		    if (firstCondition) {
		        sql.append(" WHERE");
		        firstCondition = false;
		    } else {
		        sql.append(" AND");
		    }
		    sql.append(" season = ?");
		    parameters.add(Integer.parseInt(season));
		}

			JSONArray tournamentArray = new JSONArray();

			try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
			     PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

			    for (int i = 0; i < parameters.size(); i++) {
			        stmt.setObject(i + 1, parameters.get(i));
			    }

			    ResultSet rs = stmt.executeQuery();

			    while (rs.next()) {
			        JSONObject tournamentObject = new JSONObject();
			        
			        addData(out, rs, tournamentObject);

			        String subQuery = "SELECT T.team_id , TT.net_run_rate , TT.points " +
			                          "FROM tournament_team AS TT " +
			                          "JOIN team AS T ON T.team_id = TT.team_id " +
			                          "WHERE TT.tour_id = ?";
			        
			        try (PreparedStatement subStmt = conn.prepareStatement(subQuery)) {
			            subStmt.setInt(1, rs.getInt("tour_id"));
			            ResultSet subRs = subStmt.executeQuery();

			            JSONArray teamsArray = new JSONArray();
			            
			            while (subRs.next())
			            {
			            	JSONObject teamObject = new JSONObject();
			            	teamObject.put("team_id", subRs.getInt("team_id"));
			            	teamObject.put("points", subRs.getInt("points"));
			            	teamObject.put("net_run_rate", subRs.getDouble("net_run_rate"));
			            	
			            	
			            	teamsArray.put(teamObject);
			            }
			            
			            tournamentObject.put("participated_teams", teamsArray);
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

    }
    
    private String prepareSqlStatement(HttpServletRequest request, TournamentModel tournamentModel, HttpServletResponse response, PrintWriter out) {

        if (tournamentModel.getTourId() < 0 && tournamentModel.isValid()) {
            return "INSERT INTO tournament (name, start_date, end_date, match_category, season , status) VALUES (?, ?, ?, ?, ? , ? )";
        } else if (tournamentModel.isValid() && request.getMethod().equalsIgnoreCase("PUT")) {
            return "UPDATE tournament SET name = ?, start_date = ?, end_date = ?, match_category = ?, season = ? , status = ? WHERE tour_id = ?";
        } else {
            Extra.sendError(response, out, "Invalid data or missing parameters.");
            return null;
        }
    }
    
    private boolean validateTourTeam(TournamentModel teamModel, Set<Integer> teamSet, HttpServletResponse response, PrintWriter out) {
        
    	for (TourTeam team : teamModel.tour_team ) {
            if (teamSet.contains(team.getTeamId())) {
                Extra.sendError(response, out, "Team cannot be added more than once");
                return false;
            }
            teamSet.add(team.getTeamId());
        }

        return true;
    }
    
    private void setPreparedStatementValues(PreparedStatement pstmt, TournamentModel tourModel) throws SQLException {
        pstmt.setString(1, tourModel.getName());
        pstmt.setString(2, tourModel.getStartDate());
        pstmt.setString(3, tourModel.getEndDate());
        pstmt.setString(4, tourModel.getMatchCategory());
        pstmt.setInt(5, tourModel.getSeason());
        pstmt.setString(6 , tourModel.getStatus());
        if (tourModel.getTourId() > 0) {
            pstmt.setInt(7, tourModel.getTourId());
        }
    }
    
    
    private int getGeneratedTourId(PreparedStatement pstmt) throws SQLException {
        try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
            if (generatedKeys.next()) {
                return generatedKeys.getInt(1);
            }
        }
        return -1;
    }
    
    
	private void addTeamsToTour(Connection conn, Set<Integer> teamSet, int tourId , TournamentModel tourModel) throws SQLException {
	    	
			String selectQuery = "SELECT team_id FROM tournament_team WHERE tour_id = ?";
		    Set<Integer> existingTeamIds = new HashSet<>();
	
		    try (PreparedStatement selectPstmt = conn.prepareStatement(selectQuery)) {
		        selectPstmt.setInt(1, tourId);
		        ResultSet resultSet = selectPstmt.executeQuery();
	
		        while (resultSet.next()) {
		            existingTeamIds.add(resultSet.getInt("team_id"));
		        }
		    }
	
		    Set<Integer> teamsToRemove = new HashSet<>(existingTeamIds);
		    teamsToRemove.removeAll(teamSet);
		    	
		    if (!teamsToRemove.isEmpty()) {
		        String deleteQuery = "DELETE FROM tournament_team WHERE tour_id = ? AND team_id = ?";
		        try (PreparedStatement deletePstmt = conn.prepareStatement(deleteQuery)) {
		            for (Integer teamId : teamsToRemove) {
		                deletePstmt.setInt(1, tourId);
		                deletePstmt.setInt(2, teamId);
		                deletePstmt.executeUpdate();
		            }
		        }
		    }
		    
		    String insertOrUpdateQuery = "INSERT INTO tournament_team (tour_id, team_id, points, net_run_rate) VALUES (?, ?, ?, ?) " +
			                    "ON DUPLICATE KEY UPDATE points = VALUES(points), net_run_rate = VALUES(net_run_rate)";
			
			try (PreparedStatement pstmt = conn.prepareStatement(insertOrUpdateQuery)) {
			
				for (TourTeam tourTeam : tourModel.tour_team) {
				
					if (!isValidTeam(tourTeam.getTeamId())) {
					  throw new SQLException("Team ID " + tourTeam.getTeamId() + " is not a team "+ tourId + "."  );
					}
					
					pstmt.setInt(1, tourId);
					pstmt.setInt(2, tourTeam.getTeamId());     
					
					
					
					pstmt.setObject(3, tourTeam.getPoints() , java.sql.Types.INTEGER);
					pstmt.setObject(4, tourTeam.getNetRunRate() , java.sql.Types.DECIMAL);
					
					pstmt.executeUpdate();
				}
			}
	    }
	
	
	public boolean isValidTeam(int teamId) {
	    
		String query = "SELECT COUNT(*) FROM team WHERE team_id = ?";
	    boolean exists = false;

	    try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
	         PreparedStatement pstmt = conn.prepareStatement(query)) {
	        
	        pstmt.setInt(1, teamId);

	        ResultSet resultSet = pstmt.executeQuery();
	        if (resultSet.next()) {
	            exists = resultSet.getInt(1) > 0; 
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    
	    return exists; 
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
        
        TypeToken<List<TournamentModel>> token = new TypeToken<List<TournamentModel>>() {};
        List<TournamentModel> tourModelList = new Gson().fromJson(jsonString.toString(), token.getType());

        PrintWriter out = response.getWriter();


        for (TournamentModel tourModel : tourModelList) {
            
        	if(request.getMethod().equalsIgnoreCase("PUT"))
        	{
        		if(tourModel.getTourId() < 0)
        		{
        			Extra.sendError(response, out, "TourId is required to update");
        			return;
        		}
        	}
        	
        	Set<Integer> teamSet = new HashSet<>();
            
            if (!validateTourTeam(tourModel, teamSet , response, out)) {
                return;
            }
            
            String sql = prepareSqlStatement(request, tourModel, response, out);
            if (sql == null) {
                return;
            }

            try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
                 PreparedStatement pstmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                
                conn.setAutoCommit(false); 
                
                setPreparedStatementValues(pstmt, tourModel);

                int rowsAffected = pstmt.executeUpdate();
                int tourId = tourModel.getTourId(); 
              
                if (tourId < 0 && request.getMethod().equalsIgnoreCase("POST")) {
                    tourId = getGeneratedTourId(pstmt);
                }
                
                

                if (tourId > 0) {
                    addTeamsToTour(conn, teamSet, tourId , tourModel);
                }

                if (rowsAffected > 0) {
                    conn.commit();
                    Extra.sendSuccess(response, out, "Team and players inserted/updated successfully");
                } else {
                    conn.rollback();
                    Extra.sendError(response, out, "Failed to insert/update team");
                }

            } catch (SQLException e) {
                Extra.sendError(response, out, e.getMessage());
                e.printStackTrace();
            }
        }   
    }


    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

       doPost(request, response);
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
        else if (pathArray.length == 3 && pathArray[2].equals("teams") ) {
            deleteAllTeamFromTour(response, out, tourId);
        }
        else if (pathArray.length == 4 && pathArray[2].equals("teams") ) {
           
            	deleteTeamFromTour(response, out, tourId, pathArray[3]);
            
        }else {
            Extra.sendError(response, out, "Invalid request path");
        }
    }

    private void deleteTournament(HttpServletResponse response, PrintWriter out, String tourId) throws ServletException, IOException {
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

