package com.example.model;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Nadaje id i przypisuje kolor
 */
public class PlayerFactory {
    private final AtomicInteger counter = new AtomicInteger(0);

    public Player create(String name) {
        int idx = counter.getAndIncrement();
        String id = "p" + idx;
        Stone color = (idx % 2 == 0) ? Stone.BLACK : Stone.WHITE;
        return new Player(id, name, color);
    }
}
