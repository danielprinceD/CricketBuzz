package Tournament;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Type;
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
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import Model.CommentaryModel;
import Model.TournamentModel;
import Team.Extra;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

@WebServlet("/commentaries/*")
public class Commentary extends HttpServlet {
	private static final long serialVersionUID = 1L;
  
	private static final String DB_URL = "jdbc:mysql://localhost:3306/CricketBuzz";
    private static final String USER = "root";
    private static final String PASS = "";
    
    public void getAllCommentaries(HttpServletResponse response) throws IOException {
    	
    	String sql = "SELECT c.fixture_id, c.over_count, c.ball, c.run_type, " +
                "c.commentary_text, " +
                "batter.id AS batter_id, batter.name AS batter_name, batter.role AS batter_role, batter.rating AS batter_rating, " +
                "bowler.id AS bowler_id, bowler.name AS bowler_name, bowler.role AS bowler_role, bowler.rating AS bowler_rating, " +
                "catcher.id AS catcher_id, catcher.name AS catcher_name, catcher.role AS catcher_role, catcher.rating AS catcher_rating, " +
                "c.date_time " +
                "FROM commentary c " +
                "LEFT JOIN player batter ON c.batter_id = batter.id " +
                "LEFT JOIN player bowler ON c.bowler_id = bowler.id " +
                "LEFT JOIN player catcher ON c.catcher_id = catcher.id";   
   
    	JSONArray resultArray = new JSONArray();

   try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
        PreparedStatement pstmt = conn.prepareStatement(sql)) {

       ResultSet rs = pstmt.executeQuery();

       while (rs.next()) {
           JSONObject jsonObject = new JSONObject();
           jsonObject.put("fixture_id", rs.getInt("fixture_id"));
           jsonObject.put("over_count", rs.getInt("over_count"));
           jsonObject.put("ball", rs.getInt("ball"));
           jsonObject.put("run_type", rs.getString("run_type"));
           jsonObject.put("commentary_text", rs.getString("commentary_text"));
           jsonObject.put("date_time", rs.getTimestamp("date_time"));

           JSONObject batter = new JSONObject();
           batter.put("id", rs.getInt("batter_id"));
           batter.put("name", rs.getString("batter_name"));
           batter.put("role", rs.getString("batter_role"));
           batter.put("rating", rs.getInt("batter_rating"));
           jsonObject.put("batter", batter);

           JSONObject bowler = new JSONObject();
           bowler.put("id", rs.getInt("bowler_id"));
           bowler.put("name", rs.getString("bowler_name"));
           bowler.put("role", rs.getString("bowler_role"));
           bowler.put("rating", rs.getInt("bowler_rating"));
           jsonObject.put("bowler", bowler);

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
       if(resultArray.length() > 0)
       out.print(resultArray.toString());
       else out.print("No Data Found");

   } catch (Exception e) {
       e.printStackTrace();
       response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error: " + e.getMessage());
   }
	}
    
    public void getCommentaryByFixture(HttpServletResponse response , String fixtureIdParam ) throws IOException {
    
    	int fixtureId;
    	try {
            fixtureId = Integer.parseInt(fixtureIdParam);
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid fixture_id parameter.");
            return;
        }

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

                JSONObject batter = new JSONObject();
                batter.put("id", rs.getInt("batter_id"));
                batter.put("name", rs.getString("batter_name"));
                batter.put("role", rs.getString("batter_role"));
                batter.put("rating", rs.getInt("batter_rating"));
                jsonObject.put("batter", batter);

                JSONObject bowler = new JSONObject();
                bowler.put("id", rs.getInt("bowler_id"));
                bowler.put("name", rs.getString("bowler_name"));
                bowler.put("role", rs.getString("bowler_role"));
                bowler.put("rating", rs.getInt("bowler_rating"));
                jsonObject.put("bowler", bowler);

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
            if(resultArray.length() > 0)
            out.print(resultArray.toString());
            else out.print("No Data Found");

        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error: " + e.getMessage());
        }
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String fixtureIdParam = request.getParameter("fixture_id");
        
        if (fixtureIdParam == null || fixtureIdParam.length() <= 1) {
            getAllCommentaries(response);
        	return;
        }
        else {
        	getCommentaryByFixture(response , fixtureIdParam);
        }

        int fixtureId;
        
    }


    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        StringBuilder jsonString = new StringBuilder();
        BufferedReader reader = request.getReader();
        String line;
        
        while ((line = reader.readLine()) != null) {
            jsonString.append(line);
        }

        PrintWriter out = response.getWriter();
        
        Type listType = new TypeToken<List<CommentaryModel>>() {}.getType();
        List<CommentaryModel> commentaryList = new Gson().fromJson(jsonString.toString(), listType);
        
        String sql = "INSERT INTO commentary (fixture_id, over_count, ball, run_type, commentary_text, batter_id, bowler_id, date_time, catcher_id) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

        	String fixtureId = request.getParameter("fixture_id");
        	
            for (CommentaryModel tourModel : commentaryList) {
                pstmt.setInt(1, Integer.parseInt(fixtureId));
                pstmt.setInt(2, tourModel.getOverCount());
                pstmt.setInt(3, tourModel.getBall());
                pstmt.setString(4, tourModel.getRunType());
                pstmt.setString(5, tourModel.getCommentaryText());
                pstmt.setObject(6, tourModel.getBatterId() , java.sql.Types.INTEGER);
                pstmt.setObject(7, tourModel.getBowlerId() , java.sql.Types.INTEGER);
                pstmt.setObject(8, tourModel.getDateTime() , java.sql.Types.DATE);
                pstmt.setObject(9, tourModel.getCatcherId(), java.sql.Types.INTEGER);
                
                pstmt.addBatch(); 
            }

            int[] rowsAffected = pstmt.executeBatch(); 

            if (rowsAffected.length > 0) {
                out.println("Commentary data inserted successfully.");
            } else {
                out.println("Failed to insert commentary data.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            Extra.sendError(response , out , "Database error: " + e.getMessage());
        }
    }

    
    private void deleteByFixtureId(HttpServletResponse response  , String fixtureIdParam) throws IOException {
        

        String sql = "DELETE FROM commentary WHERE fixture_id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, Integer.parseInt(fixtureIdParam));
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write("Deleted " + affectedRows + " rows.");
            } else {
            	Extra.sendError(response, response.getWriter(), "No Data Found for that Fixture ID");
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error: " + e.getMessage());
        }
    }
    
    private void deleteByCommentaryId(HttpServletResponse response , String commentaryId) throws IOException{
    	 String sql = "DELETE FROM commentary WHERE commentary_id = ?";

         try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
              PreparedStatement pstmt = conn.prepareStatement(sql)) {

             pstmt.setInt(1, Integer.parseInt(commentaryId));
             int affectedRows = pstmt.executeUpdate();

             if (affectedRows > 0) {
                 response.setStatus(HttpServletResponse.SC_OK);
                 response.getWriter().write("Deleted " + affectedRows + " rows.");
             } else {
            	 	Extra.sendError(response, response.getWriter(), "No Data Found for that Commentary ID");
             }

         } catch (Exception e) {
             e.printStackTrace();
             response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error: " + e.getMessage());
         }
    }
    
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String fixtureIdParam = request.getParameter("fixture_id");
        String commentaryParam = request.getParameter("commentary_id");
        if (commentaryParam != null) {
        	deleteByCommentaryId(response , commentaryParam);
            return;
        }
        else if(fixtureIdParam != null) {
        	deleteByFixtureId(response , fixtureIdParam);
        }
        else {
        	Extra.sendError(response, response.getWriter() , "Missing Parameters");
        }     
    }

    
    private void updateOneCommentary(HttpServletRequest request, HttpServletResponse response, PrintWriter out, String commentaryId) 
            throws IOException {
        
        StringBuilder jsonString = new StringBuilder();
        BufferedReader reader = request.getReader();
        String line;
        
        while ((line = reader.readLine()) != null) {
            jsonString.append(line);
        }
        
        CommentaryModel commentaryModel = new Gson().fromJson(jsonString.toString(), CommentaryModel.class);
        
        String sql = "UPDATE commentary SET run_type = ?, commentary_text = ?, batter_id = ?, bowler_id = ?, catcher_id = ?, date_time = ?, fixture_id = ? , over_count = ? , ball = ? "
                   + " WHERE commentary_id = ?";
        
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, commentaryModel.getRunType() != null ? commentaryModel.getRunType() : null);
            pstmt.setString(2, commentaryModel.getCommentaryText() != null ? commentaryModel.getCommentaryText() : null);
            pstmt.setObject(3, commentaryModel.getBatterId() , java.sql.Types.INTEGER);
            pstmt.setObject(4, commentaryModel.getBowlerId(), java.sql.Types.INTEGER);
            pstmt.setObject(5, commentaryModel.getCatcherId(), java.sql.Types.INTEGER);
            pstmt.setObject(6, commentaryModel.getDateTime() != null ? commentaryModel.getDateTime() : null, java.sql.Types.TIMESTAMP);
            pstmt.setInt(7, commentaryModel.getFixtureId());
            pstmt.setInt(8, commentaryModel.getOverCount());
            pstmt.setInt(9, commentaryModel.getBall());
            pstmt.setInt(10 , Integer.parseInt(commentaryId));

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                response.setStatus(HttpServletResponse.SC_OK);
                out.write("Successfully updated the commentary record for fixture_id: " + commentaryModel.getFixtureId());
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "No records found for fixture_id: " + commentaryModel.getFixtureId());
            }

        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error: " + e.getMessage());
        }
    }

    
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

    	String infoString = request.getPathInfo();
    	String[] pathStrings = infoString != null ? infoString.split("/") : null;
    	PrintWriter out = response.getWriter();
    	if(pathStrings == null || pathStrings.length <= 1)
    		return;
    	else if(pathStrings.length >= 2) {
    		updateOneCommentary(request , response , out , pathStrings[1]);
    		return;
    	}
    	
    	
    }

    

}
