package Team;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

@WebServlet("/team")
public class Team extends HttpServlet {
	private static final long serialVersionUID = 1L;
    private static final String DB_URL = "jdbc:mysql://localhost:3306/CricketBuzz";
    private static final String USER = "root";
    private static final String PASS = "";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        String teamId = request.getParameter("team_id");
        
        if (teamId == null || teamId.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\": \"team_id parameter is required\"}");
            return;
        }

        String query = "SELECT * FROM team WHERE team_id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, Integer.parseInt(teamId));
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("team_id", rs.getInt("team_id"));
                jsonObject.put("name", rs.getString("name"));
                jsonObject.put("captain_id", rs.getInt("captain_id"));
                jsonObject.put("vice_captain_id", rs.getInt("vice_captain_id"));
                jsonObject.put("wicket_keeper_id", rs.getInt("wicket_keeper_id"));
                jsonObject.put("category", rs.getString("category"));
                out.print(jsonObject.toString());
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"error\": \"Team not found\"}");
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\": \"Invalid team_id format\"}");
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
        String captainId = request.getParameter("captain_id");
        String viceCaptainId = request.getParameter("vice_captain_id");
        String wicketKeeperId = request.getParameter("wicket_keeper_id");
        String category = request.getParameter("category");
        String name = request.getParameter("name");

        if (category == null || category.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.println("category parameter is required.");
            return;
        }
        
        if (captainId != null && viceCaptainId != null && captainId.equals(viceCaptainId)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("captain_id and vice_captain_id cannot have the same value.");
            return;
        }
        
        String sql = "INSERT INTO team (captain_id, vice_captain_id, wicket_keeper_id, category , name) "
                   + "VALUES (?, ?, ?, ? , ? )";

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setObject(1, captainId != null ? Integer.parseInt(captainId) : null, Types.INTEGER);
            pstmt.setObject(2, viceCaptainId != null ? Integer.parseInt(viceCaptainId) : null, Types.INTEGER);
            pstmt.setObject(3, wicketKeeperId != null ? Integer.parseInt(wicketKeeperId) : null, Types.INTEGER);
            pstmt.setString(4, category);
            pstmt.setString(5, name);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                out.println("Team added successfully!");
            } else {
                out.println("Failed to add team.");
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.println("Invalid ID format.");
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
        String teamId = request.getParameter("team_id");
        String captainId = request.getParameter("captain_id");
        String viceCaptainId = request.getParameter("vice_captain_id");
        String wicketKeeperId = request.getParameter("wicket_keeper_id");
        String category = request.getParameter("category");
        String name = request.getParameter("name");

        if (teamId == null || teamId.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.println("team_id parameter is required.");
            return;
        }

        if (category == null || category.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.println("category parameter is required.");
            return;
        }

        String sql = "UPDATE team SET captain_id = ?, vice_captain_id = ?, wicket_keeper_id = ?, category = ? , name = ? "
                   + "WHERE team_id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setObject(1, captainId != null ? Integer.parseInt(captainId) : null, Types.INTEGER);
            pstmt.setObject(2, viceCaptainId != null ? Integer.parseInt(viceCaptainId) : null, Types.INTEGER);
            pstmt.setObject(3, wicketKeeperId != null ? Integer.parseInt(wicketKeeperId) : null, Types.INTEGER);
            pstmt.setString(4, category);
            pstmt.setString(5, name);
            pstmt.setInt(6, Integer.parseInt(teamId));

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                out.println("Team updated successfully.");
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.println("No team found with the provided ID.");
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.println("Invalid ID format.");
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
        String teamId = request.getParameter("team_id");

        if (teamId == null || teamId.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.println("team_id parameter is required.");
            return;
        }

        String sql = "DELETE FROM team WHERE team_id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, Integer.parseInt(teamId));
            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                out.println("Team deleted successfully.");
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.println("No team found with the provided ID.");
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.println("Invalid team_id format.");
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.println("Database error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
