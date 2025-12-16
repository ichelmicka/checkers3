package com.example.model;

import com.example.model.*;
import com.example.model.*; 
import org.junit.jupiter.api.Test; 
import static org.junit.jupiter.api.Assertions.*;


class MoveTest {

    @Test
    void parseValidMove() {
        Move m = Move.parse("MOVE 3 4", "p1");
        assertNotNull(m);
        assertEquals(3, m.getX());
        assertEquals(4, m.getY());
        assertEquals("p1", m.getPlayerId());
    }

    @Test
    void parseInvalidCommand() {
        Move m = Move.parse("MOV 3 4", "p1");
        assertNull(m);
    }

    @Test
    void parseMissingArguments() {
        Move m = Move.parse("MOVE 3", "p1");
        assertNull(m);
    }

    @Test
    void parseNonNumeric() {
        Move m = Move.parse("MOVE a b", "p1");
        assertNull(m);
    }

    @Test
    void parseNegativeCoordinates() {
        Move m = Move.parse("MOVE -1 5", "p1");
        assertNotNull(m);
        assertEquals(-1, m.getX());
        assertEquals(5, m.getY());
    }
}
