package com.example.model;

import java.util.Objects;

public class Position {
    public final int x;
    public final int y;

    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public boolean equals(Object o) {
        if (!(o instanceof Position)) return false;
        Position p = (Position) o;
        return p.x == x && p.y == y;
    }

    public int hashCode() {
        return Objects.hash(x, y);
    }
    
    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
