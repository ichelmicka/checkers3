package com.example.rules;

import com.example.model.Board;
import com.example.model.Position;
import com.example.model.Stone;

import java.util.*;

/**
 * Oblicza terytorium i punkty (stones + territory) z wykryciem seki.
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

        // 1) znajdź wszystkie puste regiony i nadaj im ID
        // regionId[y][x] == -1 oznacza nieodwiedzony punkt pusty
        int[][] regionId = new int[size][size];
        for (int i = 0; i < size; i++) Arrays.fill(regionId[i], -1);
        Map<Integer, Region> regions = new HashMap<>();
        int nextRegionId = 0;

        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                // pomija zajęte pola
                if (board.get(x, y) != Stone.EMPTY) continue;
                // pomija przypisany region
                if (regionId[y][x] != -1) continue;

                // flood-fill, aby cały region zebrać
                Deque<Position> stack = new ArrayDeque<>();
                stack.push(new Position(x, y));
                regionId[y][x] = nextRegionId;
                Region reg = new Region(nextRegionId);

                while (!stack.isEmpty()) {
                    Position p = stack.pop();
                    reg.addPosition(p);
                    
                    // dla każdego sąsiada: jeśli pusty i nieodwiedzony -> dodaj do stosu,
                    // jeśli zajęty -> zarejestruj kolor graniczny regionu
                    List<Position> neigh = board.getNeighbours(p.x, p.y);
                    for (Position n : neigh) {
                        Stone ns = board.get(n.x, n.y);
                        if (ns == Stone.EMPTY) {
                            if (regionId[n.y][n.x] == -1) {
                                regionId[n.y][n.x] = nextRegionId;
                                stack.push(new Position(n.x, n.y));
                            }
                        } else {
                            // rejestrujemy, jakimi kolorami otoczony jest pusty region
                            reg.addBorderingColor(ns);
                        }
                    }
                }

                regions.put(nextRegionId, reg);
                nextRegionId++;
            }
        }

        // 2) znajdź wszystkie grupy kamieni i powiąż je z regionami pustek
        // seen[y][x] - czy kamień został już przypisany do jakiejś grupy
        boolean[][] seen = new boolean[size][size];
        Map<Integer, StoneGroup> groups = new HashMap<>();
        int nextGroupId = 0;

        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                Stone s = board.get(x, y);
                // ignorujemy puste pola i już przetworzone kamienie
                if (s == Stone.EMPTY || seen[y][x]) continue;

                Deque<Position> stack = new ArrayDeque<>();
                stack.push(new Position(x, y));
                StoneGroup g = new StoneGroup(nextGroupId, s);

                while (!stack.isEmpty()) {
                    Position p = stack.pop();
                    if (seen[p.y][p.x]) continue; // jeśli znów dodany
                    if (board.get(p.x, p.y) != s) continue; // sprawdzanie koloru

                    seen[p.y][p.x] = true;
                    g.addStone(p);

                    // przeglądamy sąsiadów: wolne punkty to "liberties" grupy,
                    // a jeśli sąsiednie pole należy do regionu pustego, zapamiętujemy jego ID
                    List<Position> neigh = board.getNeighbours(p.x, p.y);
                    for (Position n : neigh) {
                        Stone ns = board.get(n.x, n.y);
                        if (ns == Stone.EMPTY) {
                            g.addLiberty(n);
                            int rid = regionId[n.y][n.x];
                            if (rid != -1) g.addAdjacentRegionId(rid);
                        } else if (ns == s) {
                            // kamień tego samego koloru -> należą do tej samej grupy
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

        // 4) grupy w seki:
        // grupa jest w seki tylko wtedy, gdy ma co najmniej jedno adjacentRegionId
        // i wszystkie regiony z którymi się styka są neutralne.
        for (StoneGroup g : groups.values()) {
            if (g.adjacentRegionIds.isEmpty()) {
                // grupa nie graniczy z żadnym pustym regionem -> nie traktujemy jej jako seki
                g.isSeki = false;
                continue;
            }
            boolean allNeutral = true;
            for (Integer rid : g.adjacentRegionIds) {
                if (!neutralRegions.contains(rid)) {
                    allNeutral = false;
                    break;
                }
            }
            g.isSeki = allNeutral;
        }

        // 5) policz kamienie i przydziel terytorium (regiony z dokładnie 1 kolorem granicznym
        // i bez przylegających grup tego koloru oznaczonych jako seki)
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
            // jeśli region graniczy z więcej/0 kolorami, pomijamy
            if (r.borderingColors.size() != 1) continue;
            Stone owner = r.borderingColors.iterator().next();

            // pomijamy regiony należące do koloru, jeśli przylega do nich grupa tego koloru
            // będąca w seki (w seki nie przyznajemy terytorium)
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