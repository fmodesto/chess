package com.fmotech.chess.ai;

import com.fmotech.chess.Board;
import com.fmotech.chess.Move;

import static com.fmotech.chess.BitOperations.lowestBit;
import static com.fmotech.chess.Board.KNIGHT;
import static com.fmotech.chess.Board.PAWN;
import static com.fmotech.chess.Move.MOVE_EP_CAP;
import static com.fmotech.chess.Move.hasFlag;
import static com.fmotech.chess.Move.srcType;
import static com.fmotech.chess.MoveGenerator.slidingAttacks;
import static com.fmotech.chess.MoveGenerator.slidingDirection;
import static com.fmotech.chess.MoveTables.KING_TABLE;
import static com.fmotech.chess.MoveTables.KNIGHT_TABLE;
import static com.fmotech.chess.MoveTables.PAWN_ATTACK_HIGH_TABLE;
import static com.fmotech.chess.MoveTables.PAWN_ATTACK_LOW_TABLE;

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

        long pieces = hasFlag(move, MOVE_EP_CAP) ? board.pieces() ^ (tgt >>> 8) : board.pieces() ^ tgt;
        gain[depth] = hasFlag(move, MOVE_EP_CAP) ? value[PAWN] : value[board.type(tgt)];
        int type = srcType(move);

        long attackers = attackersTo(board, tgt, tgtPos, pieces);
        long color = board.ownPieces();

        do {
            depth++;
            color = ~color;
            gain[depth] = value[type] - gain[depth - 1];

            attackers ^= src;
            pieces ^= src;
            if (type != KNIGHT)
                attackers |= updateAttackers(board, tgtPos, tgt, src, pieces);

            src = getLeastValuablePiece(board, attackers, color);
            type = board.type(src);
        } while (src != 0);

        while (--depth > 0)
            gain[depth - 1] = -Math.max(-gain[depth - 1], gain[depth]);

        return gain[0];
    }

    private static long getLeastValuablePiece(Board board, long attadef, long color) {
        long attackers = attadef & color;
        if ((attackers) == 0) return 0;
        if ((attackers & board.pawns()) != 0) return lowestBit(attackers & board.pawns());
        if ((attackers & board.knights()) != 0) return lowestBit(attackers & board.knights());
        if ((attackers & board.bishops()) != 0) return lowestBit(attackers & board.bishops());
        if ((attackers & board.rocks()) != 0) return lowestBit(attackers & board.rocks());
        if ((attackers & board.queens()) != 0) return lowestBit(attackers & board.queens());
        if ((attackers & board.kings()) != 0) return lowestBit(attackers & board.kings());
        return 0;
    }

    private static long attackersTo(Board board, long tgt, int pos, long pieces) {
        long attackers = 0;
        attackers |= (board.enemyPawns() & PAWN_ATTACK_HIGH_TABLE[pos]);
        attackers |= (board.ownPawns() & PAWN_ATTACK_LOW_TABLE[pos]);
        attackers |= board.knights() & KNIGHT_TABLE[pos];
        attackers |= board.kings() & KING_TABLE[pos];
        for (int i = 0; i < 8; i += 2)
            attackers |= (board.bishops() | board.queens()) & slidingAttacks(tgt, ~pieces, i);
        for (int i = 1; i < 8; i += 2)
            attackers |= (board.rocks() | board.queens()) & slidingAttacks(tgt, ~pieces, i);
        return attackers & pieces;
    }

    private static long updateAttackers(Board board, int pos, long tgt, long last, long pieces) {
        int dir8 = slidingDirection(pos, last);
        long attackers = (dir8 & 1) != 0 ? board.rocks() | board.queens() : board.bishops() | board.queens();
        return attackers & slidingAttacks(tgt, ~pieces, dir8) & pieces;
    }
}
