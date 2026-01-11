package com.example;

import org.junit.jupiter.api.Test;

import com.example.model.Board;
import com.example.model.Position;
import com.example.model.Stone;

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

}
