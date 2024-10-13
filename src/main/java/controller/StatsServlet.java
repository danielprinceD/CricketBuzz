package controller;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.gson.Gson;
import model.PlayersStatsVO;
import repository.StatsDAO;
import utils.PathMatcherUtil;

public class StatsServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
    private static StatsDAO statsDAO = null;
    private final static String PLAYER_STATS = "/players";
    private final static String PLAYER_TEAM_STATS = "/team-players";
    private final static String TOP_PLAYER_BATTING = "/top-players/batter";
    private final static String TOP_PLAYER_BOWLING = "/top-players/bowler";
    private final static String PLAYER_MATCH_HISTORY_BY_ID = "/player-history/([0-9]+)";
    private final static Pattern PLAYER_MATCH_HISTORY_BY_ID_COMPILE = Pattern.compile(PLAYER_MATCH_HISTORY_BY_ID);
    
    static {
    	statsDAO = new StatsDAO();
    }
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String pathInfo = request.getPathInfo();
		
		
		response.setContentType("application/json");
		
		String tourIdString = request.getParameter("tourId");
		String yearString = request.getParameter("year");
		
		try {
			
			if(PathMatcherUtil.matchesPattern( pathInfo , PLAYER_STATS)) {
				
				List<PlayersStatsVO> playersStatsVOs = statsDAO.allPlayerStats();
				String jsonString = new Gson().toJson(playersStatsVOs);
				response.setHeader("Content-Disposition", "attachment;filename=\"player-stats.json\"");
				response.getWriter().print(jsonString);
				response.flushBuffer();
				return;
			}
			if(PathMatcherUtil.matchesPattern( pathInfo , PLAYER_TEAM_STATS)) {
				
				List<PlayersStatsVO> playersStatsVOs = statsDAO.allTeamPlayerStats();
				response.setHeader("Content-Disposition", "attachment;filename=\"team-players.json\"");
				String jsonString = new Gson().toJson(playersStatsVOs);
				response.getWriter().print(jsonString);
				return;
			}
			
			
			if(PathMatcherUtil.matchesPattern( pathInfo , TOP_PLAYER_BATTING)) {
				
				String jsonString = null;
				
				if(tourIdString != null) {
					Integer tourId = Integer.parseInt(tourIdString);
					PlayersStatsVO playersStatsVO = statsDAO.topPlayerInBattingByTourId(tourId);
					jsonString = new Gson().toJson(playersStatsVO);
				}
				else if(yearString != null) {
					Integer year = Integer.parseInt(yearString);
					PlayersStatsVO playersStatsVO = statsDAO.topPlayerInBattingByYear(year);
					jsonString = new Gson().toJson(playersStatsVO);
				}
				else {
					List<PlayersStatsVO> playersStatsVOs = statsDAO.topPlayersInBatting();
					jsonString = new Gson().toJson(playersStatsVOs);				
				}
				
				if(jsonString == null)
					Extra.sendError(response, response.getWriter() , "No Data Found");
				
				response.setHeader("Content-Disposition", "attachment;filename=\"top-player-batting.json\"");
				response.getWriter().print(jsonString);
				return;
			}
			
			
			if(PathMatcherUtil.matchesPattern( pathInfo , TOP_PLAYER_BOWLING)) {
				String jsonString = null;
				
				if(tourIdString != null) {
					Integer tourId = Integer.parseInt(tourIdString);
					PlayersStatsVO playersStatsVO = statsDAO.topPlayerInBowlingByTourId(tourId);
					jsonString = new Gson().toJson(playersStatsVO);
				}
				else if(yearString != null) {
					Integer year = Integer.parseInt(yearString);
					PlayersStatsVO playersStatsVO = statsDAO.topPlayerInBowlingByYear(year);
					jsonString = new Gson().toJson(playersStatsVO);
				}
				else {
					List<PlayersStatsVO> playersStatsVOs = statsDAO.topPlayersInBowling();
					jsonString = new Gson().toJson(playersStatsVOs);
					
				}
				if(jsonString == null)
					Extra.sendError(response, response.getWriter() , "No Data Found");
				response.setHeader("Content-Disposition", "attachment;filename=\"top-player-bowling.json\"");
				response.getWriter().print(jsonString);
				return;
			}	
			
			if(PathMatcherUtil.matchesPattern(pathInfo, PLAYER_MATCH_HISTORY_BY_ID)) {
				String json  = null;
				Matcher matcher = PLAYER_MATCH_HISTORY_BY_ID_COMPILE.matcher(pathInfo);
				if(matcher.find()) {
					Integer playerId = Integer.parseInt(matcher.group(1));
					List<PlayersStatsVO> playersStatsVOs = statsDAO.playerHistoryById(playerId);
					json = new Gson().toJson(playersStatsVOs);
				}
				if(json == null)
					Extra.sendError(response, response.getWriter() , "No Data Found");
				response.setHeader("Content-Disposition", "attachment;filename=\"match-history-by-player-id.json\"");
				response.getWriter().print(json);
				return;
			}
			
			Extra.sendError(response, response.getWriter() , "InValid Path");
			
		}catch (Exception e) {
			e.printStackTrace();
			Extra.sendError(response, response.getWriter(), e.getMessage());
		}
		
		
	}


}
