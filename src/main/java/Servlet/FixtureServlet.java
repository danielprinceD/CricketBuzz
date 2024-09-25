package Servlet;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.sql.SQLException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import DAO.FixtureDAO;
import Model.FixtureVO;

public class FixtureServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
    FixtureDAO fixtureDAO;
    
    @Override
    public void init() {
    	fixtureDAO = new FixtureDAO();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        String tourIdParam = request.getParameter("tourId");
        String fixtureIdParam = request.getParameter("fixtureId");

        if (tourIdParam == null && fixtureIdParam == null) {
            Extra.sendError(response, out, "Tournament ID / Fixture ID is required");
            return;
        }

        Integer tourId = (tourIdParam != null && !tourIdParam.isEmpty()) ? Integer.parseInt(tourIdParam) : null;
        Integer fixtureId = (fixtureIdParam != null && !fixtureIdParam.isEmpty()) ? Integer.parseInt(fixtureIdParam) : null;

        try {
        	
			List<FixtureVO> fixtureVO = fixtureDAO.getFixturesByTour(tourId, fixtureId);
			if(fixtureVO == null)
			{
				Extra.sendError(response, out, "No Data");
				return;
			}
			
			out.print(new Gson().toJson(fixtureVO));
			
		} catch (SQLException e) {
			Extra.sendError(response, out, e.getMessage() );
			e.printStackTrace();
		}
        catch (Exception e) {
        	Extra.sendError(response, out, e.getMessage());
		}
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {

    	
    	
    	StringBuilder jsonString = new StringBuilder();
        BufferedReader reader = request.getReader();
        String line;
        
        while((line = reader.readLine()) != null)
        		jsonString.append(line);
        
        PrintWriter out = response.getWriter();
        java.lang.reflect.Type fixtureListType = new TypeToken<List<FixtureVO>>() {}.getType();
        List<FixtureVO> fixtureModelList = new Gson().fromJson( jsonString.toString() , fixtureListType );
        
        String pathInfoString = request.getPathInfo();		
        String[] pathArray = pathInfoString != null ?  pathInfoString.split("/") : null;
        
        if ((pathArray == null || pathArray.length <= 1)) {
             try {
				fixtureDAO.addManyFixture(response, out, fixtureModelList, request.getParameter("tourId"), request.getMethod());
			} catch (ServletException | SQLException e) {
				
				e.printStackTrace();
			}
        } else {
            Extra.sendError(response, out, "Invalid tour_id or missing path parameters.");
        }
    }
    
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
    	
		
		PrintWriter out = response.getWriter();
    	
		try {
			
			fixtureDAO.deleteAllFixture(request ,response , out);
		} catch (Exception e) {
			Extra.sendError(response, out, e.getMessage());
		} 
		
    }
    
    protected void doPut(HttpServletRequest request, HttpServletResponse response)throws IOException , ServletException {
    	doPost(request, response);
    }
    
}

