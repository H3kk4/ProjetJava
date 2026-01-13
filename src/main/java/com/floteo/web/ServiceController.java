package com.floteo.web;

import com.floteo.domain.ServiceEntity;
import com.floteo.repository.ServiceRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/services")
public class ServiceController {

    private final ServiceRepository repo;
    public ServiceController(ServiceRepository repo) { this.repo = repo; }

    @GetMapping
    public List<ServiceEntity> all() { return repo.findAll(); }

    @PostMapping
    public ServiceEntity create(@RequestBody ServiceEntity s) { return repo.save(s); }
}
