package com.slotsure.repository;

import com.slotsure.config.Database;
import com.slotsure.model.Resource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ResourceRepository {

    public List<Resource> findActive() throws SQLException {
        return fetchResources("SELECT id, name, resource_type, location, capacity, is_active FROM resources WHERE is_active = TRUE ORDER BY name");
    }

    public List<Resource> findAll() throws SQLException {
        return fetchResources("SELECT id, name, resource_type, location, capacity, is_active FROM resources ORDER BY name");
    }

    private List<Resource> fetchResources(String sql) throws SQLException {
        List<Resource> resources = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                resources.add(new Resource(
                    rs.getLong("id"), rs.getString("name"), rs.getString("resource_type"),
                    rs.getString("location"), rs.getInt("capacity"), rs.getBoolean("is_active")
                ));
            }
        }
        return resources;
    }

    public void toggleResourceStatus(long resourceId, boolean makeActive) throws SQLException {
        String sql = "UPDATE resources SET is_active = ? WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBoolean(1, makeActive);
            stmt.setLong(2, resourceId);
            stmt.executeUpdate();
        }
    }
}