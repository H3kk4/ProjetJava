package com.floteo.web;

import com.floteo.domain.Agent;
import com.floteo.repository.AgentRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/agents")
public class AgentController {

    private final AgentRepository repo;
    public AgentController(AgentRepository repo) { this.repo = repo; }

    @GetMapping
    public List<Agent> all() { return repo.findAll(); }

    @PostMapping
    public Agent create(@RequestBody Agent a) { return repo.save(a); }
}
