package com.floteo.model;

import java.time.LocalDate;

public record Assignment(long id, long vehicleId, long agentId, LocalDate startDate, LocalDate endDate) {}
