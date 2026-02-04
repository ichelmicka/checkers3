package com.example.persistence;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Endpoint zwracający listę ruchów jako prosty tekst (każda linia: COLOR x y).
 * Ułatwia klientowi pobranie i odtworzenie sekwencji ruchów.
 */
@RestController
@RequestMapping("/api/games")
public class GameReplayController {
    private final GameRepository repo;

    public GameReplayController(GameRepository repo) {
        this.repo = repo;
    }

    // GET /api/games/{id}/raw
    @GetMapping(value = "/{id}/raw", produces = "text/plain; charset=UTF-8")
    public ResponseEntity<String> getRawMoves(@PathVariable Long id) {
        Optional<GameEntity> gop = repo.findWithMovesById(id);
        if (gop.isEmpty()) return ResponseEntity.notFound().build();

        GameEntity g = gop.get();
        List<MoveEntity> moves = g.getMoves();
        if (moves == null || moves.isEmpty()) return ResponseEntity.noContent().build();

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < moves.size(); i++) {
            MoveEntity m = moves.get(i);
            if (m == null) continue;
            Integer toRow = m.getToRow();
            Integer toCol = m.getToCol();
            if (toRow == null || toCol == null) continue;
            String color = ((i % 2) == 0) ? "BLACK" : "WHITE";
            sb.append(color).append(" ").append(toRow).append(" ").append(toCol).append("\n");
        }
        String out = sb.toString();
        if (out.isBlank()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(out);
    }

}