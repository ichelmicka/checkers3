package com.example.rules;

import com.example.model.Position;
import com.example.model.Stone;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Reprezentuje pusty region (zbiór pól) oraz kolory kamieni, które go ograniczają.
 */
public final class Region {
    /** Unikalne id regionu. */
    public final int id;
    /** Pola (pozycje) należące do regionu pustek. */
    public final List<Position> positions = new ArrayList<>();
    /** Kolory kamieni otaczających region. */
    public final Set<Stone> borderingColors = EnumSet.noneOf(Stone.class);

    public Region(int id) {
        this.id = id;
    }

    /** Dodaje pozycję do regionu. */
    public void addPosition(Position p) {
        positions.add(p);
    }

    /** Dodaje kolor granicy (kamienia) — użyteczne podczas flood-fill. */
    public void addBorderingColor(Stone s) {
        borderingColors.add(s);
    }

    /** Rozmiar regionu w polach pustych. */
    public int size() { return positions.size(); }
}