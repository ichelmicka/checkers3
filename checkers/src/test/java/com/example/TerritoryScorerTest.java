package com.example;

import com.example.model.Board;
import com.example.model.Stone;
import com.example.rules.TerritoryScorer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testy warunków terytorium dla TerritoryScorer.
 */
public class TerritoryScorerTest {

    /**
     * Test: 5x5 z czarną obwódką — środek 3x3 powinien być terytorium czarnych (9 punktów).
     */
    @Test
    public void testBlackEnclosure5x5() {
        int n = 5;
        Board b = new Board(n);

        // ustaw czarną obwódkę
        for (int x = 0; x < n; x++) {
            b.set(x, 0, Stone.BLACK);
            b.set(x, n - 1, Stone.BLACK);
        }
        for (int y = 1; y < n - 1; y++) {
            b.set(0, y, Stone.BLACK);
            b.set(n - 1, y, Stone.BLACK);
        }

        TerritoryScorer.Score s = TerritoryScorer.score(b);

        // oczekujemy 16 czarnych kamieni (obwódka) i 9 pól terytorium (3x3 środek)
        assertEquals(16, s.blackStones, "Black stones count");
        assertEquals(9, s.blackTerritory, "Black territory (3x3 inside)");
        // biali nic
        assertEquals(0, s.whiteStones);
        assertEquals(0, s.whiteTerritory);
    }

    /**
     * Test: mały przypadek - 3x3 z czarną obwódką -> środkowe pole jest 1 punktem terytorium.
     */
    @Test
    public void testBlackEnclosure3x3_singlePoint() {
        int n = 3;
        Board b = new Board(n);

        // obwódka czarna
        for (int x = 0; x < n; x++) {
            b.set(x, 0, Stone.BLACK);
            b.set(x, n - 1, Stone.BLACK);
        }
        for (int y = 1; y < n - 1; y++) {
            b.set(0, y, Stone.BLACK);
            b.set(n - 1, y, Stone.BLACK);
        }

        TerritoryScorer.Score s = TerritoryScorer.score(b);

        assertEquals(8, s.blackStones, "Black stones 3x3 border");
        assertEquals(1, s.blackTerritory, "Black territory center");
    }

    /**
     * Test neutralnego regionu: trzy kolumny, po lewej same czarne, po prawej same biale,
     * środkowa kolumna pusta -> neutralne, terytorium obu = 0
     */
    @Test
    public void testNeutralColumnIsNotTerritory() {
        int n = 3;
        Board b = new Board(n);

        // lewa kolumna czarne
        for (int y = 0; y < n; y++) b.set(0, y, Stone.BLACK);
        // prawa kolumna biale
        for (int y = 0; y < n; y++) b.set(2, y, Stone.WHITE);
        // środkowa (x=1) pozostaje pusta

        TerritoryScorer.Score s = TerritoryScorer.score(b);

        assertEquals(3, s.blackStones);
        assertEquals(3, s.whiteStones);
        // środkowa kolumna graniczy z oboma -> neutralna -> territory 0
        assertEquals(0, s.blackTerritory, "Black territory should be 0 (neutral)");
        assertEquals(0, s.whiteTerritory, "White territory should be 0 (neutral)");
    }
}