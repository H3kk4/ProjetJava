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

/**
 * DAO pour accéder à la table "assignment" (affectations).
 * Permet de créer, lire et mettre à jour l'état d'une affectation.
 */
public final class AssignmentDao {
    private final Connection conn;

    public AssignmentDao(Connection conn) {
        this.conn = conn;
    }

    /**
     * Crée une affectation (vehicle -> agent) entre deux dates.
     * endDate peut être null (affectation active).
     */
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

            // Si endDate est null, on stocke NULL en base
            if (endDate == null) ps.setNull(4, java.sql.Types.DATE);
            else ps.setDate(4, Date.valueOf(endDate));

            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return map(rs);
            }
        }
    }

    /**
     * Trouve une affectation par id.
     */
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

    /**
     * Retourne l'affectation active d'un véhicule (end_date = NULL).
     * @return Optional.empty() si aucune affectation active
     */
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

    /**
     * Liste toutes les affectations (les plus récentes en premier).
     */
    public List<Assignment> findAll() throws SQLException {
        String sql = "SELECT id, vehicle_id, agent_id, start_date, end_date FROM assignment ORDER BY start_date DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Assignment> out = new ArrayList<>();
            while (rs.next()) out.add(map(rs));
            return out;
        }
    }

    /**
     * Liste les affectations d'un agent.
     */
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

    /**
     * Liste les affectations d'un véhicule.
     */
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

    /**
     * Termine une affectation en mettant une end_date (seulement si elle était active).
     * @return true si la ligne a été mise à jour
     */
    public boolean endAssignment(long assignmentId, LocalDate endDate) throws SQLException {
        String sql = "UPDATE assignment SET end_date = ? WHERE id = ? AND end_date IS NULL";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(endDate));
            ps.setLong(2, assignmentId);
            return ps.executeUpdate() == 1;
        }
    }

    /**
     * Mapping SQL -> objet Assignment.
     * Gestion de end_date potentiellement NULL.
     */
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

    /**
     * Prépare des lignes prêtes pour l'UI (jointure assignment + agent + vehicle),
     * avec une recherche possible sur plaque / marque / modèle.
     */
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
                    // Conversion des dates SQL vers LocalDate
                    LocalDate start = rs.getDate("start_date").toLocalDate();
                    Date endSql = rs.getDate("end_date");
                    LocalDate end = (endSql == null) ? null : endSql.toLocalDate();

                    // Construction de champs "affichables" pour l'UI
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

    /**
     * Retourne l'affectation active d'un agent (end_date = NULL).
     */
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

    /**
     * Accepte une demande : passe de DEMANDEE -> EN_COURS.
     */
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

    /**
     * Refuse une demande : status = REFUSEE et on fixe la end_date.
     */
    public boolean refuseDemande(long assignmentId, LocalDate end) throws SQLException {
        String sql = """
        UPDATE assignment
        SET status = 'REFUSEE', end_date = ?
        WHERE id = ? AND status = 'DEMANDEE'
    """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(end));
            ps.setLong(2, assignmentId);
            return ps.executeUpdate() == 1;
        }
    }

    /**
     * Clôture une affectation en cours : EN_COURS -> CLOTUREE + end_date.
     */
    public boolean cloturerEnCours(long assignmentId, LocalDate end) throws SQLException {
        String sql = """
        UPDATE assignment
        SET status = 'CLOTUREE', end_date = ?
        WHERE id = ? AND status = 'EN_COURS'
    """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(end));
            ps.setLong(2, assignmentId);
            return ps.executeUpdate() == 1;
        }
    }

    // helper pour éviter de répéter out.add(map(rs)) dans deux méthodes
    private static void returnListAdd(List<Assignment> out, ResultSet rs) throws SQLException {
        out.add(map(rs));
    }
}
