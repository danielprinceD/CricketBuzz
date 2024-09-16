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
import org.json.JSONObject;

@WebServlet("/match_details")
public class MatchDetails extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private static final String DB_URL = "jdbc:mysql://localhost:3306/CricketBuzz";
    private static final String USER = "root";
    private static final String PASS = "";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        int fixtureId = Integer.parseInt(request.getParameter("fixture_id"));
        int tossWin = Integer.parseInt(request.getParameter("toss_win"));
        String result = request.getParameter("result");
        int manOfTheMatch = Integer.parseInt(request.getParameter("man_of_the_match"));
        String tossWinDecision = request.getParameter("toss_win_decision");

        String sql = "INSERT INTO match_details (fixture_id, toss_win, result, man_of_the_match, toss_win_decision) "
                   + "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, fixtureId);
            pstmt.setInt(2, tossWin);
            pstmt.setString(3, result);
            pstmt.setInt(4, manOfTheMatch);
            pstmt.setString(5, tossWinDecision);

            int rowsAffected = pstmt.executeUpdate();
            PrintWriter out = response.getWriter();
            if (rowsAffected > 0) {
                out.println("Match details added successfully.");
            } else {
                out.println("Failed to add match details.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            response.getWriter().println("Database error: " + e.getMessage());
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        int fixtureId = Integer.parseInt(request.getParameter("fixture_id"));

        String sql = "SELECT * FROM match_details WHERE fixture_id = ?";
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, fixtureId);
            ResultSet rs = pstmt.executeQuery();

            JSONObject matchDetailsJson = new JSONObject();
            if (rs.next()) {
                matchDetailsJson.put("fixture_id", rs.getInt("fixture_id"));
                matchDetailsJson.put("toss_win", rs.getInt("toss_win"));
                matchDetailsJson.put("result", rs.getString("result"));
                matchDetailsJson.put("man_of_the_match", rs.getInt("man_of_the_match"));
                matchDetailsJson.put("toss_win_decision", rs.getString("toss_win_decision"));
            }

            PrintWriter out = response.getWriter();
            out.print(matchDetailsJson.toString());
            out.flush();
        } catch (SQLException e) {
            e.printStackTrace();
            response.getWriter().println("Database error: " + e.getMessage());
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        int fixtureId = Integer.parseInt(request.getParameter("fixture_id"));
        int tossWin = Integer.parseInt(request.getParameter("toss_win"));
        String result = request.getParameter("result");
        int manOfTheMatch = Integer.parseInt(request.getParameter("man_of_the_match"));
        String tossWinDecision = request.getParameter("toss_win_decision");

        String sql = "UPDATE match_details SET toss_win = ?, result = ?, man_of_the_match = ?, toss_win_decision = ? "
                   + "WHERE fixture_id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, tossWin);
            pstmt.setString(2, result);
            pstmt.setInt(3, manOfTheMatch);
            pstmt.setString(4, tossWinDecision);
            pstmt.setInt(5, fixtureId);

            int rowsAffected = pstmt.executeUpdate();
            PrintWriter out = response.getWriter();
            if (rowsAffected > 0) {
                out.println("Match details updated successfully.");
            } else {
                out.println("Failed to update match details.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            response.getWriter().println("Database error: " + e.getMessage());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        int fixtureId = Integer.parseInt(request.getParameter("fixture_id"));

        String sql = "DELETE FROM match_details WHERE fixture_id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, fixtureId);

            int rowsAffected = pstmt.executeUpdate();
            PrintWriter out = response.getWriter();
            if (rowsAffected > 0) {
                out.println("Match details deleted successfully.");
            } else {
                out.println("Failed to delete match details.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            response.getWriter().println("Database error: " + e.getMessage());
        }
    }
}

