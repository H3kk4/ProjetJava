package com.floteo.model;

/**
 * Représente un service de l'entreprise.
 */
public record Service(
        long id,
        String name
) {
    /**
     * Affichage lisible dans l'interface graphique
     */
    @Override public String toString() {
        return name;
    }
}
