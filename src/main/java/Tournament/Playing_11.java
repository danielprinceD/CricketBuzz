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

@WebServlet("/playing_11")
public class Playing_11 extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    private static final String DB_URL = "jdbc:mysql://localhost:3306/CricketBuzz";
    private static final String USER = "root";
    private static final String PASS = "";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int teamId = Integer.parseInt(request.getParameter("team_id"));
        int fixtureId = Integer.parseInt(request.getParameter("fixture_id"));

        String sql = "SELECT * FROM playing_11 WHERE team_id = ? AND fixture_id = ?";
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
           
            pstmt.setInt(1, teamId);
            pstmt.setInt(2, fixtureId);

            ResultSet rs = pstmt.executeQuery();
            JSONArray playerArray = new JSONArray();

            while (rs.next()) {
                JSONObject playerJson = new JSONObject();
                playerJson.put("fixture_id", rs.getInt("fixture_id"));
                playerJson.put("player_id", rs.getInt("player_id"));
                playerJson.put("role", rs.getString("role"));
                playerJson.put("runs", rs.getInt("runs"));
                playerJson.put("balls_faced", rs.getInt("balls_faced"));
                playerJson.put("fours", rs.getInt("fours"));
                playerJson.put("sixes", rs.getInt("sixes"));
                playerJson.put("fifties", rs.getInt("fifties"));
                playerJson.put("hundreds", rs.getInt("hundreds"));
                playerJson.put("wickets_taken", rs.getInt("wickets_taken"));
                playerJson.put("team_id", rs.getInt("team_id"));
                
                playerArray.put(playerJson);
            }

            PrintWriter out = response.getWriter();
            out.print(playerArray.toString());
            out.flush();
        } catch (SQLException e) {
            e.printStackTrace();
            response.getWriter().println("Database error: " + e.getMessage());
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
    	
        int playerId = Integer.parseInt(request.getParameter("player_id"));
        int teamId = Integer.parseInt(request.getParameter("team_id"));
        int fixtureId = Integer.parseInt(request.getParameter("fixture_id"));
        String role = request.getParameter("role");
        int runs = Integer.parseInt(request.getParameter("runs"));
        int ballsFaced = Integer.parseInt(request.getParameter("balls_faced"));
        int fours = Integer.parseInt(request.getParameter("fours"));
        int sixes = Integer.parseInt(request.getParameter("sixes"));
        int fifties = Integer.parseInt(request.getParameter("fifties"));
        int hundreds = Integer.parseInt(request.getParameter("hundreds"));
        int wicketsTaken = Integer.parseInt(request.getParameter("wickets_taken"));

        String sql = "UPDATE playing_11 SET role = ?, runs = ?, balls_faced = ?, fours = ?, sixes = ?, "
                   + "fifties = ?, hundreds = ?, wickets_taken = ? WHERE player_id = ?  AND team_id = ? AND fixture_id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, role);
            pstmt.setInt(2, runs);
            pstmt.setInt(3, ballsFaced);
            pstmt.setInt(4, fours);
            pstmt.setInt(5, sixes);
            pstmt.setInt(6, fifties);
            pstmt.setInt(7, hundreds);
            pstmt.setInt(8, wicketsTaken);
            pstmt.setInt(9, playerId);
            pstmt.setInt(10, teamId);
            pstmt.setInt(11, fixtureId);

            int rowsAffected = pstmt.executeUpdate();
            PrintWriter out = response.getWriter();
            if (rowsAffected > 0) {
                out.println("Player details updated successfully.");
            } else {
                out.println("Failed to update player details.");
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
        int playerId = Integer.parseInt(request.getParameter("player_id"));

        String sql = "DELETE FROM playing_11 WHERE fixture_id = ? AND player_id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, fixtureId);
            pstmt.setInt(2, playerId);

            int rowsAffected = pstmt.executeUpdate();
            PrintWriter out = response.getWriter();
            if (rowsAffected > 0) {
                out.println("Player record deleted successfully.");
            } else {
                out.println("Failed to delete player record.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            response.getWriter().println("Database error: " + e.getMessage());
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
    	
        int playerId = Integer.parseInt(request.getParameter("player_id"));
        int teamId = Integer.parseInt(request.getParameter("team_id"));
        int fixtureId = Integer.parseInt(request.getParameter("fixture_id"));
        String role = request.getParameter("role");

        String sql = "INSERT INTO playing_11 ( fixture_id, player_id, team_id, role) "
                   + "VALUES (?, ?, ?, ?)";

        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, fixtureId);
            pstmt.setInt(2, playerId);
            pstmt.setInt(3, teamId);
            pstmt.setString(4, role);

            int rowsAffected = pstmt.executeUpdate();

            PrintWriter out = response.getWriter();
            if (rowsAffected > 0) {
                out.println("New player data inserted successfully.");
            } else {
                out.println("Failed to insert player data.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            response.getWriter().println("Database error: " + e.getMessage());
        }
    }

    
}
