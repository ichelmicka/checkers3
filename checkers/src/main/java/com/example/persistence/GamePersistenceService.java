package com.example.persistence;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class GamePersistenceService {
    private final GameRepository repo;

    public GamePersistenceService(GameRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public GameEntity createGame(String blackPlayer, String whitePlayer) {
        return repo.save(new GameEntity(blackPlayer, whitePlayer));
    }

    @Transactional
    public MoveEntity recordMove(Long gameId, int fromRow, int fromCol, int toRow, int toCol, boolean capture, String extra) {
        GameEntity game = repo.findById(gameId).orElseThrow(() -> new IllegalArgumentException("Game not found: " + gameId));
        int next = game.getMoves().size() + 1;
        MoveEntity m = new MoveEntity(next, fromRow, fromCol, toRow, toCol, capture, extra);
        game.addMove(m);
        repo.save(game);
        return m;
    }

    @Transactional
    public GameEntity finishGame(Long gameId, String result) {
        GameEntity game = repo.findById(gameId).orElseThrow(() -> new IllegalArgumentException("Game not found: " + gameId));
        game.setFinishedAt(java.time.Instant.now());
        game.setResult(result);
        return repo.save(game);
    }

    @Transactional(readOnly = true)
    public Optional<GameEntity> getGameWithMoves(Long id) {
        return repo.findWithMovesById(id);
    }
}