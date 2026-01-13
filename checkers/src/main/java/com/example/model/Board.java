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

    //zwraca grupe, w ktorej jest dany kamien
    public Group getGroupAt(int x, int y) {
        GroupFinder finder = new GroupFinder(this);
        return finder.findGroup(x, y);
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

    public static Board fromString(String text) {
    String[] lines = text.split("\n");
    int size = lines.length;
    Board b = new Board(size);

    for (int y = 0; y < size; y++) {
        String row = lines[y].trim();
        for (int x = 0; x < size; x++) {
            char c = row.charAt(x);
            switch (c) {
                case 'B' -> b.set(x, y, Stone.BLACK);
                case 'W' -> b.set(x, y, Stone.WHITE);
                case '.' -> b.set(x, y, Stone.EMPTY);
                default -> throw new IllegalArgumentException("Unknown board char: " + c);
            }
        }
    }
    return b;
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
