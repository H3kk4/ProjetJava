package com.floteo.model;

import java.time.LocalDate;

/**
 * Représente un véhicule du parc.
 * Correspond à une ligne de la table "vehicle".
 */
public record Vehicle(
        long id,                     // Identifiant unique
        String plate,                // Plaque d'immatriculation
        String type,                 // Type
        String brand,                // Marque
        String model,                // Modèle
        int mileage,                 // Kilométrage
        LocalDate acquisitionDate,   // Date d'acquisition
        VehicleStatus status,        // Statut du véhicule
        long etat                    // Référence vers l'état
) {}
