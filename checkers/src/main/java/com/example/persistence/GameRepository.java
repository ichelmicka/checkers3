package com.example.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.Optional;

public interface GameRepository extends JpaRepository<GameEntity, Long> {
    @EntityGraph(attributePaths = {"moves"})
    Optional<GameEntity> findWithMovesById(Long id);
}