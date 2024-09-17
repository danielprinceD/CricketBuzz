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


@WebServlet("/all_teams")
public class Teams extends HttpServlet {
	private static final long serialVersionUID = 1L;
   
	private static final String DB_URL = "jdbc:mysql://localhost:3306/CricketBuzz";
    private static final String USER = "root";
    private static final String PASS = "";
    
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String sql = "SELECT t.team_id, t.name AS team_name, t.category, " +
                     "p1.name AS captain_name, p2.name AS vice_captain_name, p3.name AS wicket_keeper_name " +
                     "FROM team t " +
                     "LEFT JOIN player p1 ON t.captain_id = p1.id " +
                     "LEFT JOIN player p2 ON t.vice_captain_id = p2.id " +
                     "LEFT JOIN player p3 ON t.wicket_keeper_id = p3.id";

        JSONArray teamsArray = new JSONArray();

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                JSONObject teamObject = new JSONObject();
                teamObject.put("team_id", rs.getInt("team_id"));
                teamObject.put("team_name", rs.getString("team_name"));
                teamObject.put("category", rs.getString("category"));
                teamObject.put("captain_name", rs.getString("captain_name"));
                teamObject.put("vice_captain_name", rs.getString("vice_captain_name"));
                teamObject.put("wicket_keeper_name", rs.getString("wicket_keeper_name"));

                teamsArray.put(teamObject);
            }

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            PrintWriter out = response.getWriter();
            out.print(teamsArray.toString());
            out.flush();

        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error: " + e.getMessage());
        }
    }

}
