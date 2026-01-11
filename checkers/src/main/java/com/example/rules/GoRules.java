package com.example.rules;

import com.example.model.*;

import java.util.ArrayList;
import java.util.List;

public class GoRules implements Rules {

    private final KoDetector koDetector = new KoDetector(); 

    @Override
    public MoveResult applyMove(Board board, int x, int y, Stone color) {

        //sprawdzenie legalności pola
        if (!board.isOnBoard(x, y)) {
            return MoveResult.error("Poza plansza");
        }
        if (board.get(x, y) != Stone.EMPTY) {
            return MoveResult.error("Pole zajete");
        }

        //pracujemy na kopii planszy
        Board copy = board.clone();
        copy.set(x, y, color);

        GroupFinder gf = new GroupFinder(copy);

        //bicie grup przeciwnika
        List<Position> captures = new ArrayList<>();

        for (Position n : copy.getNeighbours(x, y)) {
            Stone s = copy.get(n.x, n.y);
            if (s != Stone.EMPTY && s != color) {
                Group g = gf.findGroup(n.x, n.y);
                if (g.liberties.isEmpty()) {
                    // zbij całą grupę
                    for (Position p : g.stones) {
                        copy.set(p.x, p.y, Stone.EMPTY);
                        captures.add(p);
                    }
                }
            }
        }

        //sprawdzenie samobójstwa
        Group myGroup = gf.findGroup(x, y);
        if (myGroup.liberties.isEmpty() && captures.isEmpty()) {
            return MoveResult.error("Samobojstwo");
        }

        // sprawdzenie reguły ko
        String hashAfter = copy.toString();
        if (koDetector.isKo(hashAfter)) {
            return MoveResult.error("Ko - niedozwolone powtórzenie pozycji");
        }

        // dodanie do historii
        koDetector.push(board.toString());

        //zwróć wynik
        return MoveResult.ok(captures, copy);
    }
}
