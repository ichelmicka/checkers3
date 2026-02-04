package com.example.persistence;

import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/games")
public class GameRestController {
    private final GameRepository repo;

    public GameRestController(GameRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<GameEntity> all() {
        return repo.findAll();
    }

    @GetMapping("/{id}")
    public Optional<GameEntity> one(@PathVariable Long id) {
        return repo.findWithMovesById(id);
    }
}
