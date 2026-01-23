package com.floteo.model;

/**
 * Représente un agent de l'entreprise.
 * Correspond à une ligne de la table "agent".
 */
public record Agent(
        long id,           // Identifiant unique en base
        String matricule,  // Matricule de l'agent
        String firstName,  // Prénom
        String lastName,   // Nom
        long serviceId     // Référence vers le service
) {}
