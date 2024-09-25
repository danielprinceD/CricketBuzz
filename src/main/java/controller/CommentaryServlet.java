package controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.sql.SQLException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.gson.Gson;
import model.*;
import com.google.gson.reflect.TypeToken;

import repository.*;

public class CommentaryServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private CommentaryDAO commentaryDAO;
    
    @Override
    public void init() {
    	commentaryDAO = new CommentaryDAO();
    }
    
    
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException  {
        
        String fixtureIdParam = request.getParameter("fixture_id");
        
        if (fixtureIdParam == null)
        {
        	Extra.sendError(response, response.getWriter(), "Fixture ID is required");
        }
			try {
				commentaryDAO.getAllCommentaries( request,response);
			} catch (IOException e) {
				Extra.sendError(response, response.getWriter(), e.getMessage());
				e.printStackTrace();
			} catch (SQLException e) {
				Extra.sendError(response, response.getWriter(), e.getMessage());
			}
        	return;
        
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
        
        Type listType = new TypeToken<List<CommentaryVO>>() {}.getType();
        List<CommentaryVO> commentaryList = new Gson().fromJson(jsonString.toString(), listType);
        
        commentaryDAO.insert(request, response, out, commentaryList);
    }

    
    
    
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String fixtureIdParam = request.getParameter("fixtureId");
        String commentaryParam = request.getParameter("commentaryId");
        if (commentaryParam != null) {
        	commentaryDAO.deleteByCommentaryId(response , commentaryParam);
            return;
        }
        else if(fixtureIdParam != null) {
         	commentaryDAO.deleteByFixtureId(response , fixtureIdParam);
        }
        else {
        	Extra.sendError(response, response.getWriter() , "Missing Parameters");
        }     
    }

    
//    private void updateOneCommentary(HttpServletRequest request, HttpServletResponse response, PrintWriter out, String commentaryId) 
//            throws IOException {
//        
//        StringBuilder jsonString = new StringBuilder();
//        BufferedReader reader = request.getReader();
//        String line;
//        
//        while ((line = reader.readLine()) != null) {
//            jsonString.append(line);
//        }
//        
//        CommentaryModel commentaryModel = new Gson().fromJson(jsonString.toString(), CommentaryModel.class);
//        
//        String sql = "UPDATE commentary SET run_type = ?, commentary_text = ?, batter_id = ?, bowler_id = ?, catcher_id = ?, date_time = ?, fixture_id = ? , over_count = ? , ball = ? "
//                   + " WHERE commentary_id = ?";
//        
//        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
//             PreparedStatement pstmt = conn.prepareStatement(sql)) {
//
//            pstmt.setString(1, commentaryModel.getRunType() != null ? commentaryModel.getRunType() : null);
//            pstmt.setString(2, commentaryModel.getCommentaryText() != null ? commentaryModel.getCommentaryText() : null);
//            pstmt.setObject(3, commentaryModel.getBatterId() , java.sql.Types.INTEGER);
//            pstmt.setObject(4, commentaryModel.getBowlerId(), java.sql.Types.INTEGER);
//            pstmt.setObject(5, commentaryModel.getCatcherId(), java.sql.Types.INTEGER);
//            pstmt.setObject(6, commentaryModel.getDateTime() != null ? commentaryModel.getDateTime() : null, java.sql.Types.TIMESTAMP);
//            pstmt.setInt(7, commentaryModel.getFixtureId());
//            pstmt.setInt(8, commentaryModel.getOverCount());
//            pstmt.setInt(9, commentaryModel.getBall());
//            pstmt.setInt(10 , Integer.parseInt(commentaryId));
//
//            int affectedRows = pstmt.executeUpdate();
//
//            if (affectedRows > 0) {
//                response.setStatus(HttpServletResponse.SC_OK);
//                out.write("Successfully updated the commentary record for fixture_id: " + commentaryModel.getFixtureId());
//            } else {
//                response.sendError(HttpServletResponse.SC_NOT_FOUND, "No records found for fixture_id: " + commentaryModel.getFixtureId());
//            }
//
//        } catch (SQLException e) {
//            e.printStackTrace();
//            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error: " + e.getMessage());
//        }
//    }

    
//    @Override
////    protected void doPut(HttpServletRequest request, HttpServletResponse response)
////            throws ServletException, IOException {
////
////    	String infoString = request.getPathInfo();
////    	String[] pathStrings = infoString != null ? infoString.split("/") : null;
////    	PrintWriter out = response.getWriter();
////    	if(pathStrings == null || pathStrings.length <= 1)
////    		return;
////    	else if(pathStrings.length >= 2) {
//////    		updateOneCommentary(request , response , out , pathStrings[1]);
////    		return;
////    	}
////    	
////    	
////    }

    

}
