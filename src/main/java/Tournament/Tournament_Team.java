package Tournament;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

@WebServlet("/tour_team")
public class Tournament_Team extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private static final String DB_URL = "jdbc:mysql://localhost:3306/CricketBuzz";
    private static final String USER = "root";
    private static final String PASS = "";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        int tourId = Integer.parseInt(request.getParameter("tour_id"));
    	int teamId = Integer.parseInt(request.getParameter("team_id"));
        int points = 0;
        double netRunRate = 0.0;

        String sql = "INSERT INTO tournament_team ( tour_id , team_id, points, net_run_rate) VALUES ( ? , ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, tourId);
        	pstmt.setInt(2, teamId);
            pstmt.setInt(3, points);
            pstmt.setDouble(4, netRunRate);
            
            pstmt.executeUpdate();
            response.getWriter().println("Team added to tournament successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
            response.getWriter().println("Database error: " + e.getMessage());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        int tourId = Integer.parseInt(request.getParameter("tour_id"));
        int teamId = Integer.parseInt(request.getParameter("team_id"));

        String sql = "DELETE FROM tournament_team WHERE tour_id = ? AND team_id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, tourId);
            pstmt.setInt(2, teamId);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                response.getWriter().println("Team deleted from tournament successfully.");
            } else {
                response.getWriter().println("No team found with the given tour_id.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            response.getWriter().println("Database error: " + e.getMessage());
        }
    }

    // Handle PUT request to update a record by tour_id
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
    	int teamId = Integer.parseInt(request.getParameter("team_id"));
        int tourId = Integer.parseInt(request.getParameter("tour_id"));
        int points = Integer.parseInt(request.getParameter("points"));
        double netRunRate = Double.parseDouble(request.getParameter("net_run_rate"));

        String sql = "UPDATE tournament_team SET points = ?, net_run_rate = ? WHERE tour_id = ? AND team_id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, points);
            pstmt.setDouble(2, netRunRate);
            pstmt.setInt(3, tourId);
            pstmt.setInt(4, teamId);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                response.getWriter().println("Team details updated successfully.");
            } else {
                response.getWriter().println("No team found with the given tour_id.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            response.getWriter().println("Database error: " + e.getMessage());
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        
        String tourIdStr = request.getParameter("tour_id");
        
        if (tourIdStr == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.println("{\"error\": \"tour_id parameter is required\"}");
            return;
        }

        int tourId;
        
        try {
            tourId = Integer.parseInt(tourIdStr);
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.println("{\"error\": \"Invalid tour_id format\"}");
            return;
        }

        String query = "SELECT t.team_id, t.name , t.category , tt.points , tt.net_run_rate " +
                       "FROM team t " +
                       "JOIN tournament_team tt ON t.team_id = tt.team_id " +
                       "WHERE tt.tour_id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, tourId);
            ResultSet rs = pstmt.executeQuery();

            JSONArray teamArray = new JSONArray();
            
            while (rs.next()) {
                JSONObject teamObject = new JSONObject();
                teamObject.put("team_id", rs.getInt("team_id"));
                teamObject.put("category", rs.getString("category"));
                teamObject.put("points", rs.getInt("points"));
                teamObject.put("net_run_rate", rs.getDouble("net_run_rate"));
                teamArray.put(teamObject);
            }

            if (teamArray.length() > 0) {
                out.print(teamArray.toString());
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"error\": \"No teams found for the given tour_id\"}");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.println("{\"error\": \"Database error: " + e.getMessage() + "\"}");
        }
    }
}
