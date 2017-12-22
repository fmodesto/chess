package com.fmotech.chess;

import static com.fmotech.chess.DebugUtils.CHESS;

public class MoveTables {
    public static final long[] PAWN_TABLE = computePawn();
    public static final long[] PAWN_ATTACK_HIGH_TABLE = computePawnAttackHigh();
    public static final long[] PAWN_ATTACK_LOW_TABLE = computePawnAttackLow();

    public static final long[] ROOK_HIGH_TABLE = computeRookHigh();
    public static final long[] ROOK_LOW_TABLE = computeRookLow();

    public static final long[] BISHOP_HIGH_TABLE = computeBishopHigh();
    public static final long[] BISHOP_LOW_TABLE = computeBishopLow();

    public static final long[] KNIGHT_TABLE = computeKnight();

    public static final long[] KING_TABLE = computeKing();

    public static final long[] DIR3_TABLE = computeDir(0b100);
    public static final long[] DIR2_TABLE = computeDir(0b010);
    public static final long[] DIR1_TABLE = computeDir(0b001);

    private static long[] computeDir(int bit) {
        long[] table = new long[64];
        for (int i = 0; i < table.length; i++) {
            long slider = 1L << i;
            for (int j = 0; j < 8; j++) {
                if ((j & bit) != 0)
                    table[i] |= MoveGenerator.slidingAttacks(slider, -1, j);
            }
        }
        return table;
    }

    public static void main(String[] args) {
        for (int i = 0; i < 64; i++) {
            long board = KNIGHT_TABLE[i];
            DebugUtils.debug(CHESS, Board.KNIGHT, board);
        }
    }

    private static long[] computePawn() {
        long[] positions = new long[64];
        for (int i = 0; i < 64; i++) {
            positions[i] = (1L << i) << 8;
        }
        return positions;
    }

    private static long[] computePawnAttackHigh() {
        long leftMask = ~computeRight();
        long rightMask = ~computeLeft();
        long[] positions = new long[64];
        for (int i = 0; i < 64; i++) {
            long p = 1L << i;
            long board = 0;
            board |= p << 7;
            board |= p << 9;
            if (i % 8 == 7) {
                board &= leftMask;
            } else if (i % 8 == 0) {
                board &= rightMask;
            }
            positions[i] = board;
        }
        return positions;
    }

    private static long[] computePawnAttackLow() {
        long leftMask = ~computeRight();
        long rightMask = ~computeLeft();
        long[] positions = new long[64];
        for (int i = 0; i < 64; i++) {
            long p = 1L << i;
            long board = 0;
            board |= p >>> 7;
            board |= p >>> 9;
            if (i % 8 == 7) {
                board &= leftMask;
            } else if (i % 8 == 0) {
                board &= rightMask;
            }
            positions[i] = board;
        }
        return positions;
    }

    private static long[] computeRookHigh() {
        long up = computeUp();
        long left = computeLeft();
        long[] positions = new long[64];
        for (int i = 0; i < 64; i++) {
            long p = 1L << i;
            long board = 0;
            long c = p;
            while ((c & left) == 0) {
                c <<= 1;
                board |= c;
            }
            c = p;
            while ((c & up) == 0) {
                c <<= 8;
                board |= c;
            }
            positions[i] = board;
        }
        return positions;
    }

    private static long[] computeRookLow() {
        long down = computeDown();
        long right = computeRight();
        long[] positions = new long[64];
        for (int i = 0; i < 64; i++) {
            long p = 1L << i;
            long board = 0;
            long c = p;
            while ((c & right) == 0) {
                c >>>= 1;
                board |= c;
            }
            c = p;
            while ((c & down) == 0) {
                c >>>= 8;
                board |= c;
            }
            positions[i] = board;
        }
        return positions;
    }

    private static long[] computeBishopHigh() {
        long up = computeUp();
        long left = computeLeft();
        long right = computeRight();
        long[] positions = new long[64];
        for (int i = 0; i < 64; i++) {
            long p = 1L << i;
            long board = 0;
            long c = p;
            while ((c & right | c & up) == 0) {
                c <<= 7;
                board |= c;
            }
            c = p;
            while ((c & left | c & up) == 0) {
                c <<= 9;
                board |= c;
            }
            positions[i] = board;
        }
        return positions;
    }

    private static long[] computeBishopLow() {
        long down = computeDown();
        long left = computeLeft();
        long right = computeRight();
        long[] positions = new long[64];
        for (int i = 0; i < 64; i++) {
            long p = 1L << i;
            long board = 0;
            long c = p;
            while ((c & left | c & down) == 0) {
                c >>>= 7;
                board |= c;
            }
            c = p;
            while ((c & right | c & down) == 0) {
                c >>>= 9;
                board |= c;
            }
            positions[i] = board;
        }
        return positions;
    }

    private static long[] computeKnight() {
        long left = computeLeft();
        long right = computeRight();
        long leftMask = ~(right | right << 1);
        long rightMask = ~(left | left >>> 1);
        long[] positions = new long[64];
        for (int i = 0; i < 64; i++) {
            long p = 1L << i;
            long board = 0;
            board |= p >>> 17;
            board |= p >>> 15;
            board |= p >>> 10;
            board |= p >>> 6;
            board |= p << 17;
            board |= p << 15;
            board |= p << 10;
            board |= p << 6;
            if (i % 8 >= 6) {
                board &= leftMask;
            } else if (i % 8 < 2) {
                board &= rightMask;
            }
            positions[i] = board;
        }
        return positions;
    }

    private static long[] computeKing() {
        long leftMask = ~computeRight();
        long rightMask = ~computeLeft();
        long[] positions = new long[64];
        for (int i = 0; i < 64; i++) {
            long p = 1L << i;
            long board = 0;
            board |= p >>> 1;
            board |= p >>> 7;
            board |= p >>> 8;
            board |= p >>> 9;
            board |= p << 1;
            board |= p << 7;
            board |= p << 8;
            board |= p << 9;
            if (i % 8 == 7) {
                board &= leftMask;
            } else if (i % 8 == 0) {
                board &= rightMask;
            }
            positions[i] = board;
        }
        return positions;
    }

    private static long computeUp() {
        long up = 0;
        for (int i = 0; i < 8; i++) {
            up |= 1L << (i + 56);
        }
        return up;
    }

    private static long computeDown() {
        long down = 0;
        for (int i = 0; i < 8; i++) {
            down |= 1L << i;
        }
        return down;
    }

    private static long computeLeft() {
        long left = 0;
        for (int i = 0; i < 8; i++) {
            left |= 1L << ((8L * i) + 7);
        }
        return left;
    }

    private static long computeRight() {
        long right = 0;
        for (int i = 0; i < 8; i++) {
            right |= 1L << (8L * i);
        }
        return right;
    }
}
