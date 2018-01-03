package com.fmotech.chess.ai.mediocre;

import com.fmotech.chess.Board;
import com.fmotech.chess.MoveTables;
import com.fmotech.chess.Moves;
import com.fmotech.chess.ai.Evaluation;

import static com.fmotech.chess.BitOperations.bitCount;
import static com.fmotech.chess.BitOperations.lowestBitPosition;
import static com.fmotech.chess.BitOperations.sparseBitCount;
import static com.fmotech.chess.Moves.BATT3;
import static com.fmotech.chess.Moves.BATT4;
import static com.fmotech.chess.Moves.RATT1;
import static com.fmotech.chess.Moves.RATT2;
import static com.fmotech.chess.ai.EvaluationUtils.pinnedPieces;

public class OliThinkEvaluation implements Evaluation {

    final static int PAWN = 1;
    final static int KNIGHT = 2;
    final static int KING = 3;
    final static int BISHOP = 5;
    final static int ROOK = 6;
    final static int QUEEN = 7;

    final static int[] nmobil = new int[64];
    final static int[] kmobil = new int[64];
    final static long[] nmoves = new long[64];
    final static long[] kmoves = new long[64];
    final static long[][] pawnfree = new long[2][64];
    final static long[][] pawnfile = new long[2][64];
    final static long[][] pawnhelp = new long[2][64];
    final static int[][] pawnprg = new int[2][64];
    final static int pval[] = {0, 100, 290, 0, 100, 310, 500, 950};

    private static long BIT(int f) { return 1L << f; }
    static boolean TEST(int f, long b) { return (BIT(f) & (b)) != 0; }

    static void _init_shorts(long[] moves, int[] m) {
        int i, j, n;
        for (i = 0; i < 64; i++) {
            for (j = 0; j < 8; j++) {
                n = i + m[j];
                if (n < 64 && n >= 0 && ((n & 7)-(i & 7))*((n & 7)-(i & 7)) <= 4) {
                    moves[i] |= 1L << n;
                }
            }
        }
    }

    static void _init_pawns(long[] freep, long[] filep, long[] helpp, int c) {
        final int pawnrun[] = {0, 0, 1, 8, 16, 32, 64, 128};
        int i, j;
        for (i = 0; i < 64; i++) {
            int rank = i/8;
            int file = i&7;
            int m = i + (c == 1 ? -8 : 8);
            pawnprg[c][i] = pawnrun[c == 1 ? 7-rank : rank];
            for (j = 0; j < 64; j++) {
                int jrank = j/8;
                int jfile = j&7;
                int dfile = (jfile - file)*(jfile - file);
                if (dfile > 1) continue;
                if ((c == 1 && jrank < rank) || (c == 0 && jrank > rank)) {//The not touched half of the pawn
                    if (dfile == 0) filep[i] |= BIT(j);
                    freep[i] |= BIT(j);
                } else if (dfile != 0 && (jrank - rank)*(jrank - rank) <= 1) {
                    helpp[i] |= BIT(j);
                }
            }
            if (m < 0 || m > 63) continue;
            if (file > 0) {
                m = i + (c == 1 ? -9 : 7);
                if (m < 0 || m > 63) continue;
            }
            if (file < 7) {
                m = i + (c == 1 ? -7 : 9);
                if (m < 0 || m > 63) continue;
            }
        }
    }

    static {
        final int[] _knight = {-17,-10,6,15,17,10,-6,-15};
        final int[] _king = {-9,-1,7,8,9,1,-7,-8};

        for (int i = 0; i < 64; i++) nmobil[i] = (bitCount(nmoves[i])-1)*6;
        for (int i = 0; i < 64; i++) kmobil[i] = (bitCount(nmoves[i])/2)*2;

        _init_shorts(nmoves, _knight);
        _init_shorts(kmoves, _king);
        _init_pawns(pawnfree[0], pawnfile[0], pawnhelp[0], 0);
        _init_pawns(pawnfree[1], pawnfile[1], pawnhelp[1], 1);
    }

    private int[] kingpos = new int[2];
    private long[] colorb = new long[2];
    private long[] pieceb = new long[8];

    @Override
    public int evaluateBoardPosition(Board board, int alpha, int beta) {
        kingpos[0] = lowestBitPosition(board.ownKing());
        kingpos[1] = lowestBitPosition(board.enemyKing());
        colorb[0] = board.ownPieces();
        colorb[1] = board.enemyPieces();
        pieceb[PAWN] = board.pawns();
        pieceb[KNIGHT] = board.knights();
        pieceb[BISHOP] = board.bishops();
        pieceb[ROOK] = board.rooks();
        pieceb[QUEEN] = board.queens();
        pieceb[KING] = board.kings();

        return eval(0, board);
    }

    private int[] sfp = new int[1];
    public int eval(int c, Board board) {
        int sf0 = 0, sf1 = 0;
        sfp[0] = sf0;
        int ev0 = evalc(0, sfp, pinnedPieces(board, board.ownPieces()));
        sf0 = sfp[0];
        sfp[0] = sf1;
        int ev1 = evalc(1, sfp, pinnedPieces(board, board.enemyPieces()));
        sf1 = sfp[0];

        if (sf1 < 6) ev0 += kmobil[kingpos[0]]*(6-sf1);
        if (sf0 < 6) ev1 += kmobil[kingpos[1]]*(6-sf0);

        return (c != 0 ? (ev1 - ev0) : (ev0 - ev1));
    }

    /* The evaluation for Color c. It's only mobility stuff. Pinned pieces are still awarded for limiting opposite's king */
    private int evalc(int c, int[] sf, long pin) {
        int f;
        int mn = 0, katt = 0;
        int oc = c^1;
        long ocb = colorb[oc];
        long m, b, a, cb;
        long kn = MoveTables.KING_TABLE[kingpos[oc]];

        b = pieceb[PAWN] & colorb[c];
        while (b != 0) {
            int ppos = 0;
            f = lowestBitPosition(b);
            b ^= BIT(f);
            ppos = pawnprg[c][f];
            m = (c == 0 ? MoveTables.PAWN_HIGH_TABLE : MoveTables.PAWN_LOW_TABLE)[f];
            a = (c == 0 ? MoveTables.PAWN_ATTACK_HIGH_TABLE : MoveTables.PAWN_ATTACK_LOW_TABLE)[f];
            if ((a & kn) != 0) katt += sparseBitCount(a & kn) << 4;
            if ((BIT(f) & pin) != 0) {
                if ((getDir(f, kingpos[c]) & 16) == 0) m = 0;
            } else {
                ppos += sparseBitCount(a & pieceb[PAWN] & colorb[c]) << 2;
            }
            if (m != 0) ppos += 8; else ppos -= 8;
            if ((pawnfile[c][f] & pieceb[PAWN] & ocb) == 0) { //Free file?
                if ((pawnfree[c][f] & pieceb[PAWN] & ocb) == 0) ppos *= 2; //Free run?
                if ((pawnhelp[c][f] & pieceb[PAWN] & colorb[c]) == 0) ppos -= 33; //Hanging backpawn?
            }

            mn += ppos;
        }

        cb = colorb[c] & (~pin);
        b = pieceb[KNIGHT] & cb;
        while (b != 0) {
            sf[0] += 1;
            f = lowestBitPosition(b);
            b ^= BIT(f);
            a = nmoves[f];
            if ((a & kn) != 0) katt += sparseBitCount(a & kn) << 4;
            mn += nmobil[f];
        }

        b = pieceb[KNIGHT] & pin;
        while (b != 0) {
            sf[0] += 1;
            f = lowestBitPosition(b);
            b ^= BIT(f);
            a = nmoves[f];
            if ((a & kn) != 0) katt += sparseBitCount(a & kn) << 4;
        }

        colorb[oc] ^= BIT(kingpos[oc]); //Opposite King doesn't block mobility at all
        b = pieceb[QUEEN] & cb;
        while (b != 0) {
            sf[0] += 4;
            f = lowestBitPosition(b);
            b ^= BIT(f);
            long occupied = colorb[0] | colorb[1];
            a = RATT1(f, occupied) | RATT2(f, occupied) | BATT3(f, occupied) | BATT4(f, occupied);
            if ((a & kn) != 0) katt += sparseBitCount(a & kn) << 4;
            mn += bitCount(a);
        }

        colorb[oc] ^= (pieceb[QUEEN] | pieceb[ROOK]) & ocb; //Opposite Queen & Rook doesn't block mobility for bishop
        b = pieceb[BISHOP] & cb;
        while (b != 0) {
            sf[0] += 1;
            f = lowestBitPosition(b);
            b ^= BIT(f);
            long occupied = colorb[0] | colorb[1];
            a = BATT3(f, occupied) | BATT4(f, occupied);
            if ((a & kn) != 0) katt += sparseBitCount(a & kn) << 4;
            mn += bitCount(a) << 3;
        }

        colorb[oc] ^= pieceb[ROOK] & ocb; //Opposite Queen doesn't block mobility for rook.
        colorb[c] ^= pieceb[ROOK] & cb; //Own non-pinned Rook doesn't block mobility for rook.
        b = pieceb[ROOK] & cb;
        while (b != 0) {
            sf[0] += 2;
            f = lowestBitPosition(b);
            b ^= BIT(f);
            long occupied = colorb[0] | colorb[1];
            a = RATT1(f, 0) | RATT2(f, occupied);
            if ((a & kn) != 0) katt += sparseBitCount(a & kn) << 4;
            mn += bitCount(a) << 2;
        }

        colorb[c] ^= pieceb[ROOK] & cb; // Back
        b = pin & (pieceb[ROOK] | pieceb[BISHOP] | pieceb[QUEEN]);
        while (b != 0) {
            f = lowestBitPosition(b);
            b ^= BIT(f);
            int p = identPiece(f);
            long occupied = colorb[0] | colorb[1];
            if (p == BISHOP) {
                sf[0] += 1;
                a = BATT3(f, occupied) | BATT4(f, occupied);
                if ((a & kn) != 0) katt += sparseBitCount(a & kn) << 4;
            } else if (p == ROOK) {
                sf[0] += 2;
                a = RATT1(f, occupied) | RATT2(f, occupied);
                if ((a & kn) != 0) katt += sparseBitCount(a & kn) << 4;
            } else {
                sf[0] += 4;
                a = RATT1(f, occupied) | RATT2(f, occupied) | BATT3(f, occupied) | BATT4(f, occupied);
                if ((a & kn) != 0) katt += sparseBitCount(a & kn) << 4;
            }
            int t = p | getDir(f, kingpos[c]);
            if ((t & 10) == 10) mn += bitCount(RATT1(f, occupied));
            if ((t & 18) == 18) mn += bitCount(RATT2(f, occupied));
            if ((t & 33) == 33) mn += bitCount(BATT3(f, occupied));
            if ((t & 65) == 65) mn += bitCount(BATT4(f, occupied));
        }

        colorb[oc] ^= pieceb[QUEEN] & ocb; //Back
        colorb[oc] ^= BIT(kingpos[oc]); //Back
        if (sf[0] == 1 && (pieceb[PAWN] & colorb[c]) == 0) mn =- 200; //No mating material
        if (sf[0] < 7) katt = katt * sf[0] / 7; //Reduce the bonus for attacking king squares
        if (sf[0] < 2) sf[0] = 2;

        int ma = 0;
        ma += pval[PAWN] * bitCount(pieceb[PAWN] & colorb[c]);
        ma += pval[KNIGHT] * bitCount(pieceb[KNIGHT] & colorb[c]);
        ma += pval[BISHOP] * bitCount(pieceb[BISHOP] & colorb[c]);
        ma += pval[ROOK] * bitCount(pieceb[ROOK] & colorb[c]);
        ma += pval[QUEEN] * bitCount(pieceb[QUEEN] & colorb[c]);
        return ma + mn + katt;
    }

    int identPiece(int f) {
        if (TEST(f, pieceb[PAWN])) return PAWN;
        if (TEST(f, pieceb[KNIGHT])) return KNIGHT;
        if (TEST(f, pieceb[BISHOP])) return BISHOP;
        if (TEST(f, pieceb[ROOK])) return ROOK;
        if (TEST(f, pieceb[QUEEN])) return QUEEN;
        if (TEST(f, pieceb[KING])) return KING;
        return 0;
    }

    static byte getDir(int f, int t) {
        if (((f ^ t) & 56) == 0) return 8;
        if (((f ^ t) & 7) == 0) return 16;
        return (byte)(((f - t) % 7) != 0 ? 32 : 64);
    }
}
