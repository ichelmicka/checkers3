package com.example;

import org.junit.jupiter.api.Test;

import com.example.rules.KoDetector;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testy dla detektora ko (KoDetector).
 */
public class KoDetectorTest {

    @Test
    public void testSimpleKoSequence() {
        KoDetector kd = new KoDetector(); // domyślna pojemność

        String pos0 = "POS0"; // symulowana reprezentacja planszy przed ruchem
        String pos1 = "POS1"; // symulowana reprezentacja planszy po ruchu

        // pierwsze sprawdzenie: pozycja po ruchu nie występuje w historii
        assertFalse(kd.isKo(pos1), "Puste history -> nie jest ko");

        // po wykonaniu ruchu serwer powinien dodać poprzednią pozycję do historii:
        kd.push(pos0);

        // teraz, jeśli ktoś spróbuje wykonać ruch, który przywróci pos0, to detektor powinien zgłosić ko
        assertTrue(kd.isKo(pos0), "Powrót do POS0 powinien być rozpoznany jako ko");

        // a pos1 nie jest ko (chyba, że zostało dodane)
        assertFalse(kd.isKo(pos1), "POS1 nie powinno być ko, jeśli nie ma go w historii");
    }

    @Test
    public void testCapacityEviction() {
        // ustawiam małą pojemność
        KoDetector kd = new KoDetector(3);

        kd.push("A"); // history: A
        kd.push("B"); // history: B, A
        kd.push("C"); // history: C, B, A

        // najnowszy to C
        assertTrue(kd.isKo("C"));
        assertFalse(kd.isKo("B"));
        assertFalse(kd.isKo("A"));

        kd.push("D"); // history: D, C, B  (A zostało wyrzucone)

        assertTrue(kd.isKo("D"));
        assertFalse(kd.isKo("C")); // C jest nadal w historii jako drugi element? isKo porównuje tylko z najnowszym
        assertFalse(kd.isKo("A"), "A powinno zostać wyrzucone z historii i nie być wykrywalne jako ko");
    }
}