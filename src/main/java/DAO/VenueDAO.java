package DAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import Model.VenueVO;

public class VenueDAO {
	
    private static final String DB_URL = "jdbc:mysql://localhost:3306/CricketBuzz";
    private static final String USER = "root";
    private static final String PASS = "";

    private static final String INSERT_VENUE = "INSERT INTO venue (stadium, location, pitch_condition, description, capacity, curator) VALUES (?, ?, ?, ?, ?, ?)";
    private static final String UPDATE_VENUE = "UPDATE venue SET stadium = ?, location = ?, pitch_condition = ?, description = ?, capacity = ?, curator = ? WHERE venue_id = ?";
    private static final String DELETE_VENUE = "DELETE FROM venue WHERE venue_id = ?";
    private static final String SELECT_ALL = "SELECT * FROM venue";
    private static final String SELECT_BY_ID = "SELECT * FROM venue WHERE venue_id = ?";

    
    public int insertVenue( VenueVO venue) throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(INSERT_VENUE)) {

            pstmt.setString(1, venue.getStadium());
            pstmt.setString(2, venue.getLocation());
            pstmt.setString(3, venue.getPitchCondition());
            pstmt.setString(4, venue.getDescription());
            pstmt.setLong(5, venue.getCapacity());
            pstmt.setString(6, venue.getCurator());

            return pstmt.executeUpdate();
        }
    }

    public int updateVenue(VenueVO venue) throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(UPDATE_VENUE)) {

            pstmt.setString(1, venue.getStadium());
            pstmt.setString(2, venue.getLocation());
            pstmt.setString(3, venue.getPitchCondition());
            pstmt.setString(4, venue.getDescription());
            pstmt.setLong(5, venue.getCapacity());
            pstmt.setString(6, venue.getCurator());
            pstmt.setInt(7, venue.getVenueId());

            return pstmt.executeUpdate();
        }
    }

    public void deleteVenue(int venueId) throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(DELETE_VENUE)) {

            pstmt.setInt(1, venueId);
            pstmt.executeUpdate();
        }
    }

    public List<VenueVO> getAllVenues() throws SQLException {
        List<VenueVO> venues = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(SELECT_ALL);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                VenueVO venue = new VenueVO();
                venue.setVenueId(rs.getInt("venue_id"));
                venue.setStadium(rs.getString("stadium"));
                venue.setLocation(rs.getString("location"));
                venue.setPitchCondition(rs.getString("pitch_condition"));
                venue.setDescription(rs.getString("description"));
                venue.setCapacity(rs.getLong("capacity"));
                venue.setCurator(rs.getString("curator"));

                venues.add(venue);
            }
        }
        return venues;
    }

    public VenueVO getVenueById(int venueId) throws SQLException {
        VenueVO venue = null;
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(SELECT_BY_ID)) {

            pstmt.setInt(1, venueId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    venue = new VenueVO();
                    venue.setVenueId(rs.getInt("venue_id"));
                    venue.setStadium(rs.getString("stadium"));
                    venue.setLocation(rs.getString("location"));
                    venue.setPitchCondition(rs.getString("pitch_condition"));
                    venue.setDescription(rs.getString("description"));
                    venue.setCapacity(rs.getLong("capacity"));
                    venue.setCurator(rs.getString("curator"));
                }
            }
        }
        return venue;
    }
}
