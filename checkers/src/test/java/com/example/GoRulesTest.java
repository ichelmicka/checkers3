package com.example;
import com.example.model.*; 
import com.example.rules.*;
import org.junit.jupiter.api.Test; 
import static org.junit.jupiter.api.Assertions.*;


public class GoRulesTest {
    @Test
    void placeStoneOnOccupiedShouldFail() {
        Board b = new Board(5);
        Rules rules = new GoRules();

        b.set(2, 2, Stone.BLACK);
        
        MoveResult r = rules.applyMove(b, 2, 2, Stone.WHITE);

        assertFalse(r.isOk());
        assertEquals("Pole zajete", r.getErrorMessage());
    }



    @Test
    void surroundedSingleStoneShouldBeCaptured() {
        Board b = new Board(3);
        Rules rules = new GoRules();
        // ustawiamy białego kamienia
        b.set(1, 1, Stone.WHITE);

        // otaczamy go czarnymi z trzech stron
        b.set(0, 1, Stone.BLACK);
        b.set(2, 1, Stone.BLACK);
        b.set(1, 0, Stone.BLACK);

        // ostatni ruch czarnego zamyka oddechy białego
        //MoveResult r = b.placeStone(1, 2, Stone.BLACK);
        MoveResult r = rules.applyMove(b, 1, 2, Stone.BLACK);


        assertTrue(r.isOk(), "Ruch powinien być poprawny");
        assertEquals(1, r.getCaptures().size(), "Powinien zostać zbity dokładnie 1 kamień");

        // sprawdzamy, że kamień został usunięty z planszy
        Board after = r.getBoardSnapshot();
        assertEquals(Stone.EMPTY, after.get(1, 1));
    }

    //samobojstwo, ktore zbija piony przeciwnika
    @Test
    void suicideThatCapturesIsLegal() {
        Board b = new Board(3);
        Rules rules = new GoRules();

        // białe w środku
        b.set(1, 1, Stone.WHITE);

        // czarne otaczają, ale mają 1 oddech
        b.set(0, 1, Stone.BLACK);
        b.set(2, 1, Stone.BLACK);
        b.set(1, 0, Stone.BLACK);

        // czarne grają samobójstwo, ale biją białego
        MoveResult r = rules.applyMove(b, 1, 2, Stone.BLACK);

        assertTrue(r.isOk());
        assertEquals(1, r.getCaptures().size());

        Board after = r.getBoardSnapshot();
        assertEquals(Stone.EMPTY, after.get(1, 1));
    }


    @Test
    void suicideMoveShouldFail() {
        Board b = new Board(3);
        Rules rules = new GoRules();

        // otaczamy punkt (1,1)
        b.set(0, 1, Stone.BLACK);
        b.set(2, 1, Stone.BLACK);
        b.set(1, 0, Stone.BLACK);
        b.set(1, 2, Stone.BLACK);

        MoveResult r = rules.applyMove(b, 1, 1, Stone.WHITE);

        assertFalse(r.isOk());
        assertEquals("Samobojstwo", r.getErrorMessage());
    }

    @Test
    void capturingLargerGroupShouldWork() {
        Board b = new Board(5);
        Rules rules = new GoRules();

        // grupa 2 białych
        b.set(2, 2, Stone.WHITE);
        b.set(2, 3, Stone.WHITE);

        // czarne otaczają
        b.set(1, 2, Stone.BLACK);
        b.set(3, 2, Stone.BLACK);
        b.set(1, 3, Stone.BLACK);
        b.set(3, 3, Stone.BLACK);
        b.set(2, 1, Stone.BLACK);

        MoveResult r = rules.applyMove(b, 2, 4, Stone.BLACK);
        System.out.println(b);
        assertEquals(2, r.getCaptures().size());

        Board after = r.getBoardSnapshot();
        assertEquals(Stone.EMPTY, after.get(2, 2));
        assertEquals(Stone.EMPTY, after.get(2, 3));
    }


}
