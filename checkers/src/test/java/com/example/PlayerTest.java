package com.example.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlayerTest {

    @Test
    void addPrisoners() {
        Player p = new Player("id1", "Ala", Stone.BLACK);

        p.addPrisoners(3);
        p.addPrisoners(2);

        assertEquals(5, p.getPrisoners());
    }

    @Test
    void playerStoresDataCorrectly() {
        Player p = new Player("id1", "Ala", Stone.WHITE);

        assertEquals("id1", p.getId());
        assertEquals("Ala", p.getName());
        assertEquals(Stone.WHITE, p.getColor());
    }
}
