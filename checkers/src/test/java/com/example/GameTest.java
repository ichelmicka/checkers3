package com.example.game;

import com.example.game.*;
import com.example.model.*;
import com.example.model.*; 
import org.junit.jupiter.api.Test; 
import static org.junit.jupiter.api.Assertions.*;

class GameTest {

    @Test
    void joinTwoPlayers() {
        Game g = new Game(9);

        Player p1 = new Player("id1", "Ala", Stone.BLACK);
        Player p2 = new Player("id2", "Ola", Stone.WHITE);

        assertTrue(g.join(p1));
        assertTrue(g.join(p2));
        assertFalse(g.join(new Player("id3", "Ela", Stone.BLACK)));
    }

    @Test
    void gameStartsAfterTwoPlayers() {
        Game g = new Game(9);

        Player p1 = new Player("id1", "Ala", Stone.BLACK);
        Player p2 = new Player("id2", "Ola", Stone.WHITE);

        g.join(p1);
        assertEquals(GameState.WAITING, g.getState());

        g.join(p2);
        assertEquals(GameState.RUNNING, g.getState());
    }

    @Test
    void applyMoveBeforeStartShouldFail() {
        Game g = new Game(9);
        Player p1 = new Player("id1", "Ala", Stone.BLACK);
        g.join(p1);

        Move m = new Move(MoveType.MOVE, new Position(3, 3), "id1");

        MoveResult r = g.applyMove(m);

        assertFalse(r.isOk());
        assertEquals("Game not running", r.getErrorMessage());
    }

    @Test
    void applyMoveWrongTurnShouldFail() {
        Game g = new Game(9);

        Player p1 = new Player("id1", "Ala", Stone.BLACK);
        Player p2 = new Player("id2", "Ola", Stone.WHITE);

        g.join(p1);
        g.join(p2);

        Move m = new Move(MoveType.MOVE, new Position(3, 3), "id2"); // bialy proboje sie ruszyc pierwszy
        MoveResult r = g.applyMove(m);

        assertFalse(r.isOk());
        assertEquals("Not your turn", r.getErrorMessage());
    }

    @Test
    void applyValidMoveShouldSwitchTurn() {
        Game g = new Game(9);

        Player p1 = new Player("id1", "Ala", Stone.BLACK);
        Player p2 = new Player("id2", "Ola", Stone.WHITE);

        g.join(p1);
        g.join(p2);

        Move m = new Move(MoveType.MOVE, new Position(3, 3), "id1");
        MoveResult r = g.applyMove(m);

        assertTrue(r.isOk());
        assertEquals(Stone.WHITE, g.getCurrentTurn());
    }
}
