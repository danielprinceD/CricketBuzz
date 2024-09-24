package Team;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import Model.PlayerModel;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class Player extends HttpServlet {
	
	private static final String DB_URL = "jdbc:mysql://localhost:3306/CricketBuzz";
	private static final String USER = "root";
	private static final String PASS = "";
	
	protected void addData(JSONObject playerObject , ResultSet rs) {
		
		try {
			
		playerObject.put("id", rs.getInt("id"));
        playerObject.put("name", rs.getString("name"));
        playerObject.put("role", rs.getString("role"));
        playerObject.put("address", rs.getString("address"));
        playerObject.put("gender", rs.getString("gender"));
        playerObject.put("rating", rs.getInt("rating"));
        playerObject.put("batting_style", rs.getString("batting_style"));
        playerObject.put("bowling_style", rs.getString("bowling_style"));
        
		}
		catch (Exception e) {
			e.printStackTrace();
			
		}
		
	}
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException , IOException {
		
		response.setContentType("application/json");
		
		String pathInfoString = request.getPathInfo();
		String[] pathArray = pathInfoString != null ? pathInfoString.split("/") : null;
		PrintWriter out = response.getWriter();

		    
	    StringBuilder sql = new StringBuilder("SELECT P.id , P.role , P.gender , P.bowling_style , P.name , P.rating , P.batting_style , A.address_id , A.street , A.city , A.state , A.door_num , A.nationality  FROM player as P JOIN address AS A ON P.address_id = A.address_id"); 
	   
	    List<Object> filters = new ArrayList<>();

	    String role = request.getParameter("role");
	    String gender = request.getParameter("gender");
	    String bowlingStyle = request.getParameter("bowling_style");
	    String name = request.getParameter("name");
	    String rating = request.getParameter("rating");
	    String battingStyle = request.getParameter("batting_style");
	    
	    
	    boolean whereAdded = false;

	    if (pathArray != null && pathArray.length >= 2) {
	        if (!whereAdded) {
	            sql.append(" WHERE id = ?");
	            whereAdded = true;
	        } else {
	            sql.append(" AND id = ?");
	        }
	        filters.add(Integer.parseInt(pathArray[1]));
	    }

	    if (role != null) {
	        if (!whereAdded) {
	            sql.append(" WHERE role = ?");
	            whereAdded = true;
	        } else {
	            sql.append(" AND role = ?");
	        }
	        filters.add(role);
	    }

	    

	    if (gender != null) {
	        if (!whereAdded) {
	            sql.append(" WHERE gender = ?");
	            whereAdded = true;
	        } else {
	            sql.append(" AND gender = ?");
	        }
	        filters.add(gender);
	    }

	    if (bowlingStyle != null) {
	        if (!whereAdded) {
	            sql.append(" WHERE bowling_style = ?");
	            whereAdded = true;
	        } else {
	            sql.append(" AND bowling_style = ?");
	        }
	        filters.add(bowlingStyle);
	    }

	    if (name != null) {
	        if (!whereAdded) {
	            sql.append(" WHERE name = ?");
	            whereAdded = true;
	        } else {
	            sql.append(" AND name = ?");
	        }
	        filters.add(name);
	    }

	    if (rating != null) {
	        if (!whereAdded) {
	            sql.append(" WHERE rating = ?");
	            whereAdded = true;
	        } else {
	            sql.append(" AND rating = ?");
	        }
	        filters.add(rating);
	    }

	    if (battingStyle != null) {
	        if (!whereAdded) {
	            sql.append(" WHERE batting_style = ?");
	            whereAdded = true;
	        } else {
	            sql.append(" AND batting_style = ?");
	        }
	        filters.add(battingStyle);
	    }
	    
	    

	    try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
	         PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

	        for (int i = 0; i < filters.size(); i++) {
	            pstmt.setObject(i + 1, filters.get(i));
	        }

	        ResultSet rs = pstmt.executeQuery();
	        JSONArray playersArray = new JSONArray();

	        while (rs.next()) {
	            JSONObject playerObject = new JSONObject();
	            playerObject.put("id", rs.getInt("id"));
	            playerObject.put("role", rs.getString("role"));
	            
	            JSONObject addressObject = new JSONObject();
	            
	            addressObject.put("address_id", rs.getString("address_id"));
	            addressObject.put("door_num", rs.getString("door_num"));
	            addressObject.put("city", rs.getString("city"));
	            addressObject.put("state", rs.getString("state"));
	            addressObject.put("nationality", rs.getString("nationality"));
	            addressObject.put("street", rs.getString("street"));
	            addressObject.put("address_id", rs.getInt("address_id"));
	            
	            playerObject.put("address", addressObject);
	            
	            playerObject.put("gender", rs.getString("gender"));
	            playerObject.put("bowling_style", rs.getString("bowling_style"));
	            playerObject.put("name", rs.getString("name"));
	            playerObject.put("rating", rs.getInt("rating"));
	            playerObject.put("batting_style", rs.getString("batting_style"));
	            playersArray.put(playerObject);
	        }

	        	out.print(playersArray.toString());
	        
	        out.flush();

	    } catch (SQLException e) {
	        e.printStackTrace();
	        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error: " + e.getMessage());
	    	}
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

	    java.lang.reflect.Type listType = new TypeToken<List<PlayerModel>>() {}.getType();
	    
	    List<PlayerModel> playerModels = new Gson().fromJson(jsonString.toString(), listType);

	    if (playerModels == null || playerModels.isEmpty()) {
	        Extra.sendError(response, out, "No player data provided.");
	        return;
	    }
	    
	    

	    Connection conn = null;
	    PreparedStatement pstmt = null;
	    String sql = null;
	    Boolean isPut = request.getMethod().equalsIgnoreCase("put");
	    try {
	        conn = DriverManager.getConnection(DB_URL, USER, PASS);
	        conn.setAutoCommit(false);

	        for (PlayerModel playerModel : playerModels) {
	            
	            if (playerModel.isValid()) {

	                
	                if (!isPut) {
	                    sql = "INSERT INTO player (name, role, address_id, gender, rating, batting_style, bowling_style) VALUES (?, ?, ?, ?, ?, ?, ?)";
	                } else {
	                    sql = "UPDATE player SET name = ?, role = ?, address_id = ?, gender = ?, rating = ?, batting_style = ?, bowling_style = ? WHERE id = ?";
	                }

	                pstmt = conn.prepareStatement(sql);
	                pstmt.setString(1, playerModel.getName());
	                pstmt.setString(2, playerModel.getRole());

	                String insertAddress;
	                if (isPut) {
	                    insertAddress = "UPDATE address SET door_num = ?, street = ?, city = ?, state = ?, nationality = ? WHERE address_id = ?";
	                } else {
	                    insertAddress = "INSERT INTO address (door_num, street, city, state, nationality) VALUES (?, ?, ?, ?, ?)";
	                }

	                try (PreparedStatement addressPstmt = conn.prepareStatement(insertAddress, Statement.RETURN_GENERATED_KEYS)) {
	                    
	                    addressPstmt.setString(1, playerModel.getAddress().door_num);
	                    addressPstmt.setString(2, playerModel.getAddress().street);
	                    addressPstmt.setString(3, playerModel.getAddress().city);
	                    addressPstmt.setString(4, playerModel.getAddress().state);
	                    addressPstmt.setString(5, playerModel.getAddress().nationality);

	                    if (isPut) {
	                        addressPstmt.setInt(6, playerModel.getAddress().address_id);
	                    }

	                    int addressRowsAffected = addressPstmt.executeUpdate();

	                    if (addressRowsAffected > 0 && !isPut) {
	                        try (ResultSet generatedKeys = addressPstmt.getGeneratedKeys()) {
	                            if (generatedKeys.next()) {
	                                int addressId = generatedKeys.getInt(1);
	                                System.out.println(addressId);
	                                pstmt.setInt(3, addressId); 
	                            }
	                        }
	                    } 
	                }

	                if(isPut) {
	                	
	                	pstmt.setInt(3, playerModel.getAddress().address_id);
	                }
	              
	                pstmt.setString(4, playerModel.getGender());
	                pstmt.setDouble(5, playerModel.getRating());
	                pstmt.setString(6, playerModel.getBattingStyle());
	                pstmt.setString(7, playerModel.getBowlingStyle());
	                
	                
	              
	                if (isPut) {

                    	System.out.println(playerModel.getId());
	                    pstmt.setInt(8, playerModel.getId());
	                }

	                int rowsAffected = pstmt.executeUpdate();

	                if (rowsAffected <= 0) {
	                    Extra.sendError(response, out, "Failed to process player data.");
	                    conn.rollback();
	                    return;
	                }

	            } else {
	                Extra.sendError(response, out, "Invalid player data.");
	                conn.rollback();
	                return;
	            }
	        }

	        conn.commit();
	        Extra.sendSuccess(response, out, "Players processed successfully.");

	    } catch (NumberFormatException e) {
	        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
	        out.println("Invalid player_id format.");
	    } catch (SQLException e) {
	        if (conn != null) {
	            try {
	                conn.rollback();
	            } catch (SQLException rollbackEx) {
	                rollbackEx.printStackTrace();
	            }
	        }
	        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	        out.println("Database error: " + e.getMessage());
	        e.printStackTrace();
	    } 
	}

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {

        response.setContentType("text/plain");
        PrintWriter out = response.getWriter();
        
        String pathInfoString = request.getPathInfo();		
		String[] pathArray = pathInfoString != null ?  pathInfoString.split("/") : null;
		
		if(pathArray == null || pathArray.length == 0)
		{
			Extra.sendError(response, out, "No ID is mentioned");
			return;
		}
		
		String addressSql = "SELECT address_id FROM player WHERE id = ? ";
		
		
		String sql = "DELETE FROM player where id = ?";
		
		
		try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
                PreparedStatement pstmt = conn.prepareStatement(sql); 
				) {
			Integer playerId = Integer.parseInt(pathArray[1]);
			pstmt.setInt(1, playerId);
			int affected = pstmt.executeUpdate();
			
			if(affected > 0)
			Extra.sendSuccess(response, out,"Deleted Successfully");
			else 
				Extra.sendError(response, out, "No Data Found in that id");
		}
		catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            Extra.sendError(response, out, e.getMessage());
            e.printStackTrace();
        }
		catch (Exception e) {
			Extra.sendError(response, out, e.getMessage());
		}
    }
    
    @Override 
    protected void doPut(HttpServletRequest request , HttpServletResponse response) throws ServletException , IOException
    {
    	doPost(request, response);
    }

}
