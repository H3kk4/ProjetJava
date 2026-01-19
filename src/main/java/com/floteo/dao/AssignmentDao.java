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

    public List<com.floteo.ui.InterfaceController.AssignmentRow> findRowsForUi(String plateOrText) throws SQLException {
        String sql = """
        SELECT
          a.id                  AS assignment_id,
          a.agent_id            AS agent_id,
          a.vehicle_id          AS vehicle_id,
          a.start_date          AS start_date,
          a.end_date            AS end_date,
          a.status              AS a_status,
          ag.first_name         AS first_name,
          ag.last_name          AS last_name,
          v.brand               AS brand,
          v.model               AS model,
          v.plate               AS plate,
          v.status              AS v_status
        FROM assignment a
        JOIN agent ag   ON ag.id = a.agent_id
        JOIN vehicle v  ON v.id  = a.vehicle_id
        WHERE (? = '' OR LOWER(v.plate) LIKE LOWER(?) OR LOWER(v.brand) LIKE LOWER(?) OR LOWER(v.model) LIKE LOWER(?))
        ORDER BY a.start_date DESC, a.id DESC
        """;

        String q = plateOrText == null ? "" : plateOrText.trim();
        String like = "%" + q + "%";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, q);
            ps.setString(2, like);
            ps.setString(3, like);
            ps.setString(4, like);

            try (ResultSet rs = ps.executeQuery()) {
                List<com.floteo.ui.InterfaceController.AssignmentRow> out = new ArrayList<>();
                while (rs.next()) {
                    LocalDate start = rs.getDate("start_date").toLocalDate();
                    Date endSql = rs.getDate("end_date");
                    LocalDate end = (endSql == null) ? null : endSql.toLocalDate();

                    String agent = rs.getString("last_name") + " " + rs.getString("first_name");
                    String vehicule = rs.getString("brand") + " " + rs.getString("model");
                    String plate = rs.getString("plate");
                    String status = rs.getString("a_status");

                    out.add(new com.floteo.ui.InterfaceController.AssignmentRow(
                            rs.getLong("assignment_id"),
                            rs.getLong("agent_id"),
                            rs.getLong("vehicle_id"),
                            start,               // dateDemande = start (faute de mieux)
                            agent,
                            vehicule,
                            plate,
                            start,
                            end,
                            status
                    ));
                }
                return out;
            }
        }
    }

    public Optional<Assignment> findActiveByAgent(long agentId) throws SQLException {
        String sql = """
        SELECT id, vehicle_id, agent_id, start_date, end_date
        FROM assignment
        WHERE agent_id = ? AND end_date IS NULL
        ORDER BY start_date DESC
        LIMIT 1
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, agentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(map(rs));
            }
        }
    }

    public boolean accept(long assignmentId) throws SQLException {
        String sql = """
      UPDATE assignment
      SET status = 'EN_COURS'
      WHERE id = ? AND status = 'DEMANDEE'
    """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, assignmentId);
            return ps.executeUpdate() == 1;
        }
    }

    public boolean refuse(long assignmentId) throws SQLException {
        String sql = """
      UPDATE assignment
      SET status = 'REFUSEE'
      WHERE id = ? AND status = 'DEMANDEE'
    """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, assignmentId);
            return ps.executeUpdate() == 1;
        }
    }
}
