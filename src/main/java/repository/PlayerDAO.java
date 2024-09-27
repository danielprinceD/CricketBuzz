package repository;

import model.*;
import utils.PlayerRedisUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PlayerDAO {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/CricketBuzz";
    private static final String USER = "root";
    private static final String PASS = "";
    
    private AddressDAO addressDAO = new AddressDAO();

    public List<PlayerVO> getAllPlayers() throws SQLException {
    	
        List<PlayerVO> players = PlayerRedisUtil.getPlayers();
        
        if(players.size() > 0)
        	return players;
        
        String sql = "SELECT P.id, P.name, P.role, A.address_id, A.door_num, A.street, A.city, A.state, A.nationality, P.gender, P.rating, P.batting_style, P.bowling_style FROM player AS P JOIN address AS A ON P.address_id = A.address_id";

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
            	
                PlayerVO player = new PlayerVO();
                player.setId(rs.getInt("id"));
                player.setName(rs.getString("name"));
                player.setRole(rs.getString("role"));
                player.setGender(rs.getString("gender"));
                player.setRating(rs.getDouble("rating"));
                player.setBattingStyle(rs.getString("batting_style"));
                player.setBowlingStyle(rs.getString("bowling_style"));

                AddressVO address = new AddressVO();
                address.setAddressId(rs.getInt("address_id"));
                address.setDoorNum(rs.getString("door_num"));
                address.setStreet(rs.getString("street"));
                address.setCity(rs.getString("city"));
                address.setState(rs.getString("state"));
                address.setNationality(rs.getString("nationality"));

                player.setAddress(address);
                players.add(player);
            }
            
        }
        if(players.size() > 0)
        	PlayerRedisUtil.setPlayers(players);
        
        return players;
    }

   	    public boolean insertPlayer(PlayerVO player) throws SQLException {
   	    	
	        int addressId = addressDAO.insertAddress(player.getAddress());
	        if (addressId > 0) {
	            String sql = "INSERT INTO player (name, role, address_id, gender, rating, batting_style, bowling_style) VALUES (?, ?, ?, ?, ?, ?, ?)";
	            
	            try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
	                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
	                
	                pstmt.setString(1, player.getName());
	                pstmt.setString(2, player.getRole());
	                pstmt.setInt(3, addressId);
	                pstmt.setString(4, player.getGender());
	                pstmt.setDouble(5, player.getRating());
	                pstmt.setString(6, player.getBattingStyle());
	                pstmt.setString(7, player.getBowlingStyle());
	               
	                int rowsAffected = pstmt.executeUpdate();
	                
	                try(ResultSet generatedKey = pstmt.getGeneratedKeys()){
	                	
	                	if(generatedKey.next())
	                	{
	                		int playerId = generatedKey.getInt(1);
	                		if(PlayerRedisUtil.isCached())
	    	                	PlayerRedisUtil.setPlayerById(player, playerId);
	                	}
	                }
	                return rowsAffected > 0;
	            }
	        }
	        return false;
	    }
	
   	 public int getOldAddressId(int playerId) throws SQLException {
   	    String addressSql = "SELECT address_id FROM player WHERE id = ?";
   	    
   	    try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
   	         PreparedStatement pstmt = conn.prepareStatement(addressSql)) {
   	         
   	        pstmt.setInt(1, playerId); 
   	        
   	        try (ResultSet rs = pstmt.executeQuery()) {
   	            if (rs.next()) {
   	                return rs.getInt("address_id"); 
   	            }
   	        }
   	    } catch (SQLException e) {
   	        e.printStackTrace();
   	        throw e;
   	    }
   	    
   	    return -1; 
   	}

   	    
	    public boolean updatePlayer(PlayerVO player) throws SQLException {
	        
	    	int addressId = getOldAddressId(player.getId());
	    	
	    	if(addressId != player.getAddress().getAddressId())
	    		throw new SQLException("You cannot change address_id");

	    	boolean addressUpdated = addressDAO.updateAddress(player.getAddress());
	        
	        if (addressUpdated) {
	            String sql = "UPDATE player SET name = ?, role = ?, address_id = ?, gender = ?, rating = ?, batting_style = ?, bowling_style = ? WHERE id = ?";
	            
	            try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
	                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
	                
	                pstmt.setString(1, player.getName());
	                pstmt.setString(2, player.getRole());
	                pstmt.setInt(3, player.getAddress().getAddressId());
	                pstmt.setString(4, player.getGender());
	                pstmt.setDouble(5, player.getRating());
	                pstmt.setString(6, player.getBattingStyle());
	                pstmt.setString(7, player.getBowlingStyle());
	                pstmt.setInt(8, player.getId());
	                
	                int rowsAffected = pstmt.executeUpdate();
	                if(rowsAffected > 0 && PlayerRedisUtil.isCached())
	                	PlayerRedisUtil.setPlayerById(player, player.getId());
	                	
	                return rowsAffected > 0;
	            }
	        }
	        return false;
	    }
	    
	    
	    public boolean deletePlayer(int playerId) throws SQLException {
	       
	    	int addressId = getOldAddressId(playerId);
	        
	        if (addressId < 0) {
	            throw new SQLException("Cannot find address ID for deletion");
	        }
	        
	        AddressDAO addressDAO = new AddressDAO();
	        
	        if (!addressDAO.deleteAddress(addressId)) {
	            throw new SQLException("Error: Cannot delete Address ID");
	        }
	        
	        String sql = "DELETE FROM player WHERE id = ?";
	        
	        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
	             PreparedStatement pstmt = conn.prepareStatement(sql)) {
	             
	            pstmt.setInt(1, playerId);
	            int affectedRows = pstmt.executeUpdate();
	            if(PlayerRedisUtil.isCached())
	            	PlayerRedisUtil.deletePlayerById(playerId);
	            	
	            	
	            return (affectedRows > 0);
	        }
	    }

    
    public PlayerVO getPlayerById(int playerId) throws SQLException {
        
    	PlayerVO player = PlayerRedisUtil.getPlayerById(playerId);
    	
    	if(player != null)
    		return player;
        
    	String sql = "SELECT P.id, P.name, P.role, A.address_id, A.door_num, A.street, A.city, A.state, A.nationality, P.gender, P.rating, P.batting_style, P.bowling_style FROM player AS P JOIN address AS A ON P.address_id = A.address_id WHERE P.id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, playerId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                
            	player = new PlayerVO();
                player.setId(rs.getInt("id"));
                player.setName(rs.getString("name"));
                player.setRole(rs.getString("role"));
                player.setGender(rs.getString("gender"));
                player.setRating(rs.getDouble("rating"));
                player.setBattingStyle(rs.getString("batting_style"));
                player.setBowlingStyle(rs.getString("bowling_style"));

                AddressVO address = new AddressVO();
                
                address.setAddressId(rs.getInt("address_id"));
                address.setDoorNum(rs.getString("door_num"));
                address.setStreet(rs.getString("street"));
                address.setCity(rs.getString("city"));
                address.setState(rs.getString("state"));
                address.setNationality(rs.getString("nationality"));
                player.setAddress(address);

            }
        }
        if(player != null && PlayerRedisUtil.isCached() )
        	PlayerRedisUtil.setPlayerById(player , playerId);
        	
        return player;
    }
}
