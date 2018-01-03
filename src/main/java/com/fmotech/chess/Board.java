package com.fmotech.chess;

import java.nio.ByteBuffer;
import java.util.Arrays;

import static com.fmotech.chess.BitOperations.highInt;
import static com.fmotech.chess.BitOperations.lowInt;
import static com.fmotech.chess.BitOperations.reverse;

public class Board {
    private static final long CASTLE = 0x8100000000000081L;
    private static final long EN_PASSANT = 0x0000ff0000ff0000L;

    private static final long RANK_1 = 0x00_00_00_00_00_00_00_ffL;
    private static final long RANK_8 = 0xff_00_00_00_00_00_00_00L;

    private static final long LOW_ROOK = 1L;
    private static final long HIGH_ROOK = 1L << 7;

    public static final int KING = 0b100;
    public static final int QUEEN = 0b110;
    public static final int ROOK = 0b101;
    public static final int BISHOP = 0b011;
    public static final int KNIGHT = 0b010;
    public static final int PAWN = 0b001;

    public static final int SPECIAL = 0b111;

    private static final long HASH1_BITS = 0xDEADBEEFDEADBEEFL;
    private static final long HASH2_BITS = 0x9E3779B97F4A7C13L;

    public static final Board INIT = Board.of(0,
            0xffff000000000000L,
            0x9900000000000099L,
            0x7600000000000076L | CASTLE,
            0xa5ff00000000ffa5L);

    private Board next;
    private int[] moves = new int[256];
    private long ply;
    private long b4;
    private long b3;
    private long b2;
    private long b1;

    public static Board of(long ply, long b4, long b3, long b2, long b1) {
        return new Board().set(ply, b4, b3, b2, b1);
    }

    public static Board of(int ply, int fifty, long color, long pawns, long rooks, long knights, long bishops, long queens, long kings, long enPassant, long castle) {
        return Board.of(BitOperations.joinInts(fifty, ply), color,
                queens | kings | rooks | enPassant | castle,
                queens | knights | bishops | enPassant | castle,
                pawns | bishops | rooks | enPassant | castle);
    }

    public static Board fen(String fen) {
        return FenFormatter.fromFen(fen);
    }

    public Board cloneBoard() {
        return Board.of(ply, b4, b3, b2, b1);
    }

    private Board set(long ply, long b4, long b3, long b2, long b1) {
        this.ply = ply;
        this.b4 = b4;
        this.b3 = b3;
        this.b2 = b2;
        this.b1 = b1;
        return this;
    }

    private Board nextBoard() {
        if (next == null) {
            next = new Board();
        }
        return next;
    }

    public Board nextTurn() {
        return nextBoard().set(ply + 1, reverse(~b4), reverse(b3), reverse(b2), reverse(b1));
    }

    public int ply() {
        return lowInt(ply);
    }

    public int fullMove() {
        return (lowInt(ply) / 2) + 1;
    }

    public int fifty() {
        return lowInt(ply) - highInt(ply);
    }

    public int[] moves() {
        Arrays.fill(moves, 0);
        return moves;
    }

    public Board move(int move) {
        long src = 1L << (move & 0xFF);
        long dest = 1L << ((move >>> 8) & 0xFF);
        int promotion = (move >>> 24) & 0x07;
        return move(src, dest, promotion);
    }

    public Board move(long src, long dest, int promotion) {
        long currentPly = ply();
        if ((src & ownPawns()) != 0) {
            long oldEnPassant = enPassant();
            long newEnPassant = (src << 8) & (dest >>> 8);
            long enPassantKill = (dest & oldEnPassant) >>> 8;
            long clear = ~(src | dest | newEnPassant | oldEnPassant | enPassantKill);
            long b4 = this.b4 & clear;
            long b3 = this.b3 & clear;
            long b2 = this.b2 & clear;
            long b1 = this.b1 & clear;
            promotion = promotion == 0 && (dest & RANK_8) != 0 ? QUEEN : promotion;
            long p3 = promotion == 0 ? 0 : (promotion >>> 2 & 1) * dest;
            long p2 = promotion == 0 ? 0 : (promotion >>> 1 & 1) * dest;
            long p1 = promotion == 0 ? dest : (promotion & 1) * dest;
            b4 |= whiteTurn() ? 0 : src | enPassantKill | (oldEnPassant & ~dest);
            b3 |= newEnPassant | p3;
            b2 |= newEnPassant | p2;
            b1 |= newEnPassant | p1;
            return nextBoard().set((currentPly + 1) << 32 | currentPly, b4, b3, b2, b1);
        } else if ((src & ownKing() & RANK_1) != 0) {
            long castle = 0;
            long castleClear = 0;
            if (src >>> 2 == dest) {
                castleClear = 1L;
                castle = src >>> 1;
            } else if (src << 2 == dest) {
                castleClear = 1L << 7;
                castle = src << 1;
            }
            long clear = ~(src | dest | castle | enPassant() | castleClear);
            long b4 = this.b4 & clear;
            long b3 = this.b3 & clear;
            long b2 = this.b2 & clear & ~(castle() & RANK_1);
            long b1 = this.b1 & clear;
            b4 |= whiteTurn() ? 0 : src | (enPassant() & ~dest) | castleClear;
            b3 |= dest | castle;
            b2 |= 0;
            b1 |= castle;
            long ply = (dest & enemyPieces()) == 0 ? this.ply : (currentPly + 1) << 32 | currentPly;
            return nextBoard().set(ply, b4, b3, b2, b1);
        } else if ((src & ownRooks() & RANK_1) != 0) {
            long clear = ~(src | dest | enPassant());
            long b4 = this.b4 & clear;
            long b3 = this.b3 & clear;
            long b2 = this.b2 & clear;
            long b1 = this.b1 & clear;
            b4 |= whiteTurn() ? 0 : src | (enPassant() & ~dest);
            b3 |= dest;
            b2 |= 0;
            b1 |= dest;
            long ply = (dest & enemyPieces()) == 0 ? this.ply : (currentPly + 1) << 32 | currentPly;
            return nextBoard().set(ply, b4, b3, b2, b1);
        } else {
            long clear = ~(src | dest | enPassant());
            long b4 = this.b4 & clear;
            long b3 = this.b3 & clear;
            long b2 = this.b2 & clear;
            long b1 = this.b1 & clear;
            b4 |= whiteTurn() ? 0 : src | (enPassant() & ~dest);
            b3 |= ((this.b3 & src) != 0) ? dest : 0;
            b2 |= ((this.b2 & src) != 0) ? dest : 0;
            b1 |= ((this.b1 & src) != 0) ? dest : 0;
            long ply = (dest & enemyPieces()) == 0 ? this.ply : (currentPly + 1) << 32 | currentPly;
            return nextBoard().set(ply, b4, b3, b2, b1);
        }
    }

    public boolean whiteTurn() {
        return  (b4 & ~(b3 | b2 | b1)) == 0;
    }

    public boolean own(long piece) {
        return (piece & b4) == 0;
    }

    public boolean enemy(long piece) {
        return (piece & b4) != 0;
    }

    public long enPassant() {
        return b3 & b2 & b1 & EN_PASSANT;
    }

    public boolean castleLow() {
        return ((castle() & LOW_ROOK) == LOW_ROOK) && ((pieces() & (0x0E | ownKing())) == ownKing());
    }

    public boolean castleHigh() {
        return ((castle() & HIGH_ROOK) == HIGH_ROOK) && ((pieces() & (0x70 | ownKing())) == ownKing());
    }

    public int type(int pos, int castle, int enPassant) {
        int type = (int) (((b3 >>> pos) & 1) << 2 | ((b2 >>> pos) & 1) << 1 | ((b1 >>> pos) & 1));
        return type != SPECIAL ? type : ((1 << pos) & CASTLE) != 0 ? castle : enPassant;
    }

    public int type(long bit) {
        int type = (b3 & bit) != 0 ? 0b100 : 0;
        type |= (b2 & ~(b1 & b3) & bit) != 0 ? 0b010 : 0;
        type |= (b1 & bit) != 0 ? 0b001 : 0;
        return type;
    }

    // Pieces

    public long pieces() {
        return (b3 | b2 | b1) & ~enPassant();
    }

    public long pawns() {
        return ~b3 & ~b2 & b1;
    }

    public long rooks() {
        return b3 & (~b2 | CASTLE) & b1;
    }

    public long knights() {
        return ~b3 & b2 & ~b1;
    }

    public long bishops() {
        return ~b3 & b2 & b1;
    }

    public long queens() {
        return b3 & b2 & ~b1;
    }

    public long kings() {
        return b3 & ~b2 & ~b1;
    }

    public long castle() {
        return b3 & b2 & b1 & CASTLE;
    }

    public long rooksQueens() {
        return rooks() | queens();
    }

    public long bishopsQueens() {
        return bishops() | queens();
    }

    // Own pieces

    public long ownPieces() {
        return ~b4 & pieces();
    }

    public long ownPawns() {
        return ~b4 & ~b3 & ~b2 & b1;
    }

    public long ownRooks() {
        return ~b4 & b3 & (~b2 | CASTLE) & b1;
    }

    public long ownKnights() {
        return ~b4 & ~b3 & b2 & ~b1;
    }

    public long ownBishops() {
        return ~b4 & ~b3 & b2 & b1;
    }

    public long ownQueens() {
        return ~b4 & b3 & b2 & ~b1;
    }

    public long ownKing() {
        return ~b4 & b3 & ~b2 & ~b1;
    }

    public long ownCastle() {
        return castle() & RANK_1;
    }

    public long ownRooksQueens() {
        return ~b4 & (rooks() | queens());
    }

    public long ownBishopsQueens() {
        return ~b4 & (bishops() | queens());
    }

    // Enemy pieces

    public long enemyPieces() {
        return b4 & pieces();
    }

    public long enemyPawns() {
        return b4 & ~b3 & ~b2 & b1;
    }

    public long enemyRooks() {
        return b4 & b3 & (~b2 | CASTLE) & b1;
    }

    public long enemyKnights() {
        return b4 & ~b3 & b2 & ~b1;
    }

    public long enemyBishops() {
        return b4 & ~b3 & b2 & b1;
    }

    public long enemyQueens() {
        return b4 & b3 & b2 & ~b1;
    }

    public long enemyKing() {
        return b4 & b3 & ~b2 & ~b1;
    }

    public long enemyCastle() {
        return castle() & RANK_8;
    }

    public long enemyRooksQueens() {
        return b4 & (rooks() | queens());
    }

    public long enemyBishopsQueens() {
        return b4 & (bishops() | queens());
    }

    public long enemyEnPassantPawn() {
        return (b3 & b2 & b1 & 0x0000ff0000000000L) >>> 8;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Board board = (Board) o;
        if (b4 != board.b4) return false;
        if (b3 != board.b3) return false;
        if (b2 != board.b2) return false;
        return b1 == board.b1;
    }

    @Override
    public int hashCode() {
        long hash = hash();
        return (int) (hash ^ (hash >>> 32));
    }

    @Override
    public String toString() {
        return DebugUtils.toHexString(hash()) + " " + FenFormatter.toFen(this);
    }

    public long hash() {
        // Spooky Hash 64
        // Extracted from David Curtis: https://github.com/mayconbordin/streaminer/blob/master/src/main/java/org/streaminer/util/hash/SpookyHash64.java
        // Licensed under the Apache License, Version 2.0 (the "License") http://www.apache.org/licenses/LICENSE-2.0

        long h0, h1, h2, h3;
        h0 = HASH2_BITS;
        h1 = HASH2_BITS;
        h2 = HASH1_BITS;
        h3 = HASH1_BITS;

        h2 += b4;
        h3 += b3;

        h2 = (h2 << 50) | (h2 >>> 14);  h2 += h3;  h0 ^= h2;
        h3 = (h3 << 52) | (h3 >>> 12);  h3 += h0;  h1 ^= h3;
        h0 = (h0 << 30) | (h0 >>> 34);  h0 += h1;  h2 ^= h0;
        h1 = (h1 << 41) | (h1 >>> 23);  h1 += h2;  h3 ^= h1;
        h2 = (h2 << 54) | (h2 >>> 10);  h2 += h3;  h0 ^= h2;
        h3 = (h3 << 48) | (h3 >>> 16);  h3 += h0;  h1 ^= h3;
        h0 = (h0 << 38) | (h0 >>> 26);  h0 += h1;  h2 ^= h0;
        h1 = (h1 << 37) | (h1 >>> 27);  h1 += h2;  h3 ^= h1;
        h2 = (h2 << 62) | (h2 >>> 2);   h2 += h3;  h0 ^= h2;
        h3 = (h3 << 34) | (h3 >>> 30);  h3 += h0;  h1 ^= h3;
        h0 = (h0 << 5)  | (h0 >>> 59);  h0 += h1;  h2 ^= h0;
        h1 = (h1 << 36) | (h1 >>> 28);  h1 += h2;  h3 ^= h1;

        h0 += b2;
        h1 += b1;

        h2 += HASH1_BITS;
        h3 += HASH1_BITS;

        h3 ^= h2;  h2 = (h2 << 15) | (h2 >>> 49);  h3 += h2;
        h0 ^= h3;  h3 = (h3 << 52) | (h3 >>> 12);  h0 += h3;
        h1 ^= h0;  h0 = (h0 << 26) | (h0 >>> 38);  h1 += h0;
        h2 ^= h1;  h1 = (h1 << 51) | (h1 >>> 13);  h2 += h1;
        h3 ^= h2;  h2 = (h2 << 28) | (h2 >>> 36);  h3 += h2;
        h0 ^= h3;  h3 = (h3 << 9)  | (h3 >>> 55);  h0 += h3;
        h1 ^= h0;  h0 = (h0 << 47) | (h0 >>> 17);  h1 += h0;
        h2 ^= h1;  h1 = (h1 << 54) | (h1 >>> 10);  h2 += h1;
        h3 ^= h2;  h2 = (h2 << 32) | (h2 >>> 32);  h3 += h2;
        h0 ^= h3;  h3 = (h3 << 25) | (h3 >>> 39);  h0 += h3;
        h1 ^= h0;  h0 = (h0 << 63) | (h0 >>> 1);   h1 += h0;

        return h0;
    }

    public void write(ByteBuffer buffer) {
        buffer.putLong(ply);
        buffer.putLong(b4);
        buffer.putLong(b3);
        buffer.putLong(b2);
        buffer.putLong(b1);
    }

    public static Board read(ByteBuffer buffer) {
        return Board.of(buffer.getLong(), buffer.getLong(), buffer.getLong(), buffer.getLong(), buffer.getLong());
    }

    public void load(ByteBuffer buffer) {
        this.ply = buffer.getLong();
        this.b4 = buffer.getLong();
        this.b3 = buffer.getLong();
        this.b2 = buffer.getLong();
        this.b1 = buffer.getLong();
    }
}
