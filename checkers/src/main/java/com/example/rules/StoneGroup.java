package com.example.rules;

import com.example.model.Position;
import com.example.model.Stone;

import java.util.HashSet;
import java.util.Set;

/**
 * Reprezentuje grupę kamieni jednego koloru wraz z jej oddechami
 * oraz informacją o tym, z którymi regionami pustek się styka.
 *
 * Pole {@code isSeki} jest ustawiane gdy grupa styka się z regionem neutralnym.
 */
public final class StoneGroup {
    /** Lokalne id grupy. */
    public final int id;
    /** Kolor kamieni w grupie. */
    public final Stone color;
    /** Pozycje kamieni należących do grupy. */
    public final Set<Position> stones = new HashSet<>();
    /** Zbiory oddechów (pozycje puste). */
    public final Set<Position> liberties = new HashSet<>();
    /** Identyfikatory regionów pustek, z którymi grupa się styka. */
    public final Set<Integer> adjacentRegionIds = new HashSet<>();
    /** Flaga seki (ustawiana przez TerritoryScorer). */
    public boolean isSeki = false;

    public StoneGroup(int id, Stone color) {
        this.id = id;
        this.color = color;
    }

    public void addStone(Position p) { stones.add(p); }
    public void addLiberty(Position p) { liberties.add(p); }
    public void addAdjacentRegionId(int rid) { adjacentRegionIds.add(rid); }
    public void markSeki() { isSeki = true; }
}