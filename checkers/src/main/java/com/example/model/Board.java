package com.example.model;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

public final class Board implements Cloneable {
    private final int size;
    private Stone[][] cells;

    public Board(int size) {
        this.size = size;
        this.cells = new Stone[size][size];
        for (int i = 0; i<size; i++) {
            Arrays.fill(cells[i], Stone.EMPTY);
        }
    }

    public int getSize() {
        return size;
    }

    public void set(int x, int y, Stone s) {
        cells[y][x] = s;
    }

    public Stone get(int x, int y) {
        return cells[y][x];
    }

    public boolean isOnBoard(int x, int y) {
        return x>=0 && x<size && y>=0 && y<size;
    }

    public List<Position> getNeighbours(int x, int y) {
        List<Position> list = new ArrayList<>();
        if (isOnBoard(x-1, y)) list.add(new Position(x-1, y));
        if (isOnBoard(x+1, y)) list.add(new Position(x+1, y));
        if (isOnBoard(x, y-1)) list.add(new Position(x, y-1));
        if (isOnBoard(x, y+1)) list.add(new Position(x, y+1));
        return list;
    }

    //jesli sasiad jest otoczony usuwamy go
    public MoveResult placeStone(int x, int y, Stone color)
    {
        if (!isOnBoard(x, y)) {
            return MoveResult.error("Poza plansza");
        }
        if (get(x, y) != Stone.EMPTY) {
            return MoveResult.error("Pole zajete");
        }

        //postaw kamien
        set(x, y, color);

        // Lista zbitych kamieni
        java.util.List<Position> captures = new java.util.ArrayList<>();

        //sprawdz czterch sasiadow
        int[][] dirs = {{1,0}, {-1,0}, {0,1}, {0,-1}};

        for (int[] d : dirs) {
            int nx = x + d[0];
            int ny = y + d[1];
            if (!isOnBoard(nx, ny)) continue;

            Stone neighbour = get(nx, ny);

            if (neighbour != Stone.EMPTY && isSurrounded(nx, ny))
            {
                set(nx, ny, Stone.EMPTY);
                captures.add(new Position(nx, ny));
            }
        }
        return MoveResult.ok(captures, this.clone());
    }

    private boolean isSurrounded(int x, int y) {
        int[][] dirs = { {1,0}, {-1,0}, {0,1}, {0,-1} };

        for (int[] d : dirs) {
            int nx = x + d[0];
            int ny = y + d[1];

            if (!isOnBoard(nx, ny)) continue;

            // Jeśli sąsiad jest pusty - nie jest otoczony
            if (get(nx, ny) == Stone.EMPTY) return false;
        }

        return true;
    }


    @Override
    public Board clone() {
        try {
            Board copy = (Board) super.clone();

            // głęboka kopia tablicy
            copy.cells = new Stone[size][size];
            for (int i = 0; i < size; i++) {
                System.arraycopy(this.cells[i], 0, copy.cells[i], 0, size);
            }

            return copy;

        } catch (CloneNotSupportedException e) {
            throw new AssertionError("Clone not supported", e);
        }
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Stone[] row : cells) {
            for (Stone s : row) {
                sb.append(s == Stone.BLACK ? "B" :
                          s == Stone.WHITE ? "W" : ".");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
