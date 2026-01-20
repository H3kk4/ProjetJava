package com.floteo.dao;

import com.floteo.model.Etat;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EtatDao {

    private final Connection conn;

    public EtatDao(Connection conn) {
        this.conn = conn;
    }

    public List<Etat> findAll() throws SQLException {
        String sql = """
            SELECT id, name
            FROM etat
            """;

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            List<Etat> out = new ArrayList<>();
            while (rs.next()) {
                out.add(map(rs));
            }
            return out;
        }
    }

    public Optional<Etat> findByName(String name) throws SQLException {
        String sql = """
            SELECT id, name
            FROM etat
            WHERE name= ?
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(map(rs));
            }
        }
    }

    private static Etat map(ResultSet rs) throws SQLException {
        return new Etat(
                rs.getLong("id"),
                rs.getString("name")
        );
    }
}