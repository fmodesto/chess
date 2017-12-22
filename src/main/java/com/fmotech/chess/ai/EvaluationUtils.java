package com.fmotech.chess.ai;

import com.fmotech.chess.Board;
import com.fmotech.chess.DebugUtils;

import static com.fmotech.chess.BitOperations.lowestBitPosition;
import static com.fmotech.chess.BitOperations.nextLowestBit;
import static com.fmotech.chess.MoveGenerator.E;
import static com.fmotech.chess.MoveGenerator.N;
import static com.fmotech.chess.MoveGenerator.NE;
import static com.fmotech.chess.MoveGenerator.NW;
import static com.fmotech.chess.MoveGenerator.S;
import static com.fmotech.chess.MoveGenerator.SE;
import static com.fmotech.chess.MoveGenerator.SW;
import static com.fmotech.chess.MoveGenerator.W;
import static com.fmotech.chess.MoveGenerator.shiftOne;
import static com.fmotech.chess.MoveGenerator.slidingAttacks;
import static com.fmotech.chess.MoveTables.KING_TABLE;
import static com.fmotech.chess.MoveTables.KNIGHT_TABLE;

public class EvaluationUtils {

    public static final long[] PAWN_ISOLATED_TABLE = createPawnIsolatedMask();
    public static final long[][] PAWN_PASSED_TABLE = createPawnPassedMask();
    private static final long FILE_8 = 0x0101010101010101L;

    public static final int PAWN_IDX = 0;
    public static final int KNIGHT_IDX = 1;
    public static final int BISHOP_IDX = 2;
    public static final int ROOK_IDX = 3;
    public static final int QUEEN_IDX = 4;
    public static final int KING_IDX = 5;

    public static final int OWN_SIDE = 0;
    public static final int ENEMY_SIDE = 1;

    public static final long A1 = 0x0000000000000080L;
    public static final long A2 = 0x0000000000008000L;
    public static final long A3 = 0x0000000000800000L;
    public static final long A4 = 0x0000000080000000L;
    public static final long A5 = 0x0000008000000000L;
    public static final long A6 = 0x0000800000000000L;
    public static final long A7 = 0x0080000000000000L;
    public static final long A8 = 0x8000000000000000L;
    public static final long B1 = 0x0000000000000040L;
    public static final long B2 = 0x0000000000004000L;
    public static final long B3 = 0x0000000000400000L;
    public static final long B4 = 0x0000000040000000L;
    public static final long B5 = 0x0000004000000000L;
    public static final long B6 = 0x0000400000000000L;
    public static final long B7 = 0x0040000000000000L;
    public static final long B8 = 0x4000000000000000L;
    public static final long C1 = 0x0000000000000020L;
    public static final long C2 = 0x0000000000002000L;
    public static final long C3 = 0x0000000000200000L;
    public static final long C4 = 0x0000000020000000L;
    public static final long C5 = 0x0000002000000000L;
    public static final long C6 = 0x0000200000000000L;
    public static final long C7 = 0x0020000000000000L;
    public static final long C8 = 0x2000000000000000L;
    public static final long D1 = 0x0000000000000010L;
    public static final long D2 = 0x0000000000001000L;
    public static final long D3 = 0x0000000000100000L;
    public static final long D4 = 0x0000000010000000L;
    public static final long D5 = 0x0000001000000000L;
    public static final long D6 = 0x0000100000000000L;
    public static final long D7 = 0x0010000000000000L;
    public static final long D8 = 0x1000000000000000L;
    public static final long E1 = 0x0000000000000008L;
    public static final long E2 = 0x0000000000000800L;
    public static final long E3 = 0x0000000000080000L;
    public static final long E4 = 0x0000000008000000L;
    public static final long E5 = 0x0000000800000000L;
    public static final long E6 = 0x0000080000000000L;
    public static final long E7 = 0x0008000000000000L;
    public static final long E8 = 0x0800000000000000L;
    public static final long F1 = 0x0000000000000004L;
    public static final long F2 = 0x0000000000000400L;
    public static final long F3 = 0x0000000000040000L;
    public static final long F4 = 0x0000000004000000L;
    public static final long F5 = 0x0000000400000000L;
    public static final long F6 = 0x0000040000000000L;
    public static final long F7 = 0x0004000000000000L;
    public static final long F8 = 0x0400000000000000L;
    public static final long G1 = 0x0000000000000002L;
    public static final long G2 = 0x0000000000000200L;
    public static final long G3 = 0x0000000000020000L;
    public static final long G4 = 0x0000000002000000L;
    public static final long G5 = 0x0000000200000000L;
    public static final long G6 = 0x0000020000000000L;
    public static final long G7 = 0x0002000000000000L;
    public static final long G8 = 0x0200000000000000L;
    public static final long H1 = 0x0000000000000001L;
    public static final long H2 = 0x0000000000000100L;
    public static final long H3 = 0x0000000000010000L;
    public static final long H4 = 0x0000000001000000L;
    public static final long H5 = 0x0000000100000000L;
    public static final long H6 = 0x0000010000000000L;
    public static final long H7 = 0x0001000000000000L;
    public static final long H8 = 0x0100000000000000L;

    private static long[][] createPawnPassedMask() {
        long[][] positions = new long[2][64];
        for (int i = 0; i < 64; i++) {
            long p = 1L << i;
            for (int j = 0; j < 7; j++) {
                if (i % 8 > 0)
                    positions[0][i] |= p << 7 + 8L * j;
                if (i % 8 < 7)
                    positions[0][i] |= p << 9 + 8L * j;
                positions[0][i] |= p << 8 + 8L * j;
            }
        }
        for (int i = 0; i < 64; i++) {
            long p = 1L << i;
            for (int j = 0; j < 7; j++) {
                if (i % 8 > 0)
                    positions[1][i] |= p >>> 9 + 8L * j;
                if (i % 8 < 7)
                    positions[1][i] |= p >>> 7 + 8L * j;
                positions[1][i] |= p >>> 8 + 8L * j;
            }
        }
        return positions;
    }

    private static long[] createPawnIsolatedMask() {
        long[] files = createFiles();
        long[] positions = new long[64];
        for (int i = 0; i < 64; i++) {
            if (i % 8 > 0)
                positions[i] |= files[i % 8 - 1];
            if (i % 8 < 7)
                positions[i] |= files[i % 8 + 1];
        }
        return positions;
    }

    private static long[] createFiles() {
        long[] positions = new long[8];
        for (int i = 0; i < positions.length; i++) {
            positions[i] = FILE_8 << i;
        }
        return positions;
    }

    public static byte[][] toBoardPosition(int... data) {
        byte[][] array = new byte[2][data.length];
        for (int i = 0; i < data.length; i++) {
            array[0][data.length - i - 1] = (byte) data[i];
        }
        for (int i = 0; i < data.length; i++) {
            array[1][i] = array[0][((7 - rank(i)) << 3) | file(i)];
        }
        return array;
    }

    public static void generateMasks(Board board, long[] masks) {
        masks[0] = shiftOne(board.ownPawns(), NW) | shiftOne(board.ownPawns(), NE);
        masks[1] = generateMask(board.ownKnights(), KNIGHT_TABLE);
        masks[2] = generateBishopsMask(board.ownBishops(), ~board.pieces());
        masks[3] = generateRooksMask(board.ownRooks(), ~board.pieces());
        masks[4] = generateBishopsMask(board.ownQueens(), ~board.pieces())
                | generateRooksMask(board.ownQueens(), ~board.pieces());
        masks[5] = generateMask(board.ownKing(), KING_TABLE);
        masks[6] = shiftOne(board.enemyPawns(), SW) | shiftOne(board.enemyPawns(), SE);
        masks[7] = generateMask(board.enemyKnights(), KNIGHT_TABLE);
        masks[8] = generateBishopsMask(board.enemyBishops(), ~board.pieces());
        masks[9] = generateRooksMask(board.enemyRooks(), ~board.pieces());
        masks[10] = generateBishopsMask(board.enemyQueens(), ~board.pieces())
                | generateRooksMask(board.enemyQueens(), ~board.pieces());
        masks[11] = generateMask(board.enemyKing(), KING_TABLE);
    }

    private static long generateBishopsMask(long slider, long empty) {
        return slider == 0 ? 0 : slidingAttacks(slider, empty, NW)
                | slidingAttacks(slider, empty, NE)
                | slidingAttacks(slider, empty, SW)
                | slidingAttacks(slider, empty, SE);
    }

    private static long generateRooksMask(long slider, long empty) {
        return slider == 0 ? 0 : slidingAttacks(slider, empty, N)
                | slidingAttacks(slider, empty, S)
                | slidingAttacks(slider, empty, W)
                | slidingAttacks(slider, empty, E);
    }

    private static long generateMask(long next, long[] table) {
        long mask = 0;
        while (next != 0) {
            int pos = lowestBitPosition(next);
            mask |= table[pos];
            next = nextLowestBit(next);
        }
        return mask;
    }

    public static int rank(int pos) {
        return pos >>> 3;
    }

    public static int file(int pos) {
        return pos & 0x7;
    }
}
