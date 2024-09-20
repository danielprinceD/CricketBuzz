package Tournament;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
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

        StringBuilder jsonBuffer = new StringBuilder();
        String line;
        try (BufferedReader reader = request.getReader()) {
            while ((line = reader.readLine()) != null) {
                jsonBuffer.append(line);
            }
        }

        JSONArray jsonArray;
        try {
            jsonArray = new JSONArray(jsonBuffer.toString());
        } catch (JSONException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON format.");
            return;
        }

        String sql = "INSERT INTO over_summary (fixture_id, over_count, run, wkt) VALUES (?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            int insertedRecords = 0;
            Integer fixtureId = Integer.parseInt( request.getParameter("fixture_id") );

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                int overCount = jsonObject.getInt("over_count");
                int run = jsonObject.getInt("run");
                int wkt = jsonObject.getInt("wkt");

                pstmt.setInt(1, fixtureId);
                pstmt.setInt(2, overCount);
                pstmt.setInt(3, run);
                pstmt.setInt(4, wkt);
                insertedRecords += pstmt.executeUpdate();
            }

            if (insertedRecords > 0) {
                response.setStatus(HttpServletResponse.SC_CREATED);
                response.getWriter().write(insertedRecords + " record(s) created successfully.");
            } else {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "No records inserted.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error.");
        } catch (JSONException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON format.");
        }
    }

	
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
	        throws ServletException, IOException {

	    StringBuilder sql = new StringBuilder("SELECT * FROM over_summary WHERE 1=1");
	    List<Object> parameters = new ArrayList<>();

	    String fixtureIdParam = request.getParameter("fixture_id");
	    String overCountParam = request.getParameter("over_count");
	    String runParam = request.getParameter("run");
	    String wktParam = request.getParameter("wkt");

	    if (fixtureIdParam != null) {
	        sql.append(" AND fixture_id = ?");
	        parameters.add(Integer.parseInt(fixtureIdParam));
	    }
	    if (overCountParam != null) {
	        sql.append(" AND over_count = ?");
	        parameters.add(Integer.parseInt(overCountParam));
	    }
	    if (runParam != null) {
	        sql.append(" AND run = ?");
	        parameters.add(Integer.parseInt(runParam));
	    }
	    if (wktParam != null) {
	        sql.append(" AND wkt = ?");
	        parameters.add(Integer.parseInt(wktParam));
	    }

	    try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
	         PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

	        for (int i = 0; i < parameters.size(); i++) {
	            pstmt.setObject(i + 1, parameters.get(i));
	        }

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

	    StringBuilder jsonBuffer = new StringBuilder();
	    String line;
	    try (BufferedReader reader = request.getReader()) {
	        while ((line = reader.readLine()) != null) {
	            jsonBuffer.append(line);
	        }
	    }

	    JSONArray jsonArray;
	    try {
	        jsonArray = new JSONArray(jsonBuffer.toString());
	    } catch (JSONException e) {
	        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON format.");
	        return;
	    }

	    String sql = "UPDATE over_summary SET run = ?, wkt = ? WHERE fixture_id = ? AND over_count = ?";
	    Integer fixtureId = Integer.parseInt(request.getParameter("fixture_id"));
	    
	    try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
	         PreparedStatement pstmt = conn.prepareStatement(sql)) {

	        int updatedRecords = 0;

	        for (int i = 0; i < jsonArray.length(); i++) {
	            JSONObject jsonObject = jsonArray.getJSONObject(i);
	           
	            int overCount = jsonObject.getInt("over_count");
	            int run = jsonObject.getInt("run");
	            int wkt = jsonObject.getInt("wkt");

	            pstmt.setInt(1, run);
	            pstmt.setInt(2, wkt);
	            pstmt.setInt(3, fixtureId);
	            pstmt.setInt(4, overCount);

	            updatedRecords += pstmt.executeUpdate();
	        }

	        if (updatedRecords > 0) {
	            response.setStatus(HttpServletResponse.SC_OK);
	            response.getWriter().write(updatedRecords + " record(s) updated successfully.");
	        } else {
	            response.sendError(HttpServletResponse.SC_NOT_FOUND, "No records found to update.");
	        }

	    } catch (SQLException e) {
	        e.printStackTrace();
	        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error.");
	    } catch (JSONException e) {
	        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON format.");
	    }
	}



	@Override
	protected void doDelete(HttpServletRequest request, HttpServletResponse response)
	        throws ServletException, IOException {

	    StringBuilder sql = new StringBuilder("DELETE FROM over_summary WHERE 1=1");
	    List<Object> parameters = new ArrayList<>();

	    String fixtureIdParam = request.getParameter("fixture_id");
	    String overCountParam = request.getParameter("over_count");

	    if (fixtureIdParam != null) {
	        sql.append(" AND fixture_id = ?");
	        parameters.add(Integer.parseInt(fixtureIdParam));
	    }
	    if (overCountParam != null) {
	        sql.append(" AND over_count = ?");
	        parameters.add(Integer.parseInt(overCountParam));
	    }

	    if (parameters.isEmpty()) {
	        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No parameters provided for deletion.");
	        return;
	    }

	    try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
	         PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

	        for (int i = 0; i < parameters.size(); i++) {
	            pstmt.setObject(i + 1, parameters.get(i));
	        }

	        int affectedRows = pstmt.executeUpdate();

	        if (affectedRows > 0) {
	            response.setStatus(HttpServletResponse.SC_OK);
	            response.getWriter().write("Record(s) deleted successfully.");
	        } else {
	            response.sendError(HttpServletResponse.SC_NOT_FOUND, "No records found to delete.");
	        }

	    } catch (SQLException e) {
	        e.printStackTrace();
	        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error.");
	    } catch (NumberFormatException e) {
	        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid parameter format.");
	    }
	}




}
