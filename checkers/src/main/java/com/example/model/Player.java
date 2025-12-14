package com.example.model;

public class Player {
    private final String id;
    private final String name;
    private final Stone color;
    private int prisoners;

    public Player(String id, String name, Stone color) {
        this.id = id;
        this.name = name;
        this.color = color;
        this.prisoners = 0;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public Stone getColor() { return color; }
    public int getPrisoners() { return prisoners; }
    public void addPrisoners(int n) { prisoners += n; }
}
