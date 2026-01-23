package com.floteo.dao;

import com.floteo.model.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO pour la table "service".
 * Gère la création, la lecture et la suppression des services.
 */
public final class ServiceDao {
    private final Connection conn;

    public ServiceDao(Connection conn) {
        this.conn = conn;
    }

    /**
     * Crée un nouveau service en base et retourne l'objet créé.
     */
    public Service create(String name) throws SQLException {
        String sql = "INSERT INTO service(name) VALUES (?) RETURNING id, name";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);

            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return new Service(rs.getLong("id"), rs.getString("name"));
            }
        }
    }

    /**
     * Recherche un service par son id.
     */
    public Optional<Service> findById(long id) throws SQLException {
        String sql = "SELECT id, name FROM service WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(new Service(rs.getLong("id"), rs.getString("name")));
            }
        }
    }

    /**
     * Recherche un service par son nom.
     */
    public Optional<Service> findByName(String name) throws SQLException {
        String sql = "SELECT id, name FROM service WHERE name = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(new Service(rs.getLong("id"), rs.getString("name")));
            }
        }
    }

    /**
     * Retourne tous les services triés par nom.
     */
    public List<Service> findAll() throws SQLException {
        String sql = "SELECT id, name FROM service ORDER BY name";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            List<Service> out = new ArrayList<>();
            while (rs.next()) {
                out.add(new Service(rs.getLong("id"), rs.getString("name")));
            }
            return out;
        }
    }

    /**
     * Supprime un service par id.
     * @return true si une ligne a été supprimée
     */
    public boolean delete(long id) throws SQLException {
        String sql = "DELETE FROM service WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() == 1;
        }
    }
}
