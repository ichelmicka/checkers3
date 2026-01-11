package com.example.rules;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Klasa wykrywająca sytuację ko — niedozwolone powtórzenie poprzedniego stanu planszy.
 * Przechowuje ostatnie N hashy (domyślnie 10).
 */
public class KoDetector {
    private final Deque<String> history = new ArrayDeque<>();
    private final int capacity;

    public KoDetector() {
        this(10);
    }

    public KoDetector(int capacity) {
        this.capacity = capacity;
    }

    /** Zapamiętuje nowy stan planszy */
    public synchronized void push(String boardHash) {
        history.addFirst(boardHash);
        if (history.size() > capacity) {
            history.removeLast();
        }
    }

    /** Sprawdza, czy dany hash był już niedawno (czyli czy mamy ko) */
    public synchronized boolean isKo(String boardHash) {
        // nie można odtworzyć dokładnie poprzedniego stanu
        return !history.isEmpty() && history.peekFirst().equals(boardHash);
    }
}