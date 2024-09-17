package Tournament;

import java.io.IOException;
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

@WebServlet("/over_summary")
public class Over_Summary extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private static final String DB_URL = "jdbc:mysql://localhost:3306/CricketBuzz";
    private static final String USER = "root";
    private static final String PASS = "";
	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
	        throws ServletException, IOException {

	    String fixtureIdParam = request.getParameter("fixture_id");
	    String overCountParam = request.getParameter("over_count");
	    String runParam = request.getParameter("run");
	    String wktParam = request.getParameter("wkt");

	    if (fixtureIdParam == null || overCountParam == null || runParam == null || wktParam == null) {
	        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing parameters.");
	        return;
	    }

	    int fixtureId, overCount, run, wkt;
	    try {
	        fixtureId = Integer.parseInt(fixtureIdParam);
	        overCount = Integer.parseInt(overCountParam);
	        run = Integer.parseInt(runParam);
	        wkt = Integer.parseInt(wktParam);
	    } catch (NumberFormatException e) {
	        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid parameter format.");
	        return;
	    }

	    String sql = "INSERT INTO over_summary (fixture_id, over_count, run, wkt) VALUES (?, ?, ?, ?)";

	    try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
	         PreparedStatement pstmt = conn.prepareStatement(sql)) {

	        pstmt.setInt(1, fixtureId);
	        pstmt.setInt(2, overCount);
	        pstmt.setInt(3, run);
	        pstmt.setInt(4, wkt);
	        pstmt.executeUpdate();

	        response.setStatus(HttpServletResponse.SC_CREATED);
	        response.getWriter().write("Record created successfully.");

	    } catch (SQLException e) {
	        e.printStackTrace();
	        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error.");
	    }
	}
	
	
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
	        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid fixture_id format.");
	        return;
	    }

	    String sql = "SELECT * FROM over_summary WHERE fixture_id = ?";

	    try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
	         PreparedStatement pstmt = conn.prepareStatement(sql)) {

	        pstmt.setInt(1, fixtureId);
	        ResultSet rs = pstmt.executeQuery();

	        JSONArray jsonArray = new JSONArray();

	        while (rs.next()) {
	            JSONObject jsonObject = new JSONObject();
	            jsonObject.put("fixture_id", rs.getInt("fixture_id"));
	            jsonObject.put("over_count", rs.getInt("over_count"));
	            jsonObject.put("run", rs.getInt("run"));
	            jsonObject.put("wkt", rs.getInt("wkt"));

	            jsonArray.put(jsonObject);
	        }

	        response.setContentType("application/json");
	        response.setCharacterEncoding("UTF-8");
	        response.getWriter().print(jsonArray.toString());

	    } catch (SQLException e) {
	        e.printStackTrace();
	        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error.");
	    }
	}
	
	
	
	@Override
	protected void doPut(HttpServletRequest request, HttpServletResponse response)
	        throws ServletException, IOException {

	    String fixtureIdParam = request.getParameter("fixture_id");
	    String overCountParam = request.getParameter("over_count");
	    String runParam = request.getParameter("run");
	    String wktParam = request.getParameter("wkt");

	    if (fixtureIdParam == null || overCountParam == null || runParam == null || wktParam == null) {
	        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing parameters.");
	        return;
	    }

	    int fixtureId, overCount, run, wkt;
	    try {
	        fixtureId = Integer.parseInt(fixtureIdParam);
	        overCount = Integer.parseInt(overCountParam);
	        run = Integer.parseInt(runParam);
	        wkt = Integer.parseInt(wktParam);
	    } catch (NumberFormatException e) {
	        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid parameter format.");
	        return;
	    }

	    String sql = "UPDATE over_summary SET run = ?, wkt = ? WHERE fixture_id = ? AND over_count = ?";

	    try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
	         PreparedStatement pstmt = conn.prepareStatement(sql)) {

	        pstmt.setInt(1, run);
	        pstmt.setInt(2, wkt);
	        pstmt.setInt(3, fixtureId);
	        pstmt.setInt(4, overCount);

	        int affectedRows = pstmt.executeUpdate();

	        if (affectedRows > 0) {
	            response.setStatus(HttpServletResponse.SC_OK);
	            response.getWriter().write("Record updated successfully.");
	        } else {
	            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Record not found.");
	        }

	    } catch (SQLException e) {
	        e.printStackTrace();
	        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error.");
	    }	    
	    
	}


	@Override
	protected void doDelete(HttpServletRequest request, HttpServletResponse response)
	        throws ServletException, IOException {

	    String fixtureIdParam = request.getParameter("fixture_id");
	    String overParmString = request.getParameter("over_count");

	    if (fixtureIdParam == null || overParmString == null) {
	        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing parameter.");
	        return;
	    }

	    int fixtureId , overId;
	    try {
	        fixtureId = Integer.parseInt(fixtureIdParam);
	        overId = Integer.parseInt(overParmString);
	        
	    } catch (NumberFormatException e) {
	        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid format.");
	        return;
	    }

	    String sql = "DELETE FROM over_summary WHERE fixture_id = ? AND over_count = ?";

	    try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
	         PreparedStatement pstmt = conn.prepareStatement(sql)) {

	        pstmt.setInt(1, fixtureId);
	        pstmt.setInt(2, overId);
	        int affectedRows = pstmt.executeUpdate();

	        if (affectedRows > 0) {
	            response.setStatus(HttpServletResponse.SC_OK);
	            response.getWriter().write("Record deleted successfully.");
	        } else {
	            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Record not found.");
	        }

	    } catch (SQLException e) {
	        e.printStackTrace();
	        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error.");
	    }
	}



}
