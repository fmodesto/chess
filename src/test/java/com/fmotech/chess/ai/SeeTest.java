package com.fmotech.chess.ai;

import com.fmotech.chess.Board;
import com.fmotech.chess.DebugUtils;
import com.fmotech.chess.FenFormatter;
import com.fmotech.chess.SanFormatter;
import com.fmotech.chess.ai.See;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicLong;

import static org.apache.commons.lang3.StringUtils.substringAfterLast;
import static org.apache.commons.lang3.StringUtils.substringBefore;
import static org.apache.commons.lang3.StringUtils.substringBetween;
import static org.apache.commons.lang3.StringUtils.trim;
import static org.junit.Assert.assertEquals;

public class SeeTest {

    @Test
    public void chessProgrammingExample1() {
        Board board = Board.fen("1k1r4/1pp4p/p7/4p3/8/P5P1/1PP4P/2K1R3 w - - 0 1");
        int move = SanFormatter.moveFromSan(board, "Rxe5");
        assertEquals(100, See.see(board, move));
    }

    @Test
    public void chessProgrammingExample2() {
        Board board = Board.fen("1k1r3q/1ppn3p/p4b2/4p3/8/P2N2P1/1PP1R1BP/2K1Q3 w - - 0 1");
        int move = SanFormatter.moveFromSan(board, "Nxe5");
        assertEquals(-225, See.see(board, move));
    }

    @Test
    public void enPassant() {
        Board board = Board.fen("8/3K4/3p4/1Pp3kr/1R3p2/8/4P1P1/8 w - c6 0 3");
        DebugUtils.debug(DebugUtils.CHESS, board);
        int move = FenFormatter.moveFromFen(board, "b5c6");
        assertEquals(100, See.see(board, move));
    }

    @Test
    public void dualKing() {
        Board board = Board.fen("8/2p5/2Kp4/1P6/R4pk1/7r/4P1P1/8 w - - 3 3");
        int move = FenFormatter.moveFromFen(board, "g2h3");
        assertEquals(400, See.see(board, move));
    }

    @Test
    public void promotion() {
        Board board = Board.fen("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/1PN2Q1P/P1PBBPp1/R3K2R b KQkq - 0 2");
        int move = FenFormatter.moveFromFen(board, "g2h1q");
        assertEquals(400, See.see(board, move));
    }
}
