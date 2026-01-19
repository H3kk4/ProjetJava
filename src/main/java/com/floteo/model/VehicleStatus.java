package com.floteo.model;

public enum VehicleStatus {
    DISPONIBLE, AFFECTE, ENTRETIEN;
    public String toString() { return name().toLowerCase(); }
}
