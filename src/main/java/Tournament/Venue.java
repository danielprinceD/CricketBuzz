package Tournament;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.*;
import org.json.JSONObject;

@WebServlet("/venue")
public class Venue extends HttpServlet {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/CricketBuzz";
    private static final String USER = "root";
    private static final String PASS = "";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        String stadium = request.getParameter("stadium");
        String location = request.getParameter("location");
        String pitchCondition = request.getParameter("pitch_location");
        String description = request.getParameter("description");
        long capacity = Integer.parseInt(request.getParameter("capacity"));
        String curator = request.getParameter("curator");

        String sql = "INSERT INTO venue (stadium, location, pitch_condition, description, capacity, curator) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, stadium);
            pstmt.setString(2, location);
            pstmt.setString(3, pitchCondition);
            pstmt.setString(4, description);
            pstmt.setLong(5, capacity);
            pstmt.setString(6, curator);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                out.println("{\"message\": \"Venue created successfully!\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.println("{\"error\": \"Failed to create venue.\"}");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.println("{\"error\": \"Database error: " + e.getMessage() + "\"}");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
       
    	response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        String venueIdStr = request.getParameter("venue_id");
        if (venueIdStr == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.println("{\"error\": \"venue_id parameter is required\"}");
            return;
        }

        int venueId;
        try {
            venueId = Integer.parseInt(venueIdStr);
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.println("{\"error\": \"Invalid venue_id format\"}");
            return;
        }

        String query = "SELECT * FROM venue WHERE venue_id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, venueId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("venue_id", rs.getInt("venue_id"));
                jsonObject.put("stadium", rs.getString("stadium"));
                jsonObject.put("location", rs.getString("location"));
                jsonObject.put("pitch_condition", rs.getString("pitch_condition"));
                jsonObject.put("description", rs.getString("description"));
                jsonObject.put("capacity", rs.getLong("capacity"));
                jsonObject.put("curator", rs.getString("curator"));
                out.print(jsonObject.toString());
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"error\": \"Venue not found\"}");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.println("{\"error\": \"Database error: " + e.getMessage() + "\"}");
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        
        int venueId  = Integer.parseInt(request.getParameter("venue_id"));

        String stadium = request.getParameter("stadium");
        String location = request.getParameter("location");
        String pitchCondition = request.getParameter("pitch_location");
        String description = request.getParameter("description");
        long capacity = Integer.parseInt(request.getParameter("capacity"));
        String curator = request.getParameter("curator");

        String sql = "UPDATE venue SET stadium = ?, location = ?, pitch_condition = ?, description = ?, capacity = ?, curator = ? WHERE venue_id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, stadium);
            pstmt.setString(2, location);
            pstmt.setString(3, pitchCondition);
            pstmt.setString(4, description);
            pstmt.setLong(5, capacity);
            pstmt.setString(6, curator);
            pstmt.setInt(7, venueId);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                out.println("{\"message\": \"Venue updated successfully!\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.println("{\"error\": \"Venue not found\"}");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.println("{\"error\": \"Database error: " + e.getMessage() + "\"}");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        String venueIdStr = request.getParameter("venue_id");
        if (venueIdStr == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.println("{\"error\": \"venue_id parameter is required\"}");
            return;
        }

        int venueId;
        try {
            venueId = Integer.parseInt(venueIdStr);
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.println("{\"error\": \"Invalid venue_id format\"}");
            return;
        }

        String sql = "DELETE FROM venue WHERE venue_id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, venueId);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                out.println("{\"message\": \"Venue deleted successfully!\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.println("{\"error\": \"Venue not found\"}");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.println("{\"error\": \"Database error: " + e.getMessage() + "\"}");
        }
    }
}
