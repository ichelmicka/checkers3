package com.example.rules;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Prosty detektor ko oparty na porównaniu String
 */
public class KoDetector {
    private final Deque<String> history = new ArrayDeque<>();
    private final int capacity;

    public KoDetector() { this(10); }
    public KoDetector(int capacity) { this.capacity = capacity; }

    public synchronized void push(String boardString) {
        history.addFirst(boardString);
        if (history.size() > capacity) history.removeLast();
    }

    public synchronized boolean isKo(String boardString) {
        return !history.isEmpty() && history.peekFirst().equals(boardString);
    }
}