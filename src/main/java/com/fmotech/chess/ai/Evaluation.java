package com.fmotech.chess.ai;

import com.fmotech.chess.Board;

import static com.fmotech.chess.BitOperations.fileFill;
import static com.fmotech.chess.BitOperations.lowestBit;
import static com.fmotech.chess.BitOperations.lowestBitPosition;
import static com.fmotech.chess.BitOperations.nextLowestBit;
import static com.fmotech.chess.BitOperations.reverse;

public class Evaluation {

    private static final byte[] PAWN_TABLE = toBoardPosition(
            0,   0,   0,   0,   0,   0,   0,   0,
           20,  20,  20,  30,  30,  20,  20,  20,
           10,  10,  10,  20,  20,  10,  10,  10,
            5,   5,   5,  10,  10,   5,   5,   5,
            0,   0,  10,  20,  20,  10,   0,   0,
            5,   0,   0,   5,   5,   0,   0,   5,
           10,  10,   0, -10, -10,   0,  10,  10,
            0,   0,   0,   0,   0,   0,   0,   0
    );

    private static final byte[] KNIGHT_TABLE = toBoardPosition(
            0,   0,   0,   0,   0,   0,   0,   0,
            0,   0,   5,  10,  10,   5,   0,   0,
            5,  10,  10,  20,  20,  10,  10,   5,
            5,  10,  15,  20,  20,  15,  10,   5,
            0,   0,  10,  20,  20,  10,   5,   0,
            0,   0,  10,  10,  10,  10,   0,   0,
            0,   0,   0,   5,   5,   0,   0,   0,
            0, -10,   0,   0,   0,   0, -10,   0
    );

    private static final byte[] BISHOP_TABLE = toBoardPosition(
            0,   0,   0,   0,   0,   0,   0,   0,
            0,   0,   0,  10,  10,   0,   0,   0,
            0,   0,  10,  15,  15,  10,   0,   0,
            0,  10,  15,  20,  20,  15,  10,   0,
            0,  10,  15,  20,  20,  15,  10,   0,
            0,   0,  10,  15,  15,  10,   0,   0,
            0,   0,   0,  10,  10,   0,   0,   0,
            0,   0, -10,   0,   0, -10,   0,   0
    );

    private static final byte[] ROCK_TABLE = toBoardPosition(
            0,   0,   5,  10,  10,   5,   0,   0,
           25,  25,  25,  25,  25,  25,  25,  25,
            0,   0,   5,  10,  10,   5,   0,   0,
            0,   0,   5,  10,  10,   5,   0,   0,
            0,   0,   5,  10,  10,   5,   0,   0,
            0,   0,   5,  10,  10,   5,   0,   0,
            0,   0,   5,  10,  10,   5,   0,   0,
            0,   0,   5,  10,  10,   5,   0,   0
    );

    private static final long[] PAWN_ISOLATED_TABLE = createPawnIsolatedMask();
    private static final long[] PAWN_PASSED_TABLE = createPawnPassedMask();

    private static final long FILE_8 = 0x0101010101010101L;

    private static final int QUEEN = 1000;
    private static final int ROCK = 550;
    private static final int BISHOP = 325;
    private static final int KNIGHT = 325;
    private static final int PAWN = 100;

    private static final int[] PAWN_PASSED = new int[] { 0, 5, 10, 20, 35, 60, 100, 200 };
    private static final int PAWN_ISOLATED = -10;
    private static final int QUEEN_OPEN = 5;
    private static final int QUEEN_SEMI = 3;
    private static final int ROCK_OPEN = 10;
    private static final int ROCK_SEMI = 5;

    private static long[] createPawnPassedMask() {
        long[] positions = new long[64];
        for (int i = 0; i < 64; i++) {
            long p = 1L << i;
            for (int j = 0; j < (63 - i) / 8; j++) {
                if (i % 8 > 0)
                    positions[i] |= p << 7 + 8L * j;
                if (i % 8 < 7)
                    positions[i] |= p << 9 + 8L * j;
                positions[i] |= p << 8 + 8L * j;
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

    private static byte[] toBoardPosition(int... data) {
        byte[] array = new byte[data.length];
        for (int i = 1; i <= data.length; i++) {
            array[data.length - i] = (byte) data[i - 1];
        }
        return array;
    }

    public static int evaluateBoardPosition(Board board) {
        long ownOpen = ~fileFill(board.ownPawns());
        long enemyOpen = ~fileFill(board.enemyPawns());
        long open = (ownOpen & enemyOpen);
        int score = 0;

        if ((board.ownPawns() | board.ownQueens() | board.ownRocks()) != 0
                || nextLowestBit(board.ownKnights() | board.ownBishops()) != 0) {
            score += evalQueen(board.ownQueens(), open, ownOpen);
            score += evalRock(board.ownRocks(), open, ownOpen);
            score += eval(BISHOP, board.ownBishops(), BISHOP_TABLE);
            score += eval(KNIGHT, board.ownKnights(), KNIGHT_TABLE);
            score += evalPawns(PAWN, board.ownPawns(), board.enemyPawns());
        }

        if ((board.enemyPawns() | board.enemyQueens() | board.enemyRocks()) != 0
                || nextLowestBit(board.enemyKnights() | board.enemyBishops()) != 0) {
            score -= evalQueen(reverse(board.enemyQueens()), open, enemyOpen);
            score -= evalRock(reverse(board.enemyRocks()), open, enemyOpen);
            score -= eval(BISHOP, reverse(board.enemyBishops()), BISHOP_TABLE);
            score -= eval(KNIGHT, reverse(board.enemyKnights()), KNIGHT_TABLE);
            score -= evalPawns(PAWN, reverse(board.enemyPawns()), reverse(board.ownPawns()));
        }

        return score;
    }

    private static int evalRock(long pieces, long open, long semi) {
        int sum = 0;
        long next = pieces;
        while (next != 0) {
            long piece = lowestBit(next);
            int pos = lowestBitPosition(next);
            sum += ROCK + ROCK_TABLE[pos];
            if ((piece & open) != 0)
                sum += ROCK_OPEN;
            else if ((piece & semi) != 0)
                sum += ROCK_SEMI;
            next = nextLowestBit(next);
        }
        return sum;
    }

    private static int evalQueen(long pieces, long open, long semi) {
        int sum = 0;
        long next = pieces;
        while (next != 0) {
            long piece = lowestBit(next);
            sum += QUEEN;
            if ((piece & open) != 0)
                sum += QUEEN_OPEN;
            else if ((piece & semi) != 0)
                sum += QUEEN_SEMI;
            next = nextLowestBit(next);
        }
        return sum;
    }

    private static int eval(int points, long pieces, byte[] table) {
        int sum = 0;
        while (pieces != 0) {
            sum += points + table[lowestBitPosition(pieces)];
            pieces = nextLowestBit(pieces);
        }
        return sum;
    }

    private static int evalPawns(int points, long own, long enemy) {
        int sum = 0;
        long next = own;
        while (next != 0) {
            int pos = lowestBitPosition(next);
            sum += points + PAWN_TABLE[pos];
            if ((PAWN_ISOLATED_TABLE[pos] & own) == 0)
                sum += PAWN_ISOLATED;
            if ((PAWN_PASSED_TABLE[pos] & enemy) == 0)
                sum += PAWN_PASSED[pos >>> 3];
            next = nextLowestBit(next);
        }
        return sum;
    }
}
