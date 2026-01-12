package com.example.rules;
import com.example.model.*;

/**
 * Interfejs reguły gier.
 */
public interface Rules {
    /**
     * Próbuje zastosować ruch na planszy. 
     * 
     * @param board plansza
     * @param x kolumna
     * @param y rząd
     * @param color kolor kamienia
     * @return MoveResult opisujący rezultat
     */
    MoveResult applyMove(Board board, int x, int y, Stone color);
}
