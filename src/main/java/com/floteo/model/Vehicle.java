package com.floteo.model;

import java.time.LocalDate;

public record Vehicle(
        long id,
        String plate,
        String type,
        String brand,
        String model,
        int mileage,
        LocalDate acquisitionDate,
        VehicleStatus status
) {}
