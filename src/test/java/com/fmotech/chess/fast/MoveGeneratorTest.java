package com.fmotech.chess.fast;

import com.fmotech.chess.Board;
import org.junit.Test;

import java.time.LocalDateTime;

import static com.fmotech.chess.BitOperations.lowestBitPosition;
import static com.fmotech.chess.FenFormatter.fromFen;
import static java.time.LocalDateTime.now;
import static java.time.temporal.ChronoUnit.MILLIS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MoveGeneratorTest {

    private static final int SKIP = 1;

    @Test
    public void perftTests() {
        execute(fromFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"),
                20, 400, 8_902, 197_281, 4_865_609, 119_060_324, 3_195_901_860L);
        execute(fromFen("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1"),
                48, 2_039, 97_862, 4_085_603, 193_690_690, 8_031_647_685L);
        execute(fromFen("8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - - 0 1"),
                14, 191, 2_812, 43_238, 674_624, 11_030_083, 178_633_661);
        execute(fromFen("r3k2r/Pppp1ppp/1b3nbN/nP6/BBP1P3/q4N2/Pp1P2PP/R2Q1RK1 w kq - 0 1"),
                6, 264, 9_467, 422_333, 15_833_292, 706_045_033);
        execute(fromFen("rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 1 8"),
                44, 1_486, 62_379, 2_103_487, 89_941_194, 3_048_196_529L);
        execute(fromFen("r4rk1/1pp1qppp/p1np1n2/2b1p1B1/2B1P1b1/P1NP1N2/1PP1QPPP/R4RK1 w - - 0 10"),
                46, 2_079, 89_890, 3_894_594, 164_075_551, 6_923_051_137L);
        execute(fromFen("8/2p5/2Kp4/1P3k1r/1R3p2/8/4P1P1/8 b - - 0 1"),
                16, 269, 4590, 80_683, 1_426_460, 25_913_670, 465_252_415);
    }

    private void execute(Board board, long... counts) {
        System.out.println(board);
        for (int i = 1; i <= counts.length - SKIP; i++) {
            LocalDateTime start = now();
            long count = MoveGenerator.perft(i, false, board);
            System.out.printf("%d: %10d in %6d ms\n", i, count, MILLIS.between(start, now()));
            assertEquals(counts[i-1], count);
        }
    }

    @Test
    public void shouldBeInCheck() {
        Board board = fromFen("rnbqkbnr/ppp1pppp/3p4/8/Q7/2P5/PP1PPPPP/RNB1KBNR b KQkq - 0 1");
        assertTrue(MoveGenerator.positionAttacked(lowestBitPosition(board.ownKing()), board));
    }
}
