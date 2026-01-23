package com.floteo.dao;

import com.floteo.model.Vehicle;
import com.floteo.model.VehicleStatus;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO pour la table "vehicle".
 * Gère les opérations CRUD + quelques recherches spécifiques (status, recherche texte, types).
 */
public final class VehicleDao {

    /** Connexion JDBC utilisée pour exécuter les requêtes SQL. */
    private final Connection conn;

    public VehicleDao(Connection conn) {
        this.conn = conn;
    }

    /**
     * Crée un véhicule en base et retourne le véhicule créé (avec son id).
     */
    public Vehicle create(
            String plate,
            String type,
            String brand,
            String model,
            int mileage,
            LocalDate acquisitionDate,
            VehicleStatus status,
            long etat
    ) throws SQLException {

        // INSERT avec RETURNING : récupère directement l'objet inséré (PostgreSQL).
        String sql = """
            INSERT INTO vehicle
              (plate, type, brand, model, mileage, acquisition_date, status, etat)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            RETURNING id, plate, type, brand, model, mileage, acquisition_date, status, etat
            """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            // Remplissage des paramètres dans l'ordre des "?"
            ps.setString(1, plate);
            ps.setString(2, type);
            ps.setString(3, brand);
            ps.setString(4, model);
            ps.setInt(5, mileage);
            ps.setDate(6, Date.valueOf(acquisitionDate)); // LocalDate -> java.sql.Date
            ps.setString(7, status.name());               // Enum -> String (nom exact)
            ps.setLong(8, etat);                          // id ou référence vers l'état

            try (ResultSet rs = ps.executeQuery()) {
                rs.next(); // une seule ligne attendue
                return map(rs);
            }
        }
    }

    /**
     * Recherche un véhicule par son id.
     * @return Optional.empty() si non trouvé
     */
    public Optional<Vehicle> findById(long id) throws SQLException {
        String sql = """
            SELECT id, plate, type, brand, model, mileage, acquisition_date, status, etat
            FROM vehicle
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

    /**
     * Recherche un véhicule par sa plaque.
     * @return Optional.empty() si non trouvé
     */
    public Optional<Vehicle> findByPlate(String plate) throws SQLException {
        String sql = """
            SELECT id, plate, type, brand, model, mileage, acquisition_date, status, etat
            FROM vehicle
            WHERE plate = ?
            """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, plate);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(map(rs));
            }
        }
    }

    /**
     * Retourne tous les véhicules, triés par plaque.
     */
    public List<Vehicle> findAll() throws SQLException {
        String sql = """
            SELECT id, plate, type, brand, model, mileage, acquisition_date, status, etat
            FROM vehicle
            ORDER BY plate
            """;

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            List<Vehicle> out = new ArrayList<>();
            while (rs.next()) {
                out.add(map(rs));
            }
            return out;
        }
    }

    /**
     * Retourne la liste des libellés de types de véhicules (table vehicle_type).
     * Utile pour remplir une liste déroulante côté UI.
     */
    public List<String> findAllTypeLabels() throws SQLException {
        String sql = """
        SELECT label
        FROM vehicle_type
        ORDER BY label
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            List<String> out = new ArrayList<>();
            while (rs.next()) out.add(rs.getString("label"));
            return out;
        }
    }

    /**
     * Retourne les véhicules ayant un statut donné (DISPONIBLE, EN_MISSION, etc.).
     */
    public List<Vehicle> findByStatus(VehicleStatus status) throws SQLException {
        String sql = """
            SELECT id, plate, type, brand, model, mileage, acquisition_date, status, etat
            FROM vehicle
            WHERE status = ?
            ORDER BY plate
            """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());

            try (ResultSet rs = ps.executeQuery()) {
                List<Vehicle> out = new ArrayList<>();
                while (rs.next()) {
                    out.add(map(rs));
                }
                return out;
            }
        }
    }

    /**
     * Met à jour uniquement le statut d'un véhicule.
     * @return true si une ligne a été modifiée
     */
    public boolean updateStatus(long id, VehicleStatus status) throws SQLException {
        String sql = "UPDATE vehicle SET status = ? WHERE id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setLong(2, id);
            return ps.executeUpdate() == 1;
        }
    }

    /**
     * Supprime un véhicule par id.
     * @return true si une ligne a été supprimée
     */
    public boolean delete(long id) throws SQLException {
        String sql = "DELETE FROM vehicle WHERE id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() == 1;
        }
    }

    /**
     * Convertit une ligne SQL en objet Vehicle.
     */
    private static Vehicle map(ResultSet rs) throws SQLException {
        return new Vehicle(
                rs.getLong("id"),
                rs.getString("plate"),
                rs.getString("type"),
                rs.getString("brand"),
                rs.getString("model"),
                rs.getInt("mileage"),
                rs.getDate("acquisition_date").toLocalDate(),       // Date SQL -> LocalDate
                VehicleStatus.valueOf(rs.getString("status")),      // String -> Enum
                rs.getLong("etat")
        );
    }

    /**
     * Recherche des véhicules DISPONIBLES filtrés par texte (plaque / marque / modèle).
     * Si q est vide, renvoie tous les véhicules disponibles.
     */
    public List<Vehicle> findAvailableByText(String q) throws SQLException {
        String sql = """
    SELECT id, plate, type, brand, model, mileage, acquisition_date, status, etat
    FROM vehicle
    WHERE status = 'DISPONIBLE'
      AND (? = '' OR LOWER(plate) LIKE LOWER(?) OR LOWER(brand) LIKE LOWER(?) OR LOWER(model) LIKE LOWER(?))
    ORDER BY plate
    """;

        // Normalisation de la recherche : évite null, supprime espaces.
        String text = (q == null) ? "" : q.trim();
        String like = "%" + text + "%";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            // Le premier paramètre sert à tester si la recherche est vide.
            ps.setString(1, text);
            ps.setString(2, like);
            ps.setString(3, like);
            ps.setString(4, like);

            try (ResultSet rs = ps.executeQuery()) {
                List<Vehicle> out = new ArrayList<>();
                while (rs.next()) out.add(map(rs));
                return out;
            }
        }
    }

    /**
     * Met à jour toutes les infos principales d'un véhicule.
     * @return true si une ligne a été modifiée
     */
    public boolean update(
            long id,
            String plate,
            String type,
            String brand,
            String model,
            int mileage,
            VehicleStatus status,
            long etat
    ) throws SQLException {

        String sql = """
        UPDATE vehicle
        SET plate = ?, type = ?, brand = ?, model = ?, mileage = ?, status = ?, etat = ?
        WHERE id = ?
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, plate);
            ps.setString(2, type);
            ps.setString(3, brand);
            ps.setString(4, model);
            ps.setInt(5, mileage);
            ps.setString(6, status.name());
            ps.setLong(7, etat);
            ps.setLong(8, id);
            return ps.executeUpdate() == 1;
        }
    }
}
