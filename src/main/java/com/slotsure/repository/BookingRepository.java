package com.slotsure.repository;

import com.slotsure.config.Database;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BookingRepository {

    public record BookingDisplay(long id, String resourceName, String bookedBy, String start, String end, String purpose, String status) {}

    public List<BookingDisplay> fetchMasterSchedule() throws SQLException {
        String sql = """
            SELECT b.id, r.name AS resource, u.full_name, b.start_at, b.end_at, b.purpose,
                CASE 
                    WHEN b.status = 'CANCELLED' THEN 'Cancelled'
                    WHEN NOW() > b.end_at THEN 'Completed'
                    WHEN NOW() BETWEEN b.start_at AND b.end_at THEN 'In Progress'
                    WHEN DATE(b.start_at) = CURDATE() THEN 'Today'
                    WHEN DATE(b.start_at) = CURDATE() + INTERVAL 1 DAY THEN 'Tomorrow'
                    ELSE 'Upcoming'
                END AS live_status
            FROM bookings b
            JOIN resources r ON b.resource_id = r.id
            JOIN app_users u ON b.booked_by = u.id
            ORDER BY b.start_at DESC LIMIT 100
            """;

        List<BookingDisplay> list = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                list.add(new BookingDisplay(
                    rs.getLong("id"), 
                    rs.getString("resource"), 
                    rs.getString("full_name"),
                    rs.getTimestamp("start_at").toLocalDateTime().toString().replace("T", " "),
                    rs.getTimestamp("end_at").toLocalDateTime().toString().replace("T", " "),
                    rs.getString("purpose"), 
                    rs.getString("live_status") // Injecting the calculated real-time status
                ));
            }
        }
        return list;
    }

    public void cancelBooking(long bookingId) throws SQLException {
        String sql = "UPDATE bookings SET status = 'CANCELLED' WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, bookingId);
            stmt.executeUpdate();
        }
    }
}