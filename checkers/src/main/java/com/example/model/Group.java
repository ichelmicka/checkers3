package com.example.model;
import java.util.HashSet;
import java.util.Set;

public class Group {
    public final Stone color;
    public final Set<Position> stones = new HashSet<>();
    public final Set<Position> liberties = new HashSet<>();

    // czy grupa jest martwa 
    private boolean dead = false;

    public Group(Stone color) {
        this.color = color;
    }

    // Ustawianie statusu grupy 
    public void setDead(boolean dead) { 
        this.dead = dead; 
    } 
    
    // Pobieranie statusu grupy 
    public boolean isDead() {
        return dead; 
    }

}
