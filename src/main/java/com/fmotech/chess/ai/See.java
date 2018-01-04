package com.fmotech.chess.ai;

import com.fmotech.chess.Board;
import com.fmotech.chess.Move;
import com.fmotech.chess.MoveTables;

import static com.fmotech.chess.BitOperations.lowestBit;
import static com.fmotech.chess.Board.KNIGHT;
import static com.fmotech.chess.Board.ROOK;
import static com.fmotech.chess.Board.SPECIAL;
import static com.fmotech.chess.Move.srcType;
import static com.fmotech.chess.MoveTables.BATT3;
import static com.fmotech.chess.MoveTables.BATT4;
import static com.fmotech.chess.MoveTables.RATT1;
import static com.fmotech.chess.MoveTables.RATT2;

public class See {

    private static final int[] value = new int[]{
            0, 100, 325, 325, 9900, 500, 900
    };

    private static int[] gain = new int[32];

    public static int see(Board board, int move) {
        if (Move.tgtType(move) == 0)
            return 0;

        int tgtPos = Move.tgtPos(move);
        long src = 1L << Move.srcPos(move);
        long tgt = 1L << tgtPos;

        int depth = 0;

        boolean enPassant = board.type(tgtPos, ROOK, SPECIAL) == SPECIAL;
        long pieces = enPassant ? board.pieces() ^ board.enemyEnPassantPawn() : board.pieces() ^ tgt;

        gain[depth] = value[Move.tgtType(move)];
        int type = srcType(move);

        long attackers = attackersTo(board, tgtPos, pieces);
        long color = board.ownPieces();

        long rockMask = RATT1(tgtPos, 0) | RATT2(tgtPos, 0);
        long lowMask = RATT1(tgtPos, 0) | BATT3(tgtPos, 0);

        do {
            depth++;
            color = ~color;
            gain[depth] = value[type] - gain[depth - 1];

            attackers ^= src;
            pieces ^= src;
            if (type != KNIGHT)
                attackers |= updateAttackers(board, tgtPos, src, pieces, rockMask, lowMask);

            src = getLeastValuablePiece(board, attackers, color);
            type = board.type(src);
        } while (src != 0);

        while (--depth > 0)
            gain[depth - 1] = -Math.max(-gain[depth - 1], gain[depth]);

        return gain[0];
    }

    private static long attackersTo(Board board, int pos, long pieces) {
        long attackers = 0;
        attackers |= (board.enemyPawns() & MoveTables.PAWN_ATTACK[0][pos]);
        attackers |= (board.ownPawns() & MoveTables.PAWN_ATTACK[1][pos]);
        attackers |= board.knights() & MoveTables.KNIGHT[pos];
        attackers |= board.kings() & MoveTables.KING[pos];
        attackers |= (board.rooksQueens()) & (RATT1(pos, pieces) | RATT2(pos, pieces));
        attackers |= (board.bishopsQueens()) & (BATT3(pos, pieces) | BATT4(pos, pieces));
        return attackers & pieces;
    }

    private static long getLeastValuablePiece(Board board, long attadef, long color) {
        long attackers = attadef & color;
        if ((attackers) == 0) return 0;
        if ((attackers & board.pawns()) != 0) return lowestBit(attackers & board.pawns());
        if ((attackers & board.knights()) != 0) return lowestBit(attackers & board.knights());
        if ((attackers & board.bishops()) != 0) return lowestBit(attackers & board.bishops());
        if ((attackers & board.rooks()) != 0) return lowestBit(attackers & board.rooks());
        if ((attackers & board.queens()) != 0) return lowestBit(attackers & board.queens());
        if ((attackers & board.kings()) != 0) return lowestBit(attackers & board.kings());
        return 0;
    }

    private static long updateAttackers(Board board, int pos, long last, long pieces, long rockMask, long lowMask) {
        if ((last & rockMask) != 0) {
            if ((last & lowMask) != 0)
                return RATT1(pos, pieces) & pieces & (board.rooks() | board.queens());
            else
                return RATT2(pos, pieces) & pieces & (board.rooks() | board.queens());
        } else {
            if ((last & lowMask) != 0)
                return BATT3(pos, pieces) & pieces & (board.bishops() | board.queens());
            else
                return BATT4(pos, pieces) & pieces & (board.bishops() | board.queens());
        }
    }
}
