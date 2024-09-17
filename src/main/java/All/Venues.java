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



@WebServlet("/all_venues")
public class Venues extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
	private static final String DB_URL = "jdbc:mysql://localhost:3306/CricketBuzz";
    private static final String USER = "root";
    private static final String PASS = "";
    
    
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String sql = "SELECT * FROM venue";

        JSONArray venuesArray = new JSONArray();

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                JSONObject venueObject = new JSONObject();
                venueObject.put("venue_id", rs.getInt("venue_id"));
                venueObject.put("stadium", rs.getString("stadium"));
                venueObject.put("location", rs.getString("location"));
                venueObject.put("pitch_condition", rs.getString("pitch_condition"));
                venueObject.put("description", rs.getString("description"));
                venueObject.put("capacity", rs.getLong("capacity"));
                venueObject.put("curator", rs.getString("curator"));

                venuesArray.put(venueObject);
            }

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            PrintWriter out = response.getWriter();
            out.print(venuesArray.toString());
            out.flush();

        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error: " + e.getMessage());
        }
    }
    

}
