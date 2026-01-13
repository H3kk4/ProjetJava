package com.floteo.dao;

import com.floteo.model.Vehicle;
import com.floteo.model.VehicleStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class VehicleDao {
    private final Connection conn;

    public VehicleDao(Connection conn) {
        this.conn = conn;
    }

    public Vehicle create(String registration, String brand, String model, VehicleStatus status) throws SQLException {
        String sql = """
      INSERT INTO vehicle(registration, brand, model, status)
      VALUES (?, ?, ?, ?)
      RETURNING id, registration, brand, model, status
      """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, registration);
            ps.setString(2, brand);
            ps.setString(3, model);
            ps.setString(4, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return map(rs);
            }
        }
    }

    public Optional<Vehicle> findById(long id) throws SQLException {
        String sql = "SELECT id, registration, brand, model, status FROM vehicle WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(map(rs));
            }
        }
    }

    public Optional<Vehicle> findByRegistration(String registration) throws SQLException {
        String sql = "SELECT id, registration, brand, model, status FROM vehicle WHERE registration = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, registration);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(map(rs));
            }
        }
    }

    public List<Vehicle> findAll() throws SQLException {
        String sql = "SELECT id, registration, brand, model, status FROM vehicle ORDER BY registration";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Vehicle> out = new ArrayList<>();
            while (rs.next()) out.add(map(rs));
            return out;
        }
    }

    public List<Vehicle> findByStatus(VehicleStatus status) throws SQLException {
        String sql = """
      SELECT id, registration, brand, model, status
      FROM vehicle
      WHERE status = ?
      ORDER BY registration
      """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                List<Vehicle> out = new ArrayList<>();
                while (rs.next()) out.add(map(rs));
                return out;
            }
        }
    }

    public boolean updateStatus(long id, VehicleStatus status) throws SQLException {
        String sql = "UPDATE vehicle SET status = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setLong(2, id);
            return ps.executeUpdate() == 1;
        }
    }

    public boolean delete(long id) throws SQLException {
        String sql = "DELETE FROM vehicle WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() == 1;
        }
    }

    private static Vehicle map(ResultSet rs) throws SQLException {
        return new Vehicle(
                rs.getLong("id"),
                rs.getString("registration"),
                rs.getString("brand"),
                rs.getString("model"),
                VehicleStatus.valueOf(rs.getString("status"))
        );
    }
}
