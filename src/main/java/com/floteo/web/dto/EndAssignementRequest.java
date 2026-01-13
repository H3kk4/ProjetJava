package com.floteo.web.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class EndAssignmentRequest {
    @NotNull public LocalDate endDate;
}
