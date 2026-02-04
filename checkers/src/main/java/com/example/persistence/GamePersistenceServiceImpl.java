package com.example.persistence;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class GamePersistenceServiceImpl implements GamePersistenceService {
    private final GameRepository gameRepository;
    private final MoveRepository moveRepository;

    public GamePersistenceServiceImpl(GameRepository gameRepository, MoveRepository moveRepository) {
        this.gameRepository = gameRepository;
        this.moveRepository = moveRepository;
    }

    @Override
    @Transactional
    public GameEntity createGame(String black, String white) {
        GameEntity g = new GameEntity();
        g.setBlack(black);
        g.setWhite(white);
        return gameRepository.save(g);
    }

    @Override
    @Transactional
    public MoveEntity recordMove(Long gameId, Integer fromRow, Integer fromCol, Integer toRow, Integer toCol, boolean capture, String extra) {
        System.out.println("[PERSISTENCE] recordMove called for gameId=" + gameId + " to=(" + toRow + "," + toCol + ") capture=" + capture + " extra=" + extra);
        GameEntity g = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found: " + gameId));

        MoveEntity m = new MoveEntity();
        m.setFromRow(fromRow);
        m.setFromCol(fromCol);
        m.setToRow(toRow);
        m.setToCol(toCol);
        m.setCapture(capture);
        m.setExtra(extra == null ? "" : extra);

        int nextNumber = g.getMoves() == null ? 1 : g.getMoves().size() + 1;
        m.setMoveNumber(nextNumber);

        g.addMove(m);
        MoveEntity saved = moveRepository.save(m);
        System.out.println("[PERSISTENCE] move saved id=" + saved.getId());
        gameRepository.save(g);
        System.out.println("[PERSISTENCE] game saved id=" + g.getId() + " movesCount=" + g.getMoves().size());
        return saved;
    }


    @Override
    @Transactional
    public GameEntity finishGame(Long id, String result) {
        GameEntity g = gameRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Game not found: " + id));
        g.setResult(result);
        return gameRepository.save(g);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<GameEntity> getGameWithMoves(Long id) {
        return gameRepository.findWithMovesById(id);
    }
}