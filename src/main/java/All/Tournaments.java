package All;


import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;


@WebServlet("/all_tours")
public class Tournaments extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String DB_URL = "jdbc:mysql://localhost:3306/CricketBuzz";
    private static final String USER = "root";
    private static final String PASS = "";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String sql = "SELECT * FROM tournament";
        JSONArray tournamentsArray = new JSONArray();

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                JSONObject tournamentObject = new JSONObject();
                tournamentObject.put("tour_id", rs.getInt("tour_id"));
                tournamentObject.put("name", rs.getString("name"));
                tournamentObject.put("start_date", rs.getDate("start_date").toString());
                tournamentObject.put("end_date", rs.getDate("end_date").toString());
                tournamentObject.put("match_category", rs.getString("match_category"));
                tournamentObject.put("season", rs.getInt("season"));
                tournamentsArray.put(tournamentObject);
            }

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            PrintWriter out = response.getWriter();
            out.print(tournamentsArray.toString());
            out.flush();

        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error: " + e.getMessage());
        }
    }
    
}
