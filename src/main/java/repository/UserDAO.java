package repository;

import java.io.IOException;
import java.io.PrintWriter;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.SessionCookieConfig;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONObject;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import model.*;
import utils.AuthUtil;
import utils.PasswordUtil;
import controller.*;

public class UserDAO {
	
	private static final String DB_URL = "jdbc:mysql://localhost:3306/CricketBuzz";
    private static final String USER = "root";
    private static final String PASS = "";
    
    public void get(HttpServletRequest request , HttpServletResponse response) throws Exception {
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
    		            int addressId = rs.getInt("address_id");
    		            String role = rs.getString("role");
    		            
    		            JSONObject userObject = new JSONObject();
    		           
    		            userObject.put("user_id", id);
    		            userObject.put("name", name);
    		            userObject.put("email", email);
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
    
    public void register(HttpServletRequest request , HttpServletResponse response , Boolean isPut  , StringBuilder jsonString) throws Exception {
    	PrintWriter out = response.getWriter();
        String pathInfo = request.getPathInfo();
        String[] pathArray = (pathInfo == null) ? new String[] {} : pathInfo.split("/");

        if (isPut && (pathArray.length <= 1)) {
            Extra.sendError(response, response.getWriter(), "User ID is required");
            return;
        }
        
        
        
        if(pathArray != null && pathArray.length == 2 && pathArray[1].equals("logout"))
        {
        	Extra.sendSuccess(response, out, "Logged out");
        	return;
        }
        	
        if(isPut && pathArray != null && pathArray.length != 2)
        {
        	Extra.sendError(response, out, "Enter a valid path");
        	return;
        }
        
        	UserVO userModel = new Gson().fromJson(jsonString.toString(), UserVO.class);

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
                        addressPstmt.setString(1, userModel.getAddress().getDoorNum());
                        addressPstmt.setString(2, userModel.getAddress().getStreet());
                        addressPstmt.setString(3, userModel.getAddress().getCity());
                        addressPstmt.setString(4, userModel.getAddress().getState());
                        addressPstmt.setString(5, userModel.getAddress().getNationality());

                        if (isPut) {
                            addressPstmt.setInt(6, userModel.getAddress().getAddressId());
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
                            pstmt.setInt(3, userModel.getAddress().getAddressId());
                        }
                    }
                    
                    String hashedPassword = PasswordUtil.hashPassword(userModel.getPassword());
                    
                    pstmt.setString(4, userModel.getEmail());
                    pstmt.setString(5, hashedPassword);

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
    
    
   public void login(HttpServletRequest request , HttpServletResponse response , PrintWriter out , StringBuilder jsonString ) throws IOException, NoSuchAlgorithmException {
    	
    	String sql = "SELECT  user_id , email, password, role FROM user WHERE email = ? AND password = ?";
    	
    	try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASS);
           
    		PreparedStatement statement = connection.prepareStatement(sql) ) {
       	 
       	 JsonObject jsonObject = com.google.gson.JsonParser.parseString(jsonString.toString()).getAsJsonObject();
       	 
       	 String email = jsonObject.has("email") ? jsonObject.get("email").getAsString() : null;
            String password = jsonObject.has("password") ? jsonObject.get("password").getAsString() : null;
       	 	
            String hashedPassword =  PasswordUtil.hashPassword(password);
       	 
            statement.setString(1, email);
            statement.setString(2, hashedPassword);

            ResultSet resultSet = statement.executeQuery();
            
            
            if (resultSet.next()) {
           	 
                String role = resultSet.getString("role");
                Integer id = resultSet.getInt("user_id");
                
                System.out.println(id);
                response.setContentType("application/json");
                response.setStatus(HttpServletResponse.SC_OK);
                
                
                String generatedToken =  AuthUtil.generateToken( id+"", role);
                Cookie cookie = new Cookie("token", generatedToken);
                cookie.setSecure(false);
                cookie.setPath("/");
                
                response.addCookie(cookie); 
                response.setHeader("token", generatedToken);
                
                JSONObject output = new JSONObject();
                
                output.put("message", "login successful");
                output.put("token", generatedToken);
                
                
                out.print(output.toString());
                
                return;
            } else {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid email or password");
            }
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
            e.printStackTrace();
        }
   
    	
    	
    	
    	
    }
    
    
}
