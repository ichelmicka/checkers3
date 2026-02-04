package com.example.persistence;

import java.util.Optional;

public interface GamePersistenceService {
    GameEntity createGame(String black, String white);
    MoveEntity recordMove(Long gameId, Integer fromRow, Integer fromCol, Integer toRow, Integer toCol, boolean capture, String extra);
    GameEntity finishGame(Long id, String result);
    Optional<GameEntity> getGameWithMoves(Long id);
}