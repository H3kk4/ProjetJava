package com.floteo.dao;

import com.floteo.model.Agent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class AgentDao {

    private final Connection conn;

    public AgentDao(Connection conn) {
        this.conn = conn;
    }

    public Agent create(
            String matricule,
            String firstName,
            String lastName,
            long serviceId
    ) throws SQLException {

        String sql = """
            INSERT INTO agent (matricule, first_name, last_name, service_id)
            VALUES (?, ?, ?, ?)
            RETURNING id, matricule, first_name, last_name, service_id
            """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, matricule);
            ps.setString(2, firstName);
            ps.setString(3, lastName);
            ps.setLong(4, serviceId);

            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return map(rs);
            }
        }
    }

    public Optional<Agent> findById(long id) throws SQLException {
        String sql = """
            SELECT id, matricule, first_name, last_name, service_id
            FROM agent
            WHERE id = ?
            """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(map(rs));
            }
        }
    }

    public Optional<Agent> findByMatricule(String matricule) throws SQLException {
        String sql = """
            SELECT id, matricule, first_name, last_name, service_id
            FROM agent
            WHERE matricule = ?
            """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, matricule);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(map(rs));
            }
        }
    }

    public List<Agent> findAll() throws SQLException {
        String sql = """
            SELECT id, matricule, first_name, last_name, service_id
            FROM agent
            ORDER BY last_name, first_name
            """;

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            List<Agent> out = new ArrayList<>();
            while (rs.next()) {
                out.add(map(rs));
            }
            return out;
        }
    }

    public List<Agent> findByServiceId(long serviceId) throws SQLException {
        String sql = """
            SELECT id, matricule, first_name, last_name, service_id
            FROM agent
            WHERE service_id = ?
            ORDER BY last_name, first_name
            """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, serviceId);

            try (ResultSet rs = ps.executeQuery()) {
                List<Agent> out = new ArrayList<>();
                while (rs.next()) {
                    out.add(map(rs));
                }
                return out;
            }
        }
    }

    public boolean delete(long id) throws SQLException {
        String sql = "DELETE FROM agent WHERE id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() == 1;
        }
    }

    private static Agent map(ResultSet rs) throws SQLException {
        return new Agent(
                rs.getLong("id"),
                rs.getString("matricule"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getLong("service_id")
        );
    }

    public boolean update(long id, String matricule, String firstName, String lastName, long serviceId) throws SQLException {
        String sql = """
        UPDATE agent
        SET matricule = ?, first_name = ?, last_name = ?, service_id = ?
        WHERE id = ?
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, matricule);
            ps.setString(2, firstName);
            ps.setString(3, lastName);
            ps.setLong(4, serviceId);
            ps.setLong(5, id);
            return ps.executeUpdate() == 1;
        }
    }

    public List<Agent> findByText(String q) throws SQLException {
        String sql = """
        SELECT id, matricule, first_name, last_name, service_id
        FROM agent
        WHERE (? = '' OR LOWER(matricule) LIKE LOWER(?) OR LOWER(first_name) LIKE LOWER(?) OR LOWER(last_name) LIKE LOWER(?))
        ORDER BY last_name, first_name
        """;

        String text = (q == null) ? "" : q.trim();
        String like = "%" + text + "%";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, text);
            ps.setString(2, like);
            ps.setString(3, like);
            ps.setString(4, like);

            try (ResultSet rs = ps.executeQuery()) {
                List<Agent> out = new ArrayList<>();
                while (rs.next()) out.add(map(rs));
                return out;
            }
        }
    }


}
