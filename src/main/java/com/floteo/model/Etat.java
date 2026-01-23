package com.floteo.model;

/**
 * Représente un état possible (table "etat").
 * Utilisé pour qualifier l'état d'un véhicule.
 */
public record Etat(
        long id,
        String name
) { }
