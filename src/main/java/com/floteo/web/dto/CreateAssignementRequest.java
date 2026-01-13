package com.floteo.web.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class CreateAssignmentRequest {
    @NotNull public Long vehicleId;
    @NotNull public Long agentId;
    @NotNull public LocalDate startDate;
    public LocalDate endDate; // optionnel
}
