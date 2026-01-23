package com.floteo.model;

/**
 * Enumération des statuts possibles d'un véhicule.
 */
public enum VehicleStatus {
    DISPONIBLE,
    AFFECTE,
    ENTRETIEN;

    /**
     * Retourne une version lisible du statut (en minuscules),
     * pour l'affichage dans l'UI.
     */
    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
