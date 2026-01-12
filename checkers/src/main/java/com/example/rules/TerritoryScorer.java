package com.example.rules;

import com.example.model.Board;
import com.example.model.Position;
import com.example.model.Stone;

import java.util.*;

/**
 * Oblicza terytorium i punkty (stones + territory) z wykryciem seki.
 *
 * Algorytm:
 * 1) flood-fill pustych regionów -> zapamiętujemy pozycje oraz kolory kamieni, które je ograniczają,
 * 2) flood-fill grup kamieni -> zbieramy kamienie, ich oddechy i które regiony pustek stykają,
 * 3) regiony graniczące z >1 kolorem są neutralne,
 * 4) grupa jest w seki, jeśli styka się z regionem neutralnym,
 * 5) terytorium przyznajemy tylko regionom, które:
 *      - mają dokładnie jeden kolor graniczący,
 *      - żadna z graniczących z nimi grup tego koloru nie jest w seki.
 */
public final class TerritoryScorer {

    private TerritoryScorer() {}

    /**
     * Oblicza wynik dla danej planszy.
     *
     * @param board plansza do oceny
     * @return wynik scoringu
     */
    public static Score score(Board board) {
        int size = board.getSize();

        // 1) znajduje wszystkie puste regiony i nadaje im ID
        int[][] regionId = new int[size][size];
        for (int i = 0; i < size; i++) Arrays.fill(regionId[i], -1);
        Map<Integer, Region> regions = new HashMap<>();
        int nextRegionId = 0;

        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                if (board.get(x, y) != Stone.EMPTY) continue;
                if (regionId[y][x] != -1) continue;

                Deque<Position> stack = new ArrayDeque<>();
                stack.push(new Position(x, y));
                regionId[y][x] = nextRegionId;
                Region reg = new Region(nextRegionId);

                while (!stack.isEmpty()) {
                    Position p = stack.pop();
                    reg.addPosition(p);

                    List<Position> neigh = board.getNeighbours(p.x, p.y);
                    for (Position n : neigh) {
                        Stone ns = board.get(n.x, n.y);
                        if (ns == Stone.EMPTY) {
                            if (regionId[n.y][n.x] == -1) {
                                regionId[n.y][n.x] = nextRegionId;
                                stack.push(new Position(n.x, n.y));
                            }
                        } else {
                            reg.addBorderingColor(ns);
                        }
                    }
                }

                regions.put(nextRegionId, reg);
                nextRegionId++;
            }
        }

        // 2) znajduje grupy kamieni i powiązuje z regionami pustek
        boolean[][] seen = new boolean[size][size];
        Map<Integer, StoneGroup> groups = new HashMap<>();
        int nextGroupId = 0;

        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                Stone s = board.get(x, y);
                if (s == Stone.EMPTY || seen[y][x]) continue;

                Deque<Position> stack = new ArrayDeque<>();
                stack.push(new Position(x, y));
                StoneGroup g = new StoneGroup(nextGroupId, s);

                while (!stack.isEmpty()) {
                    Position p = stack.pop();
                    if (seen[p.y][p.x]) continue;
                    if (board.get(p.x, p.y) != s) continue;

                    seen[p.y][p.x] = true;
                    g.addStone(p);

                    List<Position> neigh = board.getNeighbours(p.x, p.y);
                    for (Position n : neigh) {
                        Stone ns = board.get(n.x, n.y);
                        if (ns == Stone.EMPTY) {
                            g.addLiberty(n);
                            int rid = regionId[n.y][n.x];
                            if (rid != -1) g.addAdjacentRegionId(rid);
                        } else if (ns == s) {
                            if (!seen[n.y][n.x]) stack.push(n);
                        }
                    }
                }

                groups.put(nextGroupId, g);
                nextGroupId++;
            }
        }

        // 3) regiony neutralne (granica z !=1 kolorami)
        Set<Integer> neutralRegions = new HashSet<>();
        for (Region r : regions.values()) {
            if (r.borderingColors.size() != 1) neutralRegions.add(r.id);
        }

        // 4) grupy w seki -> kontakt z regionem neutralnym
        for (StoneGroup g : groups.values()) {
            for (Integer rid : g.adjacentRegionIds) {
                if (neutralRegions.contains(rid)) {
                    g.markSeki();
                    break;
                }
            }
        }

        // 5) liczy kamienie i przydziela terytorium
        int blackStones = 0;
        int whiteStones = 0;
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                Stone st = board.get(x, y);
                if (st == Stone.BLACK) blackStones++;
                else if (st == Stone.WHITE) whiteStones++;
            }
        }

        int blackTerritory = 0;
        int whiteTerritory = 0;

        for (Region r : regions.values()) {
            if (r.borderingColors.size() != 1) continue;
            Stone owner = r.borderingColors.iterator().next();

            boolean borderingGroupInSeki = false;
            for (StoneGroup g : groups.values()) {
                if (g.color != owner) continue;
                if (g.adjacentRegionIds.contains(r.id) && g.isSeki) {
                    borderingGroupInSeki = true;
                    break;
                }
            }
            if (borderingGroupInSeki) continue;

            if (owner == Stone.BLACK) blackTerritory += r.size();
            else if (owner == Stone.WHITE) whiteTerritory += r.size();
        }

        int blackScore = blackStones + blackTerritory;
        int whiteScore = whiteStones + whiteTerritory;

        return new Score(blackStones, whiteStones, blackTerritory, whiteTerritory, blackScore, whiteScore);
    }

    /**
     * Wynik scoringu (kamienie, terytorium, suma).
     */
    public static final class Score {
        public final int blackStones;
        public final int whiteStones;
        public final int blackTerritory;
        public final int whiteTerritory;
        public final int blackScore;
        public final int whiteScore;

        public Score(int blackStones, int whiteStones, int blackTerritory, int whiteTerritory, int blackScore, int whiteScore) {
            this.blackStones = blackStones;
            this.whiteStones = whiteStones;
            this.blackTerritory = blackTerritory;
            this.whiteTerritory = whiteTerritory;
            this.blackScore = blackScore;
            this.whiteScore = whiteScore;
        }

        @Override
        public String toString() {
            return "Score{" +
                    "blackStones=" + blackStones +
                    ", whiteStones=" + whiteStones +
                    ", blackTerritory=" + blackTerritory +
                    ", whiteTerritory=" + whiteTerritory +
                    ", blackScore=" + blackScore +
                    ", whiteScore=" + whiteScore +
                    '}';
        }
    }
}