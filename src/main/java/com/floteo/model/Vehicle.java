package com.floteo.model;

public record Vehicle(long id, String registration, String brand, String model, VehicleStatus status) {}
