package Team;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.core.JsonParser;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import Auth.Auth;
import Auth.UserData;
import Model.PlayerModel;
import Model.UserModel;

public class User extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
    private static final String DB_URL = "jdbc:mysql://localhost:3306/CricketBuzz";
    private static final String USER = "root";
    private static final String PASS = "";
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
    	
    	String pathInfo = request.getPathInfo();
    	PrintWriter out = response.getWriter();
    	
    	if(pathInfo == null)
    	{
    		Extra.sendError(response, out , "ID is required");
    		return;
    	}
    	String pathArray[] = pathInfo.split("/");
    	
    	if(pathArray.length < 2 || pathArray.length > 2)
    	{
    		Extra.sendError(response, out, "Enter a valid path");
    		return;
    	}
    	
    	String sql = "SELECT U.user_id , U.name , U.role , U.email , U.password , A.address_id , A.door_num , A.street , A.city , A.nationality FROM user AS U JOIN address AS A ON A.address_id = U.address_id WHERE user_id = ?";
    	
    	try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
    		     PreparedStatement pstmt = conn.prepareStatement(sql)) {
    		    
    		    int userId = Integer.parseInt(pathArray[1]); 
    		    pstmt.setInt(1, userId); 
    		    
    		    try (ResultSet rs = pstmt.executeQuery()) {
    		        if (rs.next()) {
    		          
    		        	int id = rs.getInt("user_id");
    		            String name = rs.getString("name");
    		            String email = rs.getString("email");
    		            String password = rs.getString("password");
    		            int addressId = rs.getInt("address_id");
    		            String role = rs.getString("role");
    		            
    		            JSONObject userObject = new JSONObject();
    		           
    		            userObject.put("user_id", id);
    		            userObject.put("name", name);
    		            userObject.put("email", email);
    		            userObject.put("password", password);
    		            userObject.put("role", role);
    		            
    		            JSONObject addressObject = new JSONObject();
    		            
    		            addressObject.put("address_id", addressId);
    		            addressObject.put("door_num", rs.getString("door_num"));
    		            addressObject.put("city", rs.getString("city"));
    		            addressObject.put("street", rs.getString("street"));
    		            addressObject.put("nationality", rs.getString("nationality"));
    		            
    		            userObject.put("address", addressObject);
    		            
    		            out.println(userObject.toString());
    		        
    		        } else {
    		        	Extra.sendError(response, out, "User not found");
    		        	return;
    		        }
    		    }
    		} catch (SQLException e) {
    			Extra.sendError(response, out, e.getMessage());
	        	return;
    		} catch (Exception e) {
    			Extra.sendError(response, out, e.getMessage());
	        	return;
    		}
    	
	}
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Boolean isPut = request.getMethod().equalsIgnoreCase("PUT");

        StringBuilder jsonString = new StringBuilder();
        BufferedReader reader = request.getReader();
        String line;
        while ((line = reader.readLine()) != null) {
            jsonString.append(line);
        }

        PrintWriter out = response.getWriter();
        String pathInfo = request.getPathInfo();
        String[] pathArray = (pathInfo == null) ? new String[] {} : pathInfo.split("/");

        if (isPut && (pathArray.length <= 1)) {
            Extra.sendError(response, response.getWriter(), "User ID is required");
            return;
        }
        
        if(pathArray != null && pathArray.length == 2 && pathArray[1].equals("login"))
        {
        	
        	 String sql = "SELECT  user_id , email, password, role FROM user WHERE email = ? AND password = ?";
             
             try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASS);
                  PreparedStatement statement = connection.prepareStatement(sql)) {
            	 
            	 JsonObject jsonObject = com.google.gson.JsonParser.parseString(jsonString.toString()).getAsJsonObject();
            	 
            	 String email = jsonObject.has("email") ? jsonObject.get("email").getAsString() : null;
                 String password = jsonObject.has("password") ? jsonObject.get("password").getAsString() : null;
            	 
            	 
                 statement.setString(1, email);
                 statement.setString(2, password);

                 ResultSet resultSet = statement.executeQuery();

                 if (resultSet.next()) {
                	 
                     String role = resultSet.getString("role");
                     Integer id = resultSet.getInt("user_id");
                     System.out.println(id);
                     response.setContentType("application/json");
                     response.setStatus(HttpServletResponse.SC_OK);
                     
                     UserData.u1 = new UserData( id , email , password , role);
                     
                     out.println("{\"message\":\" "+ role +" login successful\"}");
                     
                     return;
                 } else {
                     response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid email or password");
                 }
             } catch (SQLException e) {
                 response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error: " + e.getMessage());
                 e.printStackTrace();
             }
        }
        
        if(pathArray != null && pathArray.length == 2 && pathArray[1].equals("logout"))
        {
        	UserData.u1 = new UserData(-1, "Guest", "", "USER");
        	Extra.sendSuccess(response, out, "Logged out");
        	return;
        }
        	
        if(isPut && pathArray != null && pathArray.length != 2)
        {
        	Extra.sendError(response, out, "Enter a valid path");
        	return;
        }
        
        	UserModel userModel = new Gson().fromJson(jsonString.toString(), UserModel.class);

            try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
                conn.setAutoCommit(false); 

                String sql = isPut
                        ? "UPDATE user SET name = ?, role = ?, address_id = ?, email = ?, password = ? WHERE user_id = ?"
                        : "INSERT INTO user (name, role, address_id, email, password) VALUES (?, ?, ?, ?, ?)";

                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, userModel.getName());
                    pstmt.setString(2, userModel.getRole());

                    String addressSql = isPut
                            ? "UPDATE address SET door_num = ?, street = ?, city = ?, state = ?, nationality = ? WHERE address_id = ?"
                            : "INSERT INTO address (door_num, street, city, state, nationality) VALUES (?, ?, ?, ?, ?)";

                    try (PreparedStatement addressPstmt = conn.prepareStatement(addressSql, Statement.RETURN_GENERATED_KEYS)) {
                        addressPstmt.setString(1, userModel.getAddress().door_num);
                        addressPstmt.setString(2, userModel.getAddress().street);
                        addressPstmt.setString(3, userModel.getAddress().city);
                        addressPstmt.setString(4, userModel.getAddress().state);
                        addressPstmt.setString(5, userModel.getAddress().nationality);

                        if (isPut) {
                            addressPstmt.setInt(6, userModel.getAddress().address_id);
                        }

                        int addressRowsAffected = addressPstmt.executeUpdate();

                        if (!isPut && addressRowsAffected > 0) {
                            try (ResultSet generatedKeys = addressPstmt.getGeneratedKeys()) {
                                if (generatedKeys.next()) {
                                    int addressId = generatedKeys.getInt(1);
                                    pstmt.setInt(3, addressId); 
                                }
                            }
                        } else {
                            pstmt.setInt(3, userModel.getAddress().address_id);
                        }
                    }

                    pstmt.setString(4, userModel.getEmail());
                    pstmt.setString(5, userModel.getPassword());

                    if (isPut) {
                        pstmt.setInt(6, Integer.parseInt(pathArray[1]));
                    }

                    int rowsAffected = pstmt.executeUpdate();
                    if (rowsAffected <= 0) {
                        Extra.sendError(response, out, "Failed to process User data.");
                        conn.rollback();
                        return;
                    }

                    conn.commit();
                    Extra.sendSuccess(response, out, "User Processed successfully.");

                } catch (SQLException e) {
                    if (e.getSQLState().equals("23000")) {
                        response.setStatus(HttpServletResponse.SC_CONFLICT);
                        Extra.sendError(response, out, "Email address already exists.");
                    } else {
                        conn.rollback();
                        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        Extra.sendError(response, out, e.getMessage());
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (Exception e) {
                Extra.sendError(response, out, e.getMessage());
            }
        
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }
    
    @Override
    protected void doDelete(HttpServletRequest request , HttpServletResponse response) throws ServletException , IOException{
    	
    	 response.setContentType("text/plain");
         PrintWriter out = response.getWriter();
         
         String pathInfoString = request.getPathInfo();		
 		String[] pathArray = pathInfoString != null ?  pathInfoString.split("/") : null;
 		
 		if(pathArray == null || pathArray.length == 0)
 		{
 			Extra.sendError(response, out, "No ID is mentioned");
 			return;
 		}
 		
 		
 		
 		String sql = "DELETE FROM user where user_id = ?";
 		
 		
 		try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
                 PreparedStatement pstmt = conn.prepareStatement(sql); 
 				) {
 			Integer userId = Integer.parseInt(pathArray[1]);
 			pstmt.setInt(1, userId);
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

	

}
