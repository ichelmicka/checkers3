package com.example.model;

import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class MoveResultTest {

    @Test
    void errorFactory() {
        MoveResult r = MoveResult.error("Bad move");

        assertFalse(r.isOk());
        assertEquals("Bad move", r.getErrorMessage());
        assertTrue(r.getCaptures().isEmpty());
        assertNull(r.getBoardSnapshot());
    }

    @Test
    void okFactory() {
        Board b = new Board(5);
        MoveResult r = MoveResult.ok(Collections.emptyList(), b);

        assertTrue(r.isOk());
        assertEquals(b, r.getBoardSnapshot());
    }
}
