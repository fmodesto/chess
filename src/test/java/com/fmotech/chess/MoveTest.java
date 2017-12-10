package com.fmotech.chess;

import org.junit.Test;

import static com.fmotech.chess.Board.PAWN;
import static com.fmotech.chess.Board.QUEEN;
import static com.fmotech.chess.MoveGenerator.KING_MASK;
import static org.junit.Assert.*;

public class MoveTest {

    @Test
    public void evalKing() {
        int move = Move.create(0, 0, KING_MASK, PAWN, 0);
        assertEquals(0, Move.evalCapture(move));
    }

    @Test
    public void evalLoose() {
        int move = Move.create(0, 0, QUEEN, PAWN, 0);
        assertEquals(-5, Move.evalCapture(move));
    }

    @Test
    public void evalWin() {
        int move = Move.create(0, 0, PAWN, QUEEN, 0);
        assertEquals(5, Move.evalCapture(move));
    }

    @Test
    public void evaNoCapture() {
        int move = Move.create(0, 0, PAWN, 0, 0);
        assertEquals(0, Move.evalCapture(move));
    }

}