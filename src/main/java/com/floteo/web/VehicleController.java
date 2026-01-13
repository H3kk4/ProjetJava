package com.floteo.web;

import com.floteo.domain.Vehicle;
import com.floteo.repository.VehicleRepository;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/vehicles")
public class VehicleController {

    private final VehicleRepository repo;
    public VehicleController(VehicleRepository repo) { this.repo = repo; }

    @GetMapping
    public List<Vehicle> all() { return repo.findAll(); }

    @PostMapping
    public Vehicle create(@RequestBody @Valid Vehicle v) { return repo.save(v); }

    @GetMapping("/{id}")
    public Vehicle one(@PathVariable Long id) {
        return repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Vehicle not found"));
    }
}
