package Tournament;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;

@WebServlet("/tournament")
public class Tournament extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final String DB_URL = "jdbc:mysql://localhost:3306/CricketBuzz";
    private static final String USER = "root";
    private static final String PASS = "";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        String tourId = request.getParameter("tour_id");
        
        if (tourId == null || tourId.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\": \"tour_id parameter is required\"}");
            return;
        }

        String query = "SELECT * FROM tournament WHERE tour_id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, Integer.parseInt(tourId));
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("tour_id", rs.getInt("tour_id"));
                jsonObject.put("name", rs.getString("name"));
                jsonObject.put("start_date", rs.getDate("start_date"));
                jsonObject.put("end_date", rs.getDate("end_date"));
                jsonObject.put("match_category", rs.getString("match_category"));
                jsonObject.put("season", rs.getInt("season"));
                out.print(jsonObject.toString());
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"error\": \"Tournament not found\"}");
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\": \"Invalid tour_id format\"}");
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\": \"Error fetching data\"}");
            e.printStackTrace();
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {

        response.setContentType("text/plain");
        PrintWriter out = response.getWriter();
        String name = request.getParameter("name");
        String startDate = request.getParameter("start_date");
        String endDate = request.getParameter("end_date");
        String matchCategory = request.getParameter("match_category");
        String seasonStr = request.getParameter("season");

        if (name == null || name.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.println("name parameter is required.");
            return;
        }

        int season = 0;
        if (seasonStr != null && !seasonStr.trim().isEmpty()) {
            try {
                season = Integer.parseInt(seasonStr);
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.println("Invalid season format.");
                return;
            }
        }

        String sql = "INSERT INTO tournament (name, start_date, end_date, match_category, season) "
                   + "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            pstmt.setDate(2, startDate != null && !startDate.trim().isEmpty() ? Date.valueOf(startDate) : null);
            pstmt.setDate(3, endDate != null && !endDate.trim().isEmpty() ? Date.valueOf(endDate) : null);
            pstmt.setString(4, matchCategory);
            pstmt.setInt(5, season);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                out.println("Tournament added successfully!");
            } else {
                out.println("Failed to add tournament.");
            }
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.println("Database error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {

        response.setContentType("text/plain");
        PrintWriter out = response.getWriter();
        String tourId = request.getParameter("tour_id");
        String name = request.getParameter("name");
        String startDate = request.getParameter("start_date");
        String endDate = request.getParameter("end_date");
        String matchCategory = request.getParameter("match_category");
        String seasonStr = request.getParameter("season");

        if (tourId == null || tourId.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.println("tour_id parameter is required.");
            return;
        }

        if (name == null || name.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.println("name parameter is required.");
            return;
        }

        int season = 0;
        if (seasonStr != null && !seasonStr.trim().isEmpty()) {
            try {
                season = Integer.parseInt(seasonStr);
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.println("Invalid season format.");
                return;
            }
        }

        String sql = "UPDATE tournament SET name = ?, start_date = ?, end_date = ?, match_category = ?, season = ? "
                   + "WHERE tour_id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            pstmt.setDate(2, startDate != null && !startDate.trim().isEmpty() ? Date.valueOf(startDate) : null);
            pstmt.setDate(3, endDate != null && !endDate.trim().isEmpty() ? Date.valueOf(endDate) : null);
            pstmt.setString(4, matchCategory);
            pstmt.setInt(5, season);
            pstmt.setInt(6, Integer.parseInt(tourId));

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                out.println("Tournament updated successfully.");
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.println("No tournament found with the provided ID.");
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.println("Invalid tour_id format.");
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.println("Database error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {

        response.setContentType("text/plain");
        PrintWriter out = response.getWriter();
        String tourId = request.getParameter("tour_id");

        if (tourId == null || tourId.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.println("tour_id parameter is required.");
            return;
        }

        String sql = "DELETE FROM tournament WHERE tour_id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, Integer.parseInt(tourId));
            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                out.println("Tournament deleted successfully.");
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.println("No tournament found with the provided ID.");
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.println("Invalid tour_id format.");
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.println("Database error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
