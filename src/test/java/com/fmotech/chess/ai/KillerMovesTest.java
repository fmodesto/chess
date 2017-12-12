package com.fmotech.chess.ai;

import com.fmotech.chess.ai.KillerMoves;
import org.junit.Test;

import static org.junit.Assert.*;

public class KillerMovesTest {

    private KillerMoves killers = new KillerMoves();

    @Test
    public void testLow() {
        for (int i = 0; i < 64; i++) {
            killers.addKiller(i, i);
        }
        for (int i = 0; i < 64; i++) {
            assertEquals(i, killers.getPrimaryKiller(i));
        }
    }

    @Test
    public void testMiddle() {
        for (int i = 17; i < 64 + 17; i++) {
            killers.addKiller(i, i);
        }
        for (int i = 17; i < 64 + 17; i++) {
            assertEquals(i, killers.getPrimaryKiller(i));
        }
    }

    @Test
    public void testBothLow() {
        for (int i = 0; i < 64; i++) {
            killers.addKiller(i, 2 * i);
            killers.addKiller(i, i);
        }
        for (int i = 0; i < 64; i++) {
            assertEquals(i, killers.getPrimaryKiller(i));
            assertEquals(2 * i, killers.getSecundaryKiller(i));
        }
    }

    @Test
    public void testBothMiddle() {
        for (int i = 17; i < 64 + 17; i++) {
            killers.addKiller(i, 2 * i);
            killers.addKiller(i, i);
        }
        for (int i = 17; i < 64 + 17; i++) {
            assertEquals(i, killers.getPrimaryKiller(i));
            assertEquals(2 * i, killers.getSecundaryKiller(i));
        }
    }

    @Test
    public void testReplace() {
        for (int i = 17; i < 64 + 17; i++) {
            killers.addKiller(i, 2 * i);
            killers.addKiller(i, i);
            killers.addKiller(i, 3 * i);
        }
        for (int i = 17; i < 64 + 17; i++) {
            assertEquals(3 * i, killers.getPrimaryKiller(i));
            assertEquals(i, killers.getSecundaryKiller(i));
        }
    }

}