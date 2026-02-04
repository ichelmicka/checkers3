package com.example.persistence;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GameRepository extends JpaRepository<GameEntity, Long> {
    @EntityGraph(attributePaths = {"moves"})
    Optional<GameEntity> findWithMovesById(Long id);
}
