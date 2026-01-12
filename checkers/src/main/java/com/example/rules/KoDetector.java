package com.example.rules;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Prosty detektor ko oparty na porównaniu String.
 * 
 * Przechowuje ostatnie N reprezentacji planszy i potrafi sprawdzić, czy dany
 * string powtarza ostatnie ustawienie.
 */
public class KoDetector {
    private final Deque<String> history = new ArrayDeque<>();
    private final int capacity;

    public KoDetector() { this(10); }
    public KoDetector(int capacity) { this.capacity = capacity; }

    /**
     * Dodaje nowy stan do historii.
     * 
     * @param boardString reprezentacja planszy
     */
    public synchronized void push(String boardString) {
        history.addFirst(boardString);
        if (history.size() > capacity) history.removeLast();
    }

    /**
     * Sprawdza, czy stan jest równy ostatniemu zapisanemu.
     * 
     * @param boardString
     * @return
     */
    public synchronized boolean isKo(String boardString) {
        return !history.isEmpty() && history.peekFirst().equals(boardString);
    }
}