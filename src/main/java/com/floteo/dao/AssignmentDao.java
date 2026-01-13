package com.floteo.dao;

import com.floteo.model.Assignment;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class AssignmentDao {
    private final Connection conn;

    public AssignmentDao(Connection conn) {
        this.conn = conn;
    }

    public Assignment create(long vehicleId, long agentId, LocalDate startDate, LocalDate endDate) throws SQLException {
        String sql = """
      INSERT INTO assignment(vehicle_id, agent_id, start_date, end_date)
      VALUES (?, ?, ?, ?)
      RETURNING id, vehicle_id, agent_id, start_date, end_date
      """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, vehicleId);
            ps.setLong(2, agentId);
            ps.setDate(3, Date.valueOf(startDate));
            if (endDate == null) ps.setNull(4, java.sql.Types.DATE);
            else ps.setDate(4, Date.valueOf(endDate));

            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return map(rs);
            }
        }
    }

    public Optional<Assignment> findById(long id) throws SQLException {
        String sql = "SELECT id, vehicle_id, agent_id, start_date, end_date FROM assignment WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(map(rs));
            }
        }
    }

    public Optional<Assignment> findActiveByVehicle(long vehicleId) throws SQLException {
        String sql = """
      SELECT id, vehicle_id, agent_id, start_date, end_date
      FROM assignment
      WHERE vehicle_id = ?
        AND end_date IS NULL
      ORDER BY start_date DESC
      LIMIT 1
      """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, vehicleId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(map(rs));
            }
        }
    }

    public List<Assignment> findAll() throws SQLException {
        String sql = "SELECT id, vehicle_id, agent_id, start_date, end_date FROM assignment ORDER BY start_date DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Assignment> out = new ArrayList<>();
            while (rs.next()) out.add(map(rs));
            return out;
        }
    }

    public List<Assignment> findByAgent(long agentId) throws SQLException {
        String sql = """
      SELECT id, vehicle_id, agent_id, start_date, end_date
      FROM assignment
      WHERE agent_id = ?
      ORDER BY start_date DESC
      """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, agentId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Assignment> out = new ArrayList<>();
                while (rs.next()) out.add(map(rs));
                return out;
            }
        }
    }

    public List<Assignment> findByVehicle(long vehicleId) throws SQLException {
        String sql = """
      SELECT id, vehicle_id, agent_id, start_date, end_date
      FROM assignment
      WHERE vehicle_id = ?
      ORDER BY start_date DESC
      """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, vehicleId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Assignment> out = new ArrayList<>();
                while (rs.next()) out.add(map(rs));
                return out;
            }
        }
    }

    public boolean endAssignment(long assignmentId, LocalDate endDate) throws SQLException {
        String sql = "UPDATE assignment SET end_date = ? WHERE id = ? AND end_date IS NULL";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(endDate));
            ps.setLong(2, assignmentId);
            return ps.executeUpdate() == 1;
        }
    }

    private static Assignment map(ResultSet rs) throws SQLException {
        LocalDate start = rs.getDate("start_date").toLocalDate();
        Date endSql = rs.getDate("end_date");
        LocalDate end = (endSql == null) ? null : endSql.toLocalDate();

        return new Assignment(
                rs.getLong("id"),
                rs.getLong("vehicle_id"),
                rs.getLong("agent_id"),
                start,
                end
        );
    }
}
