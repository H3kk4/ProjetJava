package com.floteo.web;

import com.floteo.domain.Assignment;
import com.floteo.service.AssignmentService;
import com.floteo.web.dto.CreateAssignmentRequest;
import com.floteo.web.dto.EndAssignmentRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/assignments")
public class AssignmentController {

    private final AssignmentService service;
    public AssignmentController(AssignmentService service) { this.service = service; }

    @PostMapping
    public Assignment create(@RequestBody @Valid CreateAssignmentRequest req) {
        return service.create(req.vehicleId, req.agentId, req.startDate, req.endDate);
    }

    @PatchMapping("/{id}/end")
    public Assignment end(@PathVariable Long id, @RequestBody @Valid EndAssignmentRequest req) {
        return service.endAssignment(id, req.endDate);
    }
}
