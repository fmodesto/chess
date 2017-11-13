package com.fmotech.chess;

import org.junit.Test;

import static com.fmotech.chess.BitOperations.lowestBitPosition;
import static com.fmotech.chess.DebugUtils.FEN;
import static com.fmotech.chess.DebugUtils.w;
import static org.junit.Assert.*;

public class MoveGeneratorTest {

    @Test
    public void shouldBeInCheck() {
        Board board = FenFormatter.fromFen("rnbqkbnr/ppp1pppp/3p4/8/Q7/2P5/PP1PPPPP/RNB1KBNR b KQkq - 0 1");
        assertTrue(MoveGenerator.isPositionInAttack(board, lowestBitPosition(board.ownKing())));
    }

    @Test
    public void shouldDetectPawnLastRowAttack() {
        Board board = FenFormatter.fromFen("rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 1 8");
        long[] moves = board.moves();
        MoveGenerator.generateDirtyMoves(board, moves);
        DebugUtils.debug(FEN, board);
        assertEquals(w("d7"), moves[10]);
        assertEquals(w("c8"), moves[11]);

        Board kill = board.move(w("d7"), w("c8"));
        assertFalse(MoveGenerator.isPositionInAttack(board, lowestBitPosition(board.ownKing())));
        assertFalse(MoveGenerator.isPositionInAttack(kill, lowestBitPosition(kill.ownKing())));
    }
}
