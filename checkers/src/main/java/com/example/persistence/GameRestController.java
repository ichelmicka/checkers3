package com.example.persistence;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Optional;

/**
 * REST API:
 * POST /api/games                -> create game (body: {"black":"A","white":"B"})
 * POST /api/games/{id}/moves     -> add move (body: {fromRow,fromCol,toRow,toCol,capture,extra,playerColor})
 * POST /api/games/{id}/finish    -> finish game (body: {"result":"BLACK_WIN"})
 * GET  /api/games/{id}/moves/raw -> raw moves (each line: COLOR x y)
 *
 * NOTE: endpoint path changed to "/{id}/moves/raw" to avoid mapping conflicts with other controllers.
 */
@RestController
@RequestMapping("/api/games")
public class GameRestController {
    private final GamePersistenceService service;

    public GameRestController(GamePersistenceService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<GameEntity> createGame(@RequestBody CreateGameDto dto) {
        GameEntity g = service.createGame(dto.black, dto.white);
        return ResponseEntity.created(URI.create("/api/games/" + g.getId())).body(g);
    }

    @PostMapping("/{id}/moves")
    public ResponseEntity<MoveEntity> addMove(@PathVariable Long id, @RequestBody MoveDto dto) {
        MoveEntity m = service.recordMove(id, dto.fromRow, dto.fromCol, dto.toRow, dto.toCol, dto.capture, dto.extra);
        return ResponseEntity.ok(m);
    }

    @PostMapping("/{id}/finish")
    public ResponseEntity<GameEntity> finish(@PathVariable Long id, @RequestBody FinishDto dto) {
        GameEntity g = service.finishGame(id, dto.result);
        return ResponseEntity.ok(g);
    }

    /**
     * Changed path to avoid conflicts: now GET /api/games/{id}/moves/raw
     * Returns text/plain where each line: "BLACK 3 3"
     */
    @GetMapping(value = "/{id}/moves/raw", produces = "text/plain; charset=UTF-8")
    public ResponseEntity<String> getRawMoves(@PathVariable Long id) {
        Optional<GameEntity> gop = service.getGameWithMoves(id);
        if (gop.isEmpty()) return ResponseEntity.notFound().build();

        GameEntity g = gop.get();
        StringBuilder sb = new StringBuilder();

        List<MoveEntity> moves = g.getMoves();
        if (moves == null || moves.isEmpty()) {
            // brak ruchów -> zwróć 204 No Content
            return ResponseEntity.noContent().build();
        }

        for (int i = 0; i < moves.size(); i++) {
            MoveEntity m = moves.get(i);
            if (m == null) continue;
            Integer toRow = m.getToRow();
            Integer toCol = m.getToCol();

            // pomin ruchy bez współrzędnych (być może są jakieś zapisy "meta")
            if (toRow == null || toCol == null) {
                continue;
            }

            // kolor na podstawie indeksu (0 -> BLACK, 1 -> WHITE, 2 -> BLACK, ...)
            String color = ((i % 2) == 0) ? "BLACK" : "WHITE";

            sb.append(color).append(" ").append(toRow).append(" ").append(toCol).append("\n");
        }

        String out = sb.toString();
        if (out.isBlank()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(out);
    }


    // DTOs
    public static class CreateGameDto { public String black; public String white; }
    public static class MoveDto {
        public int fromRow, fromCol, toRow, toCol;
        public boolean capture;
        public String extra; // e.g. {"playerId":"..."}
    }
    public static class FinishDto { public String result; }
}