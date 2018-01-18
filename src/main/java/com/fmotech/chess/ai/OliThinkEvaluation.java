package com.fmotech.chess.ai;

import com.fmotech.chess.Board;
import com.fmotech.chess.MoveGenerator;
import com.fmotech.chess.MoveTables;
import com.fmotech.chess.Utils;

import static com.fmotech.chess.BitOperations.bitCount;
import static com.fmotech.chess.BitOperations.highInt;
import static com.fmotech.chess.BitOperations.joinInts;
import static com.fmotech.chess.BitOperations.lowInt;
import static com.fmotech.chess.BitOperations.lowestBitPosition;
import static com.fmotech.chess.BitOperations.nextLowestBit;
import static com.fmotech.chess.BitOperations.northFill;
import static com.fmotech.chess.BitOperations.southFill;
import static com.fmotech.chess.BitOperations.sparseBitCount;
import static com.fmotech.chess.KoggeStone.E;
import static com.fmotech.chess.KoggeStone.N;
import static com.fmotech.chess.KoggeStone.NE;
import static com.fmotech.chess.KoggeStone.NW;
import static com.fmotech.chess.KoggeStone.S;
import static com.fmotech.chess.KoggeStone.SE;
import static com.fmotech.chess.KoggeStone.SW;
import static com.fmotech.chess.KoggeStone.W;
import static com.fmotech.chess.KoggeStone.shiftOne;
import static com.fmotech.chess.MoveGenerator.pinnedPieces;
import static com.fmotech.chess.MoveTables.DIR;
import static com.fmotech.chess.MoveTables.MASK;
import static com.fmotech.chess.Utils.BIT;
import static com.fmotech.chess.Utils.RANK;
import static com.fmotech.chess.Utils.TEST;
import static java.util.Arrays.asList;
import static java.util.stream.IntStream.range;

public class OliThinkEvaluation implements Evaluation {

    private static final int[] KNIGHT_MOBILITY = range(0, 64).map(i -> (bitCount(MoveTables.KNIGHT[i]) - 1) * 6).toArray();
    private static final int[] KING_MOBILITY = range(0, 64).map(i -> (bitCount(MoveTables.KNIGHT[i]) / 2) * 2).toArray();

    private static final long[][] PAWN_FREE = new long[][] {
            range(0, 64).mapToLong(Utils::BIT).map(b -> northFill(shiftOne(b, NW) | shiftOne(b, N) | shiftOne(b, NE))).toArray(),
            range(0, 64).mapToLong(Utils::BIT).map(b -> southFill(shiftOne(b, SW) | shiftOne(b, S) | shiftOne(b, SE))).toArray() };
    private static final long[][] PAWN_FILE = new long[][] {
            range(0, 64).mapToLong(Utils::BIT).map(b -> northFill(shiftOne(b, N))).toArray(),
            range(0, 64).mapToLong(Utils::BIT).map(b -> southFill(shiftOne(b, S))).toArray() };
    private static final long[][] PAWN_HELP = new long[][] {
            range(0, 64).mapToLong(Utils::BIT).map(b -> shiftOne(b, W) | shiftOne(b, SW) | shiftOne(b, SE) | shiftOne(b, E)).toArray(),
            range(0, 64).mapToLong(Utils::BIT).map(b -> shiftOne(b, W) | shiftOne(b, NW) | shiftOne(b, NE) | shiftOne(b, E)).toArray() };
    private static final int[][] PAWN_RUN = new int[][] {
            range(0, 64).map(i -> asList(0, 0, 1, 8, 16, 32, 64, 128).get(RANK(i))).toArray(),
            range(0, 64).map(i -> asList(0, 0, 1, 8, 16, 32, 64, 128).get(7 - RANK(i))).toArray() };

    @Override
    public int evaluateBoardPosition(Board board, int alpha, int beta) {
        int ownKing = lowestBitPosition(board.ownKing());
        int enemyKing = lowestBitPosition(board.enemyKing());

        long dataWhite = evaluateSide(board, 0, ownKing, enemyKing, board.ownPieces(), board.enemyPieces());
        int pieceWhite = highInt(dataWhite);
        int evaluationWhite = lowInt(dataWhite);

        long dataBlack = evaluateSide(board, 1, enemyKing, ownKing, board.enemyPieces(), board.ownPieces());
        int pieceBlack = highInt(dataBlack);
        int evaluationBlack = lowInt(dataBlack);

        if (pieceBlack < 6) evaluationWhite += KING_MOBILITY[ownKing] * (6 - pieceBlack);
        if (pieceWhite < 6) evaluationBlack += KING_MOBILITY[enemyKing] * (6 - pieceWhite);

        return evaluationWhite - evaluationBlack;
    }

    static long evaluateSide(Board board, int side, int ownKing, int enemyKing, long own, long enemy) {
        int pieceValue = 0;
        int mobility = 0;
        int kingAttack = 0;

        long kingSurrounds = MoveTables.KING[enemyKing];
        long pin = pinnedPieces(ownKing, board, own, enemy);

        long pieces = own | enemy;

        long next = board.pawns() & own;
        while (next != 0) {
            int from = lowestBitPosition(next);
            int ppos = PAWN_RUN[side][from];
            long m = BIT(from + (side == 1 ? -8 : 8)) & ~pieces;
            long attack = MoveTables.PAWN_ATTACK[side][from] & pieces;
            if ((attack & kingSurrounds) != 0) kingAttack += sparseBitCount(attack & kingSurrounds) << 4;
            if (TEST(from, pin)) {
                if (DIR(from, ownKing) != 2) m = 0;
            } else {
                ppos += sparseBitCount(attack & board.pawns() & own) << 2;
            }
            if (m != 0) ppos += 8; else ppos -= 8;
            if ((PAWN_FILE[side][from] & board.pawns() & enemy) == 0) { //Free file?
                if ((PAWN_FREE[side][from] & board.pawns() & enemy) == 0) ppos *= 2; //Free run?
                if ((PAWN_HELP[side][from] & board.pawns() & own) == 0) ppos -= 33; //Hanging backpawn?
            }
            mobility += ppos;
            next = nextLowestBit(next);
        }

        next = board.knights() & own;
        while (next != 0) {
            pieceValue += 1;
            int from = lowestBitPosition(next);
            long attack = MoveTables.KNIGHT[from];
            if ((attack & kingSurrounds) != 0) kingAttack += sparseBitCount(attack & kingSurrounds) << 4;
            if (!TEST(from, pin)) mobility += KNIGHT_MOBILITY[from];
            next = nextLowestBit(next);
        }

        pieces ^= BIT(enemyKing); //Opposite King doesn't block mobility at all
        next = board.queens() & own;
        while (next != 0) {
            pieceValue += 4;
            int from = lowestBitPosition(next);
            long attack = MoveGenerator.queenMove(from, pieces);
            if ((attack & kingSurrounds) != 0) kingAttack += sparseBitCount(attack & kingSurrounds) << 4;
            mobility += TEST(from, pin) ? bitCount(attack & MASK(from, ownKing)) : bitCount(attack);
            next = nextLowestBit(next);
        }

        pieces ^= board.rooksQueens() & enemy; //Opposite Queen & Rook doesn't block mobility for bishop
        next = board.bishops() & own;
        while (next != 0) {
            pieceValue += 1;
            int from = lowestBitPosition(next);
            long attack = MoveGenerator.bishopMove(from, pieces);
            if ((attack & kingSurrounds) != 0) kingAttack += sparseBitCount(attack & kingSurrounds) << 4;
            mobility += TEST(from, pin) ? bitCount(attack & MASK(from, ownKing)) : bitCount(attack) << 3;
            next = nextLowestBit(next);
        }

        pieces ^= board.rooks() & enemy; //Opposite Queen doesn't block mobility for rook.
        pieces ^= board.rooks() & own & ~pin; //Own non-pinned Rook doesn't block mobility for rook.
        next = board.rooks() & own;
        while (next != 0) {
            pieceValue += 2;
            int from = lowestBitPosition(next);
            long attack = MoveGenerator.rookMove(from, pieces);
            if ((attack & kingSurrounds) != 0) kingAttack += sparseBitCount(attack & kingSurrounds) << 4;
            mobility += TEST(from, pin) ? bitCount(attack & MASK(from, ownKing)) : bitCount(attack) << 2;
            next = nextLowestBit(next);
        }

        if (pieceValue == 1 && (board.pawns() & own) == 0) mobility = -200; //No mating material
        if (pieceValue < 7) kingAttack = kingAttack * pieceValue / 7; //Reduce the bonus for attacking king squares
        if (pieceValue < 2) pieceValue = 2;
        return joinInts(pieceValue, mobility + kingAttack);
    }
}
