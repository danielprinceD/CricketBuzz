package Tournament;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

@WebServlet("/commentary")
public class Commentary extends HttpServlet {
	private static final long serialVersionUID = 1L;
  
	private static final String DB_URL = "jdbc:mysql://localhost:3306/CricketBuzz";
    private static final String USER = "root";
    private static final String PASS = "";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String fixtureIdParam = request.getParameter("fixture_id");

        if (fixtureIdParam == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing fixture_id parameter.");
            return;
        }

        int fixtureId;
        try {
            fixtureId = Integer.parseInt(fixtureIdParam);
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid fixture_id parameter.");
            return;
        }

        // SQL query with joins to get player details for batter_id, bowler_id, and catcher_id
        String sql = "SELECT c.fixture_id, c.over_count, c.ball, c.run_type, " +
                     "c.commentary_text, " +
                     "batter.id AS batter_id, batter.name AS batter_name, batter.role AS batter_role, batter.rating AS batter_rating, " +
                     "bowler.id AS bowler_id, bowler.name AS bowler_name, bowler.role AS bowler_role, bowler.rating AS bowler_rating, " +
                     "catcher.id AS catcher_id, catcher.name AS catcher_name, catcher.role AS catcher_role, catcher.rating AS catcher_rating, " +
                     "c.date_time " +
                     "FROM commentary c " +
                     "LEFT JOIN player batter ON c.batter_id = batter.id " +
                     "LEFT JOIN player bowler ON c.bowler_id = bowler.id " +
                     "LEFT JOIN player catcher ON c.catcher_id = catcher.id " +
                     "WHERE c.fixture_id = ?";
        
        JSONArray resultArray = new JSONArray();

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, fixtureId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("fixture_id", rs.getInt("fixture_id"));
                jsonObject.put("over_count", rs.getInt("over_count"));
                jsonObject.put("ball", rs.getInt("ball"));
                jsonObject.put("run_type", rs.getString("run_type"));
                jsonObject.put("commentary_text", rs.getString("commentary_text"));
                jsonObject.put("date_time", rs.getTimestamp("date_time"));

                // Batter details
                JSONObject batter = new JSONObject();
                batter.put("id", rs.getInt("batter_id"));
                batter.put("name", rs.getString("batter_name"));
                batter.put("role", rs.getString("batter_role"));
                batter.put("rating", rs.getInt("batter_rating"));
                jsonObject.put("batter", batter);

                // Bowler details
                JSONObject bowler = new JSONObject();
                bowler.put("id", rs.getInt("bowler_id"));
                bowler.put("name", rs.getString("bowler_name"));
                bowler.put("role", rs.getString("bowler_role"));
                bowler.put("rating", rs.getInt("bowler_rating"));
                jsonObject.put("bowler", bowler);

                // Catcher details
                int catcherId = rs.getInt("catcher_id");
                if (!rs.wasNull()) {
                    JSONObject catcher = new JSONObject();
                    catcher.put("id", catcherId);
                    catcher.put("name", rs.getString("catcher_name"));
                    catcher.put("role", rs.getString("catcher_role"));
                    catcher.put("rating", rs.getInt("catcher_rating"));
                    jsonObject.put("catcher", catcher);
                }

                resultArray.put(jsonObject);
            }

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            PrintWriter out = response.getWriter();
            out.print(resultArray.toString());

        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error: " + e.getMessage());
        }
    }


    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
    	
        int fixtureId = Integer.parseInt(request.getParameter("fixture_id"));
        int overCount = Integer.parseInt(request.getParameter("over_count"));
        int ball = Integer.parseInt(request.getParameter("ball"));
        String runType = request.getParameter("run_type");
        String commentaryText = request.getParameter("commentary_text");
        Integer batterId = parseInteger(request.getParameter("batter_id"));
        Integer bowlerId = parseInteger(request.getParameter("bowler_id"));
        String dateTimeParam = request.getParameter("date_time");
        Integer catcherId = parseInteger(request.getParameter("catcher_id"));

        String sql = "INSERT INTO commentary (fixture_id, over_count, ball, run_type, commentary_text, batter_id, bowler_id, date_time, catcher_id) "
                   + "VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, fixtureId);
            pstmt.setInt(2, overCount);
            pstmt.setInt(3, ball);
            pstmt.setString(4, runType);
            pstmt.setString(5, commentaryText);
            pstmt.setObject(6, batterId, java.sql.Types.INTEGER);
            pstmt.setObject(7, bowlerId, java.sql.Types.INTEGER);
            pstmt.setObject(8, dateTimeParam);
            pstmt.setObject(9, catcherId, java.sql.Types.INTEGER);

            int rowsAffected = pstmt.executeUpdate();

            PrintWriter out = response.getWriter();
            if (rowsAffected > 0) {
                out.println("Commentary data inserted successfully.");
            } else {
                out.println("Failed to insert commentary data.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error: " + e.getMessage());
        }
    }

    private Integer parseInteger(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        return Integer.parseInt(value);
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String fixtureIdParam = request.getParameter("fixture_id");
       
        if (fixtureIdParam == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing fixture_id parameter.");
            return;
        }

        int fixtureId;
        try {
            fixtureId = Integer.parseInt(fixtureIdParam);
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid fixture_id parameter.");
            return;
        }

        String sql = "DELETE FROM commentary WHERE fixture_id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, fixtureId);
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write("Deleted " + affectedRows + " rows.");
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "No records found for fixture_id " + fixtureId);
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error: " + e.getMessage());
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

    	
    	String overCountParam = request.getParameter("over_count");
    	String ballParam = request.getParameter("ball");
        String fixtureIdParam = request.getParameter("fixture_id");
        
        if (fixtureIdParam == null || ballParam == null || fixtureIdParam == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing parameter.");
            return;
        }

        int fixtureId , ball , overCount;
        try {
            fixtureId = Integer.parseInt(fixtureIdParam);
            ball = Integer.parseInt(ballParam);
            overCount = Integer.parseInt(overCountParam);
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid parameter.");
            return;
        }
        String runType = request.getParameter("run_type");
        String commentaryText = request.getParameter("commentary_text");
        String batterIdParam = request.getParameter("batter_id");
        String bowlerIdParam = request.getParameter("bowler_id");
        String catcherIdParam = request.getParameter("catcher_id");
        String dateTimeParam = request.getParameter("date_time");

        if (dateTimeParam == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing required fields: over_count, ball, or date_time.");
            return;
        }

        int  batterId = 0, bowlerId = 0, catcherId = 0;
        try {

            if (batterIdParam != null) batterId = Integer.parseInt(batterIdParam);
            if (bowlerIdParam != null) bowlerId = Integer.parseInt(bowlerIdParam);
            if (catcherIdParam != null) catcherId = Integer.parseInt(catcherIdParam);
            
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid numerical parameter.");
            return;
        }

        String sql = "UPDATE commentary SET run_type = ?, commentary_text = ?, batter_id = ?, bowler_id = ?, catcher_id = ?, date_time = ? WHERE fixture_id = ? AND over_count = ? AND ball = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, runType != null ? runType : null);
            pstmt.setString(2, commentaryText != null ? commentaryText : null);
            pstmt.setObject(3, batterId > 0 ? batterId : null, java.sql.Types.INTEGER);  // Set batter_id if provided, otherwise NULL
            pstmt.setObject(4, bowlerId > 0 ? bowlerId : null, java.sql.Types.INTEGER);  // Set bowler_id if provided, otherwise NULL
            pstmt.setObject(5, catcherId > 0 ? catcherId : null, java.sql.Types.INTEGER);  // Set catcher_id if provided, otherwise NULL
            pstmt.setString(6, dateTimeParam); 
            pstmt.setInt(7, fixtureId);
            pstmt.setInt(8, overCount);
            pstmt.setInt(9, ball);

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write("Successfully updated the record " + fixtureId);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "No records found " + fixtureId);
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error: " + e.getMessage());
        }
    }

    

}
