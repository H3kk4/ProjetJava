package com.floteo.service;

import com.floteo.dao.AgentDao;
import com.floteo.dao.AssignmentDao;
import com.floteo.dao.VehicleDao;
import com.floteo.model.VehicleStatus;

import java.sql.Connection;
import java.time.LocalDate;

/**
 * Couche "métier" : elle orchestre les règles fonctionnelles
 * en s'appuyant sur les DAO et sur une transaction SQL.
 */
public final class AssignmentService {
    private final Connection conn;
    private final AgentDao agentDao;
    private final VehicleDao vehicleDao;
    private final AssignmentDao assignmentDao;

    public AssignmentService(Connection conn, AgentDao agentDao, VehicleDao vehicleDao, AssignmentDao assignmentDao) {
        this.conn = conn;
        this.agentDao = agentDao;
        this.vehicleDao = vehicleDao;
        this.assignmentDao = assignmentDao;
    }

    /**
     * Affecte un véhicule à un agent à partir d'une date donnée.
     * - Vérifie l'existence du véhicule et de l'agent
     * - Empêche l'affectation si le véhicule est en entretien
     * - Empêche l'affectation s'il existe déjà une affectation active
     * - Effectue les modifications dans une transaction SQL
     */
    public void assignVehicle(long vehicleId, long agentId, LocalDate startDate) throws Exception {
        var vehicleOpt = vehicleDao.findById(vehicleId);
        if (vehicleOpt.isEmpty()) throw new IllegalArgumentException("Véhicule introuvable: " + vehicleId);

        var agentOpt = agentDao.findById(agentId);
        if (agentOpt.isEmpty()) throw new IllegalArgumentException("Agent introuvable: " + agentId);

        var vehicle = vehicleOpt.get();
        if (vehicle.status() == VehicleStatus.ENTRETIEN) {
            throw new IllegalStateException("Véhicule en entretien, impossible d'affecter.");
        }
        if (assignmentDao.findActiveByVehicle(vehicleId).isPresent()) {
            throw new IllegalStateException("Véhicule déjà affecté (affectation en cours).");
        }

        conn.setAutoCommit(false);
        try {
            assignmentDao.create(vehicleId, agentId, startDate, null);
            vehicleDao.updateStatus(vehicleId, VehicleStatus.AFFECTE);
            conn.commit();
        } catch (Exception e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    /**
     * Termine l'affectation active d'un véhicule
     * et remet le véhicule en état DISPONIBLE.
     */
    public void endActiveAssignmentForVehicle(long vehicleId, LocalDate endDate) throws Exception {
        var active = assignmentDao.findActiveByVehicle(vehicleId)
                .orElseThrow(() -> new IllegalStateException("Aucune affectation en cours pour ce véhicule."));

        conn.setAutoCommit(false);
        try {
            assignmentDao.endAssignment(active.id(), endDate);
            vehicleDao.updateStatus(vehicleId, VehicleStatus.DISPONIBLE);
            conn.commit();
        } catch (Exception e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }
}
