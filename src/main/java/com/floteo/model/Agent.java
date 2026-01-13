package com.floteo.model;

public record Agent(
        long id,
        String matricule,
        String firstName,
        String lastName,
        long serviceId
) {}