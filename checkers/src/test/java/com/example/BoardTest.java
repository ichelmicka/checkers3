package com.example.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BoardTest {

    @Test
    void boardInitializesEmpty() {
        Board b = new Board(5);

        for (int x = 0; x < 5; x++) {
            for (int y = 0; y < 5; y++) {
                assertEquals(Stone.EMPTY, b.get(x, y));
            }
        }
    }

    @Test
    void neighboursCenter() {
        Board b = new Board(5);
        List<Position> n = b.getNeighbours(2, 2);

        assertEquals(4, n.size());
    }

    @Test
    void neighboursCorner() {
        Board b = new Board(5);
        List<Position> n = b.getNeighbours(0, 0);

        assertEquals(2, n.size());
    }

    @Test
    void placeStoneOnOccupiedShouldFail() {
        Board b = new Board(5);

        b.set(2, 2, Stone.BLACK);
        MoveResult r = b.placeStone(2, 2, Stone.WHITE);

        assertFalse(r.isOk());
    }



    @Test
    void surroundedSingleStoneShouldBeCaptured() {
        Board b = new Board(3);

        // ustawiamy białego kamienia
        b.set(1, 1, Stone.WHITE);

        // otaczamy go czarnymi z trzech stron
        b.set(0, 1, Stone.BLACK);
        b.set(2, 1, Stone.BLACK);
        b.set(1, 0, Stone.BLACK);

        // ostatni ruch czarnego zamyka oddechy białego
        MoveResult r = b.placeStone(1, 2, Stone.BLACK);

        assertTrue(r.isOk(), "Ruch powinien być poprawny");
        assertEquals(1, r.getCaptures().size(), "Powinien zostać zbity dokładnie 1 kamień");

        // sprawdzamy, że kamień został usunięty z planszy
        assertEquals(Stone.EMPTY, b.get(1, 1));
    }


}
