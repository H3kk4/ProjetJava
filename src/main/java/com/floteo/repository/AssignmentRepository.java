package com.floteo.repository;

import com.floteo.domain.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

    @Query("""
    select a from Assignment a
    where a.vehicle.id = :vehicleId
      and (a.endDate is null or a.endDate >= :start)
      and a.startDate <= :end
  """)
    List<Assignment> findOverlaps(Long vehicleId, LocalDate start, LocalDate end);

    @Query("""
    select a from Assignment a
    where a.vehicle.id = :vehicleId and a.endDate is null
  """)
    List<Assignment> findActiveByVehicle(Long vehicleId);
}
