package com.floteo.dao;

import com.floteo.model.Etat;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO pour la table "etat".
 * Permet de lire les différents états disponibles en base.
 */
public class EtatDao {

    /** Connexion JDBC utilisée pour les requêtes SQL. */
    private final Connection conn;

    public EtatDao(Connection conn) {
        this.conn = conn;
    }

    /**
     * Retourne tous les états présents en base.
     */
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

    /**
     * Recherche un état par son nom.
     * @return Optional.empty() si aucun état ne correspond
     */
    public Optional<Etat> findByName(String name) throws SQLException {
        String sql = """
            SELECT id, name
            FROM etat
            WHERE name = ?
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(map(rs));
            }
        }
    }

    /**
     * Convertit une ligne SQL en objet Etat.
     */
    private static Etat map(ResultSet rs) throws SQLException {
        return new Etat(
                rs.getLong("id"),
                rs.getString("name")
        );
    }
}
