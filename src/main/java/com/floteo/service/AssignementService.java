package com.floteo.service;

import com.floteo.domain.*;
import com.floteo.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class AssignmentService {

    private final AssignmentRepository assignmentRepo;
    private final VehicleRepository vehicleRepo;
    private final AgentRepository agentRepo;

    public AssignmentService(AssignmentRepository assignmentRepo, VehicleRepository vehicleRepo, AgentRepository agentRepo) {
        this.assignmentRepo = assignmentRepo;
        this.vehicleRepo = vehicleRepo;
        this.agentRepo = agentRepo;
    }

    @Transactional
    public Assignment create(Long vehicleId, Long agentId, LocalDate start, LocalDate end) {
        Vehicle vehicle = vehicleRepo.findById(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException("Véhicule introuvable: " + vehicleId));
        Agent agent = agentRepo.findById(agentId)
                .orElseThrow(() -> new IllegalArgumentException("Agent introuvable: " + agentId));

        if (vehicle.getStatus() == VehicleStatus.ENTRETIEN) {
            throw new IllegalStateException("Impossible d'affecter un véhicule en entretien.");
        }

        LocalDate effectiveEnd = (end == null) ? LocalDate.of(9999, 12, 31) : end;
        if (effectiveEnd.isBefore(start)) {
            throw new IllegalArgumentException("endDate ne peut pas être avant startDate.");
        }

        // Pas de chevauchement avec d'autres affectations du véhicule
        List<Assignment> overlaps = assignmentRepo.findOverlaps(vehicleId, start, effectiveEnd);
        if (!overlaps.isEmpty()) {
            throw new IllegalStateException("Chevauchement d'affectation détecté pour ce véhicule.");
        }

        // Si affectation en cours (end null) => interdit
        if (end == null && !assignmentRepo.findActiveByVehicle(vehicleId).isEmpty()) {
            throw new IllegalStateException("Le véhicule a déjà une affectation en cours.");
        }

        Assignment a = new Assignment();
        a.setVehicle(vehicle);
        a.setAgent(agent);
        a.setStartDate(start);
        a.setEndDate(end);

        Assignment saved = assignmentRepo.save(a);

        // Met à jour statut véhicule (sauf entretien)
        if (vehicle.getStatus() != VehicleStatus.ENTRETIEN) {
            vehicle.setStatus(end == null ? VehicleStatus.AFFECTE : VehicleStatus.DISPONIBLE);
            vehicleRepo.save(vehicle);
        }

        return saved;
    }

    @Transactional
    public Assignment endAssignment(Long assignmentId, LocalDate endDate) {
        Assignment a = assignmentRepo.findById(assignmentId)
                .orElseThrow(() -> new IllegalArgumentException("Affectation introuvable: " + assignmentId));

        if (a.getEndDate() != null) {
            throw new IllegalStateException("Affectation déjà terminée.");
        }
        if (endDate.isBefore(a.getStartDate())) {
            throw new IllegalArgumentException("endDate ne peut pas être avant startDate.");
        }

        a.setEndDate(endDate);
        Assignment saved = assignmentRepo.save(a);

        Vehicle v = a.getVehicle();
        if (v.getStatus() != VehicleStatus.ENTRETIEN) {
            v.setStatus(VehicleStatus.DISPONIBLE);
            vehicleRepo.save(v);
        }
        return saved;
    }
}
