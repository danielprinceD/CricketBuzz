package Tournament;
import java.io.BufferedReader;
import com.google.gson.reflect.TypeToken;

import DAO.TournamentDAO;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.gson.Gson;
import Model.TournamentVO;
import Servlet.Extra;

public class TournamentServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
    private static final String DB_URL = "jdbc:mysql://localhost:3306/CricketBuzz";
    private static final String USER = "root";
    private static final String PASS = "";
    
    private TournamentDAO tournamentDAO;
    
    @Override
    public void init() {
    	tournamentDAO = new TournamentDAO();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        
        try {
            String pathInfo = request.getPathInfo();
            
            if (pathInfo == null || pathInfo.equals("/")) {
            	
                List<TournamentVO> tournaments = tournamentDAO.getAllTournaments();
                out.print(new Gson().toJson(tournaments));
           
            } else {
            	
                int tourId = Integer.parseInt(pathInfo.substring(1));
                TournamentVO tournament = tournamentDAO.getTournamentById(tourId);

                if (tournament != null) {
                    out.print(new Gson().toJson(tournament));
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.print("{ \"error\": \"Tournament not found\" }");
                }
            }
        } catch (NumberFormatException | SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{ \"error\": \"" + e.getMessage() + "\" }");
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
        
        TypeToken<List<TournamentVO>> token = new TypeToken<List<TournamentVO>>() {};
        List<TournamentVO> tournamentsVO = new Gson().fromJson(jsonString.toString(), token.getType());

        PrintWriter out = response.getWriter();


        for (TournamentVO tournamentVO : tournamentsVO) {
            
        	if(request.getMethod().equalsIgnoreCase("PUT"))
        	{
        		if(tournamentVO.getTourId() < 0)
        		{
        			Extra.sendError(response, out, "TourId is required to update");
        			return;
        		}
        	}
        	
        	Set<Integer> teamSet = new HashSet<>();
            
            if (!tournamentDAO.validateTourTeam(tournamentVO, teamSet , response, out)) {
                return;
            }
            
            String sql = tournamentDAO.prepareSqlStatement(request, tournamentVO, response, out);
            if (sql == null) {
                return;
            }

            try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
                 PreparedStatement pstmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                
                conn.setAutoCommit(false); 
                
                tournamentDAO.setPreparedStatementValues(pstmt, tournamentVO);

                int rowsAffected = pstmt.executeUpdate();
                int tourId = tournamentVO.getTourId(); 
              
                if (tourId < 0 && request.getMethod().equalsIgnoreCase("POST")) {
                    tourId = tournamentDAO.getGeneratedTourId(pstmt);
                }
                
                

                if (tourId > 0) {
                    tournamentDAO.addTeamsToTour(conn, teamSet, tourId , tournamentVO);
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
           tournamentDAO.deleteTournament(response, out, tourId);
        }
        else if (pathArray.length == 3 && pathArray[2].equals("teams") ) {
        	tournamentDAO.deleteAllTeamFromTour(response, out, tourId);
        }
        else if (pathArray.length == 4 && pathArray[2].equals("teams") ) {
           
        	tournamentDAO.deleteTeamFromTour(response, out, tourId, pathArray[3]);
            
        }else {
            Extra.sendError(response, out, "Invalid request path");
        }
    }
    
}

