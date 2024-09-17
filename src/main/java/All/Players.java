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

@WebServlet("/all_players")
public class Players extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private static final String DB_URL = "jdbc:mysql://localhost:3306/CricketBuzz";
    private static final String USER = "root";
    private static final String PASS = "";
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
	        throws ServletException, IOException {

	    String sql = "SELECT * FROM player";

	    JSONArray playersArray = new JSONArray();

	    try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
	         Statement stmt = conn.createStatement();
	         ResultSet rs = stmt.executeQuery(sql); ) {

	        while (rs.next()) {
	            JSONObject playerObject = new JSONObject();
	            playerObject.put("id", rs.getInt("id"));
	            playerObject.put("name", rs.getString("name"));
	            playerObject.put("role", rs.getString("role"));
	            playerObject.put("address", rs.getString("address"));
	            playerObject.put("gender", rs.getString("gender"));
	            playerObject.put("rating", rs.getInt("rating"));
	            playerObject.put("batting_style", rs.getString("batting_style"));
	            playerObject.put("bowling_style", rs.getString("bowling_style"));

	            playersArray.put(playerObject);
	        }

	        response.setContentType("application/json");
	        response.setCharacterEncoding("UTF-8");

	        PrintWriter out = response.getWriter();
	        out.print(playersArray.toString());
	        out.flush();

	    } catch (SQLException e) {
	        e.printStackTrace();
	        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error: " + e.getMessage());
	    }
	}


}
