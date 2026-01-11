package com.example.rules;

import com.example.model.Board;
import com.example.model.Position;
import com.example.model.Stone;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Liczy terytorium oraz punkty (stones + territory).
 */
public final class TerritoryScorer {

    private TerritoryScorer() {}

    public static Score score(Board board) {
        int size = board.getSize();
        boolean[][] visited = new boolean[size][size];

        int blackStones = 0;
        int whiteStones = 0;
        int blackTerritory = 0;
        int whiteTerritory = 0;

        // policz kamienie
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                Stone s = board.get(x, y);
                if (s == Stone.BLACK) blackStones++;
                else if (s == Stone.WHITE) whiteStones++;
            }
        }

        // dla każdego nieodwiedzonego pustego pola - flood fill
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                if (visited[y][x]) continue;
                if (board.get(x, y) != Stone.EMPTY) continue;

                // flood-fill regionu pustych pól
                Deque<Position> stack = new ArrayDeque<>();
                stack.push(new Position(x, y));
                visited[y][x] = true;

                int regionSize = 0;
                Set<Stone> bordering = new HashSet<>();

                while (!stack.isEmpty()) {
                    Position p = stack.pop();
                    regionSize++;

                    List<Position> neighbours = board.getNeighbours(p.x, p.y);
                    for (Position n : neighbours) {
                        Stone ns = board.get(n.x, n.y);
                        if (ns == Stone.EMPTY) {
                            if (!visited[n.y][n.x]) {
                                visited[n.y][n.x] = true;
                                stack.push(new Position(n.x, n.y));
                            }
                        } else {
                            // kamień — dodaj kolor do zbioru granic
                            bordering.add(ns);
                        }
                    }
                }

                // analiza granic regionu
                if (bordering.size() == 1) {
                    Stone owner = bordering.iterator().next();
                    if (owner == Stone.BLACK) blackTerritory += regionSize;
                    else if (owner == Stone.WHITE) whiteTerritory += regionSize;
                }
                // jeśli bordering.size() == 0 lub >1 -> neutralne (nikt nie dostaje)
            }
        }

        int blackScore = blackStones + blackTerritory;
        int whiteScore = whiteStones + whiteTerritory;

        return new Score(blackStones, whiteStones, blackTerritory, whiteTerritory, blackScore, whiteScore);
    }

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