package com.fmotech.chess;

import java.util.Arrays;

import static com.fmotech.chess.BitOperations.reverse;
import static org.apache.commons.lang3.StringUtils.leftPad;
import static org.apache.commons.lang3.StringUtils.substring;

public class Board {
    private static final long CASTLE = 0x8100000000000081L;
    private static final long EN_PASSANT = 0x0000ff0000ff0000L;

    private static final long RANK_1 = 0x00_00_00_00_00_00_00_ffL;
    private static final long RANK_8 = 0xff_00_00_00_00_00_00_00L;

    public static final long LOW_ROCK = 1L;
    public static final long HIGH_ROCK = 1L << 7;

    public static final int PAWN = 0b001;
    public static final int ROCK = 0b110;
    public static final int KNIGHT = 0b010;
    public static final int BISHOP = 0b011;
    public static final int QUEEN = 0b100;
    public static final int KING = 0b101;

    public static final Board INIT = Board.of(
            0xffff000000000000L,
            0x9900000000000099L,
            0xe7000000000000e7L,
            0x2cff00000000ff2cL | CASTLE);

    private Board next;
    private long[] moves = new long[32];
    private long b4;
    private long b3;
    private long b2;
    private long b1;

    public static Board of(long b4, long b3, long b2, long b1) {
        return new Board().set(b4, b3, b2, b1);
    }

    public static Board of(long color, long pawns, long rocks, long knights, long bishops, long queens, long kings, long enPassant, long castle) {
        return Board.of(color,
                rocks | queens | kings | enPassant | castle,
                rocks | knights | bishops | enPassant | castle,
                pawns | bishops | kings | enPassant | castle);
    }

    private Board set(long b4, long b3, long b2, long b1) {
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
        return nextBoard().set(reverse(~b4), reverse(b3), reverse(b2), reverse(b1));
    }

    public long[] moves() {
        Arrays.fill(moves, 0);
        return moves;
    }

    public Board move(long src, long dest) {
        if ((src & ownPawns()) != 0) {
            long oldEnPassant = enPassant();
            long newEnPassant = (src << 8) & (dest >>> 8);
            long enPassantKill = (dest & oldEnPassant) >>> 8;
            long clear = ~(src | dest | newEnPassant | oldEnPassant | enPassantKill);
            long b4 = this.b4 & clear;
            long b3 = this.b3 & clear;
            long b2 = this.b2 & clear;
            long b1 = this.b1 & clear;
            long promotion = dest & RANK_8;
            b4 |= whiteTurn() ? 0 : src | enPassantKill | (oldEnPassant & ~dest);
            b3 |= newEnPassant | promotion;
            b2 |= newEnPassant;
            b1 |= newEnPassant | (dest ^ promotion);
            return nextBoard().set(b4, b3, b2, b1);
        } else if ((src & b3 & b1 & RANK_1) != 0) {
            if (src == ownKing()) {
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
                long b2 = this.b2 & clear;
                long b1 = this.b1 & clear & ~(castle() & RANK_1);
                b4 |= whiteTurn() ? 0 : src | (enPassant() & ~dest) | castleClear;
                b3 |= dest | castle;
                b2 |= castle;
                b1 |= dest;
                return nextBoard().set(b4, b3, b2, b1);
            } else /* Rocks */ {
                long clear = ~(src | dest | enPassant());
                long b4 = this.b4 & clear;
                long b3 = this.b3 & clear;
                long b2 = this.b2 & clear;
                long b1 = this.b1 & clear;
                b4 |= whiteTurn() ? 0 : src | (enPassant() & ~dest);
                b3 |= dest;
                b2 |= dest;
                b1 |= 0;
                return nextBoard().set(b4, b3, b2, b1);
            }
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
            return nextBoard().set(b4, b3, b2, b1);
        }
    }

    public Board move(long src, long dest, int promotion) {
        long oldEnPassant = enPassant();
        long clear = ~(src | dest | oldEnPassant);
        long b4 = this.b4 & clear;
        long b3 = this.b3 & clear;
        long b2 = this.b2 & clear;
        long b1 = this.b1 & clear;
        b4 |= whiteTurn() ? 0 : src | oldEnPassant;
        b3 |= (promotion >> 2 & 1) * dest;
        b2 |= (promotion >> 1 & 1) * dest;
        b1 |= (promotion & 1) * dest;
        return nextBoard().set(b4, b3, b2, b1);
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

    public long pieces() {
        return (b3 | b2 | b1) & ~enPassant();
    }

    public long enPassant() {
        return b3 & b2 & b1 & EN_PASSANT;
    }

    public long castle() {
        return b3 & b2 & b1 & CASTLE;
    }

    public boolean castleLow() {
        return ((castle() & LOW_ROCK) == LOW_ROCK) && ((pieces() & (0x0E | ownKing())) == ownKing());
    }

    public boolean castleHigh() {
        return ((castle() & HIGH_ROCK) == HIGH_ROCK) && ((pieces() & (0x70 | ownKing())) == ownKing());
    }

    public int type(long piece) {
        return (int) ((((b3 & piece) / piece) & 1) << 3
                | (((b2 & piece) / piece) & 1) << 2
                | (((b1 & piece) / piece) & 1));
    }

    // Own pieces

    public long ownPieces() {
        return ~b4 & pieces();
    }

    public long ownPawns() {
        return ~b4 & ~b3 & ~b2 & b1;
    }

    public long ownRocks() {
        return ~b4 & b3 & b2 & (~b1 | CASTLE);
    }

    public long ownKnights() {
        return ~b4 & ~b3 & b2 & ~b1;
    }

    public long ownBishops() {
        return ~b4 & ~b3 & b2 & b1;
    }

    public long ownQueens() {
        return ~b4 & b3 & ~b2 & ~b1;
    }

    public long ownKing() {
        return ~b4 & b3 & ~b2 & b1;
    }

    // Enemy pieces

    public long enemyPieces() {
        return ~b4 & pieces();
    }

    public long enemyPawns() {
        return b4 & ~b3 & ~b2 & b1;
    }

    public long enemyRocks() {
        return b4 & b3 & b2 & (~b1 | CASTLE);
    }

    public long enemyKnights() {
        return b4 & ~b3 & b2 & ~b1;
    }

    public long enemyBishops() {
        return b4 & ~b3 & b2 & b1;
    }

    public long enemyQueens() {
        return b4 & b3 & ~b2 & ~b1;
    }

    public long enemyKing() {
        return b4 & b3 & ~b2 & b1;
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
        int result = (int) (b4 ^ (b4 >>> 32));
        result = 31 * result + (int) (b3 ^ (b3 >>> 32));
        result = 31 * result + (int) (b2 ^ (b2 >>> 32));
        result = 31 * result + (int) (b1 ^ (b1 >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return FenFormatter.toFen(this) + " " + leftPad(Long.toHexString(b4), 8, '0')
                + " " + leftPad(Long.toHexString(b3), 8, '0')
                + " " + leftPad(Long.toHexString(b2), 8, '0')
                + " " + leftPad(Long.toHexString(b1), 8, '0');
    }

    public String debugString() {
        String s4 = leftPad(Long.toBinaryString(b4), 64, '0');
        String s3 = leftPad(Long.toBinaryString(b3), 64, '0');
        String s2 = leftPad(Long.toBinaryString(b2), 64, '0');
        String s1 = leftPad(Long.toBinaryString(b1), 64, '0');

        StringBuilder sb = new StringBuilder();
        for (int j = 0; j < 8; j++) {
            sb.append(substring(s4, 8 * j, 8 * j + 8)).append(" ")
                    .append(substring(s3, 8 * j, 8 * j + 8)).append(" ")
                    .append(substring(s2, 8 * j, 8 * j + 8)).append(" ")
                    .append(substring(s1, 8 * j, 8 * j + 8)).append("\n");
        }
        return sb.subSequence(0, sb.length() - 1).toString();
    }
}
