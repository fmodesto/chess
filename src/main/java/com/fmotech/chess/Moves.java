package com.fmotech.chess;

import static com.fmotech.chess.BitOperations.bitCount;
import static com.fmotech.chess.BitOperations.lowestBitPosition;
import static com.fmotech.chess.BitOperations.nextLowestBit;

public class Moves {

    private static final long[] rays = new long[0x10000];
    private static final long[] bmask45 = new long[64];
    private static final long[] bmask135 = new long[64];

    private static long BIT(int f) { return 1L << f; }
    private static boolean TEST(int f, long b) { return (BIT(f) & b) != 0; }

    private static long RXRAY1(int pos, long occupied) { return rays[((pos) << 7) | key000(occupied, pos) | 0x8000]; }
    private static long RXRAY2(int pos, long occupied) { return rays[((pos) << 7) | key090(occupied, pos) | 0xA000]; }
    private static long BXRAY3(int pos, long occupied) { return rays[((pos) << 7) | key045(occupied, pos) | 0xC000]; }
    private static long BXRAY4(int pos, long occupied) { return rays[((pos) << 7) | key135(occupied, pos) | 0xE000]; }

    private static long RATT1(int pos, long occupied) { return rays[((pos) << 7) | key000(occupied, pos)]; }
    private static long RATT2(int pos, long occupied) { return rays[((pos) << 7) | key090(occupied, pos) | 0x2000]; }
    private static long BATT3(int pos, long occupied) { return rays[((pos) << 7) | key045(occupied, pos) | 0x4000]; }
    private static long BATT4(int pos, long occupied) { return rays[((pos) << 7) | key135(occupied, pos) | 0x6000]; }

    private static long RCAP(int pos, long occupied, long enemy) { return ROCC(pos, occupied) & enemy; }
    private static long BCAP(int pos, long occupied, long enemy) { return BOCC(pos, occupied) & enemy; }
    private static long ROCC(int pos, long occupied) { return ROCC1(pos, occupied) | ROCC2(pos, occupied); }
    private static long BOCC(int pos, long occupied) { return BOCC3(pos, occupied) | BOCC4(pos, occupied); }

    private static long ROCC1(int pos, long occupied) { return RATT1(pos, occupied) & occupied; }
    private static long ROCC2(int pos, long occupied) { return RATT2(pos, occupied) & occupied; }
    private static long BOCC3(int pos, long occupied) { return BATT3(pos, occupied) & occupied; }
    private static long BOCC4(int pos, long occupied) { return BATT4(pos, occupied) & occupied; }
    private static long RMOVE1(int pos, long occupied) { return (RATT1(pos, occupied) & ~occupied); }
    private static long RMOVE2(int pos, long occupied) { return (RATT2(pos, occupied) & ~occupied); }
    private static long BMOVE3(int pos, long occupied) { return (BATT3(pos, occupied) & ~occupied); }
    private static long BMOVE4(int pos, long occupied) { return (BATT4(pos, occupied) & ~occupied); }

    static long RMOVE(int pos, long occupied) { return (RMOVE1(pos, occupied) | RMOVE2(pos, occupied)); }
    static long BMOVE(int pos, long occupied) { return (BMOVE3(pos, occupied) | BMOVE4(pos, occupied)); }

    private static int key000(long board, int pos) {
        return (int) ((board >> (pos & 0x38)) & 0x7E);
    }

    private static int key090(long board, int pos) {
        long b = (board >> (pos&7)) & 0x0101010101010101L;
        b = b * 0x0080402010080400L;
        return (int)((b >> 57) & 0x7F);
    }

    private static int key045(long board, int pos) {
        return keyDiag(board & bmask45[pos]);
    }

    private static int key135(long board, int pos) {
        return keyDiag(board & bmask135[pos]);
    }

    private static int keyDiag(long b) {
        b *= 0x0202020202020202L;
        return (int)((b >> 57) & 0x7F);
    }

    public static long pinnedPieces(Board board, long color) {
        int king = lowestBitPosition(board.kings() & color);
        long pin = 0L;
        long next = ((RXRAY1(king, board.pieces()) | RXRAY2(king, board.pieces())) & board.pieces() & ~color) & board.rooksQueens();
        while (next != 0) {
            int t = lowestBitPosition(next);
            pin |= RCAP(t, board.pieces(), color) & ROCC(king, board.pieces());
            next = nextLowestBit(next);
        }
        next = ((BXRAY3(king, board.pieces()) | BXRAY4(king, board.pieces())) & ~color) & board.bishopsQueens();
        DebugUtils.debug(DebugUtils.CHESS, 1, board.pieces());
        DebugUtils.debug(DebugUtils.CHESS, 1, BATT4(king, board.pieces()));
        DebugUtils.debug(DebugUtils.CHESS, 1, BXRAY4(king, board.pieces()));
        while (next != 0) {
            int t = lowestBitPosition(next);
            pin |= BCAP(t, board.pieces(), color) & BOCC(king, board.pieces());
            next = nextLowestBit(next);
        }
        return pin;
    }

    public static long rookMove(int pos, long pieces, long own) {
        return (RATT1(pos, pieces) | RATT2(pos, pieces)) & ~own;
    }

    public static long bishopMove(int pos, long pieces, long own) {
        return (BATT3(pos, pieces) | BATT4(pos, pieces)) & ~own;
    }

    public static long queenMove(int pos, long pieces, long own) {
        return (RATT1(pos, pieces) | RATT2(pos, pieces) | BATT3(pos, pieces) | BATT4(pos, pieces)) & ~own;
    }

    public static long rookAttack(int pos, long pieces, long own) {
        return rookMove(pos, pieces, own) & pieces;
    }

    public static long bishopAttack(int pos, long pieces, long own) {
        return bishopMove(pos, pieces, own) & pieces;
    }

    public static long queenAttack(int pos, long pieces, long own) {
        return queenMove(pos, pieces, own) & pieces;
    }


    public static void main(String[] args) {
        Board board = Board.fen("Q1n1k3/3p4/8/1B6/Q7/4N3/8/4K3 w KQkq -");
        long pin = pinnedPieces(board, board.enemyPieces());
        DebugUtils.debug(DebugUtils.CHESS, 1, pin);
        DebugUtils.debug(DebugUtils.CHESS, 1, RATT1(63, board.pieces()) | RATT2(63, board.pieces()));
        DebugUtils.debug(DebugUtils.CHESS, 1, RMOVE(63, board.pieces()));
    }








    /**************** INITIALIZER ***************/

    private interface Generator {
        long invoke(int pos, long board, int type);
    }

    private interface Key {
        int invoke(long b, int pos);
    }

    static {
        Generator rook00 = (int pos, long board, int type) -> {
            long free = 0L, occ = 0L, xray = 0L;
            int i, b;
            for (b = 0, i = pos+1; i < 64 && i%8 != 0; i++) {
                if (TEST(i, board)) { if (b != 0) { xray |= BIT(i); break; } else { occ |= BIT(i); b = 1; }}
                if (b == 0) free |= BIT(i);
            }
            for (b = 0, i = pos-1; i >= 0 && i%8 != 7; i--) {
                if (TEST(i, board)) { if (b != 0) { xray |= BIT(i); break; } else { occ |= BIT(i); b = 1; }}
                if (b == 0) free |= BIT(i);
            }
            return (type < 2) ? free : (type == 2 ? occ : xray);
        };

        Generator rook90 = (int pos, long board, int type) -> {
            long free = 0L, occ = 0L, xray = 0L;
            int i, b;
            for (b = 0, i = pos-8; i >= 0; i-=8) {
                if (TEST(i, board)) { if (b != 0) { xray |= BIT(i); break; } else { occ |= BIT(i); b = 1; }}
                if (b == 0) free |= BIT(i);
            }
            for (b = 0, i = pos+8; i < 64; i+=8) {
                if (TEST(i, board)) { if (b != 0) { xray |= BIT(i); break; } else { occ |= BIT(i); b = 1; }}
                if (b == 0) free |= BIT(i);
            }
            return (type < 2) ? free : (type == 2 ? occ : xray);
        };

        Generator bishop45 = (int pos, long board, int type) -> {
            long free = 0L, occ = 0L, xray = 0L;
            int i, b;
            for (b = 0, i = pos+9; i < 64 && (i%8 != 0); i+=9) {
                if (TEST(i, board)) { if (b != 0) { xray |= BIT(i); break; } else { occ |= BIT(i); b = 1; }}
                if (b == 0) free |= BIT(i);
            }
            for (b = 0, i = pos-9; i >= 0 && (i%8 != 7); i-=9) {
                if (TEST(i, board)) { if (b != 0) { xray |= BIT(i); break; } else { occ |= BIT(i); b = 1; }}
                if (b == 0) free |= BIT(i);
            }
            return (type < 2) ? free : (type == 2 ? occ : xray);
        };

        Generator bishop135 = (int pos, long board, int type) -> {
            long free = 0L, occ = 0L, xray = 0L;
            int i, b;
            for (b = 0, i = pos-7; i >= 0 && (i%8 != 0); i-=7) {
                if (TEST(i, board)) { if (b != 0) { xray |= BIT(i); break; } else { occ |= BIT(i); b = 1; }}
                if (b == 0) free |= BIT(i);
            }
            for (b = 0, i = pos+7; i < 64 && (i%8 != 7); i+=7) {
                if (TEST(i, board)) { if (b != 0) { xray |= BIT(i); break; } else { occ |= BIT(i); b = 1; }}
                if (b == 0) free |= BIT(i);
            }
            return (type < 2) ? free : (type == 2 ? occ : xray);
        };

        for (int i = 0; i < 64; i++) bmask45[i] = bishop45.invoke(i, 0L, 0) | BIT(i);
        for (int i = 0; i < 64; i++) bmask135[i] = bishop135.invoke(i, 0L, 0) | BIT(i);

        initRays(0x0000, rook00, Moves::key000);
        initRays(0x2000, rook90, Moves::key090);
        initRays(0x4000, bishop45, Moves::key045);
        initRays(0x6000, bishop135, Moves::key135);
    }

    private static void initRays(int offset, Generator rayFunc, Key key) {
        int i, f, iperm, bc, index;
        long board, mmask, occ, move, xray;
        for (f = 0; f < 64; f++) {
            mmask = rayFunc.invoke(f, 0L, 0) | BIT(f);
            iperm = 1 << (bc = bitCount(mmask));
            for (i = 0; i < iperm; i++) {
                board = occupiedFreeBoard(bc, i, mmask);
                move = rayFunc.invoke(f, board, 1);
                occ = rayFunc.invoke(f, board, 2);
                xray = rayFunc.invoke(f, board, 3);
                index = key.invoke(board, f);
                rays[(f << 7) + index + offset] = occ | move;
                rays[(f << 7) + index + 0x8000 + offset] = xray;
            }
        }
    }

    static long occupiedFreeBoard(int bc, int del, long free) {
        long low, perm = free;
        for (int i = 0; i < bc; i++) {
            low = free & -free;
            free &= ~low;
            if (!TEST(i, del)) perm &= (~low);
        }
        return perm;
    }
}
