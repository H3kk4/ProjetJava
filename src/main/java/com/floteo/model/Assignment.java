package com.floteo.model;

import java.time.LocalDate;

/**
 * Représente une affectation d'un véhicule à un agent.
 * Une affectation peut être en cours (endDate = null) ou terminée.
 */
public record Assignment(
        long id,              // Identifiant unique
        long vehicleId,       // Véhicule concerné
        long agentId,         // Agent concerné
        LocalDate startDate,  // Date de début
        LocalDate endDate     // Date de fin
) {}
