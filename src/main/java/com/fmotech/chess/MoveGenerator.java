package com.fmotech.chess;

import static com.fmotech.chess.BitOperations.highestBit;
import static com.fmotech.chess.BitOperations.highestBitPosition;
import static com.fmotech.chess.BitOperations.lowestBit;
import static com.fmotech.chess.BitOperations.lowestBitPosition;
import static com.fmotech.chess.BitOperations.nextHighestBit;
import static com.fmotech.chess.BitOperations.nextLowestBit;
import static com.fmotech.chess.Board.BISHOP;
import static com.fmotech.chess.Board.KING;
import static com.fmotech.chess.Board.KNIGHT;
import static com.fmotech.chess.Board.MOVE_CAST_H;
import static com.fmotech.chess.Board.MOVE_CAST_L;
import static com.fmotech.chess.Board.MOVE_EP;
import static com.fmotech.chess.Board.MOVE_EP_CAP;
import static com.fmotech.chess.Board.MOVE_PROMO;
import static com.fmotech.chess.Board.PAWN;
import static com.fmotech.chess.Board.QUEEN;
import static com.fmotech.chess.Board.ROCK;
import static com.fmotech.chess.Board.SPECIAL;
import static com.fmotech.chess.MoveTables.BISHOP_HIGH_TABLE;
import static com.fmotech.chess.MoveTables.BISHOP_LOW_TABLE;
import static com.fmotech.chess.MoveTables.KING_TABLE;
import static com.fmotech.chess.MoveTables.KNIGHT_TABLE;
import static com.fmotech.chess.MoveTables.PAWN_ATTACK_TABLE;
import static com.fmotech.chess.MoveTables.ROCK_HIGH_TABLE;
import static com.fmotech.chess.MoveTables.ROCK_LOW_TABLE;

public class MoveGenerator {

    private static final long PAWN_RANK = 0x000000000000ff00L;
    private static final long RANK_7 = 0x00FF0000_00000000L;

    public static long countMoves(int level, Board board) {
        if (level == 0) return 1;
        long scenarios = 0;
        int[] moves = board.moves();
        int counter = generateDirtyMoves(board, moves);
        for (int i = 0; i < counter; i++) {
            Board nextBoard = board.move(moves[i]);
            int kingPosition = lowestBitPosition(nextBoard.ownKing());
            if (!isPositionInAttack(nextBoard, kingPosition)) {
                scenarios += countMoves(level - 1, nextBoard.nextTurn());
            }
        }
        return scenarios;
    }

    public static int generateValidMoves(Board board, int[] moves) {
        int counter = generateDirtyMoves(board, moves);
        int index = 0;
        for (int i = 0; i < counter; i++) {
            Board nextBoard = board.move(moves[i]);
            int kingPosition = lowestBitPosition(nextBoard.ownKing());
            if (!isPositionInAttack(nextBoard, kingPosition)) {
                moves[index++] = moves[i];
            }
        }
        return index;
    }

    public static int generateDirtyMoves(Board board, int[] moves) {
        int counter = 0;
        counter = generatePawnMoves(board, counter, moves);
        counter = generateRocksMoves(board, counter, moves);
        counter = generateKnightMoves(board, counter, moves);
        counter = generateBishopsMoves(board, counter, moves);
        counter = generateQueensMoves(board, counter, moves);
        counter = generateKingMoves(board, counter, moves);
        return counter;
    }

    private static int generatePawnMoves(Board board, int counter, int[] moves) {
        long pieces = board.pieces() | board.enPassant();
        long pawns = board.ownPawns();
        while (pawns != 0) {
            long pawn = lowestBit(pawns);
            int srcPos = lowestBitPosition(pawns);
            int promote = (pawn & RANK_7) != 0 ? MOVE_PROMO : 0;
            long next = pawn << 8;//PAWN[pos];
            if ((pieces & next) == 0) {
                int tgtPos = lowestBitPosition(next);
                counter = createPawnMove(moves, counter, srcPos, tgtPos, 0, promote);
                if ((pawn & PAWN_RANK) != 0) {
                    next <<= 8;
                    tgtPos = lowestBitPosition(next);
                    if ((pieces & next) == 0) {
                        counter = createPawnMove(moves, counter, srcPos, tgtPos, 0, MOVE_EP);
                    }
                }
            }
            next = PAWN_ATTACK_TABLE[srcPos];
            while (next != 0) {
                long pawnAttack = lowestBit(next);
                if ((pawnAttack & pieces) != 0 && board.enemy(pawnAttack)) {
                    int tgtPos = lowestBitPosition(next);
                    int capture = board.type(tgtPos, ROCK, SPECIAL);
                    counter = createPawnMove(moves, counter, srcPos, tgtPos, capture, promote);
                }
                next = nextLowestBit(next);
            }
            pawns = nextLowestBit(pawns);
        }
        return counter;
    }

    private static int generateRocksMoves(Board board, int counter, int[] moves) {
        long rocks = board.ownRocks();
        while (rocks != 0) {
            int pos = lowestBitPosition(rocks);
            long mask = generateLinearMoveFor(board, pos, ROCK_HIGH_TABLE, ROCK_LOW_TABLE);
            counter = createMove(moves, counter, board, pos, ROCK, mask);
            rocks = nextLowestBit(rocks);
        }
        return counter;
    }

    private static int generateKnightMoves(Board board, int counter, int[] moves) {
        return generateTargetMoves(board, board.ownKnights(), KNIGHT_TABLE, KNIGHT, counter, moves);
    }

    private static int generateBishopsMoves(Board board, int counter, int[] moves) {
        long bishops = board.ownBishops();
        while (bishops != 0) {
            int pos = lowestBitPosition(bishops);
            long mask = generateLinearMoveFor(board, pos, BISHOP_HIGH_TABLE, BISHOP_LOW_TABLE);
            counter = createMove(moves, counter, board, pos, BISHOP, mask);
            bishops = nextLowestBit(bishops);
        }
        return counter;
    }

    private static int generateQueensMoves(Board board, int counter, int[] moves) {
        long queens = board.ownQueens();
        while (queens != 0) {
            int pos = lowestBitPosition(queens);
            long mask = generateLinearMoveFor(board, pos, ROCK_HIGH_TABLE, ROCK_LOW_TABLE)
                    | generateLinearMoveFor(board, pos, BISHOP_HIGH_TABLE, BISHOP_LOW_TABLE);
            counter = createMove(moves, counter, board, pos, QUEEN, mask);
            queens = nextLowestBit(queens);
        }
        return counter;
    }

    private static int generateKingMoves(Board board, int counter, int[] moves) {
        long king = board.ownKing();
        int kingPos = lowestBitPosition(king);
        counter = generateTargetMoves(board, king, KING_TABLE, KING, counter, moves);
        if (board.castleLow() && !isPositionInAttack(board, kingPos) && !isPositionInAttack(board, kingPos - 1)
                && !isPositionInAttack(board, kingPos - 2)) {
            moves[counter++] = createMove(kingPos, kingPos - 2, KING, 0, MOVE_CAST_L);
        }
        if (board.castleHigh() && !isPositionInAttack(board, kingPos) && !isPositionInAttack(board, kingPos + 1)
                && !isPositionInAttack(board, kingPos + 2)) {
            moves[counter++] = createMove(kingPos, kingPos + 2, KING, 0, MOVE_CAST_H);
        }
        return counter;
    }

    private static long generateLinearMoveFor(Board board, int pos, long[] highTable, long[] lowTable) {
        long pieces = board.pieces();
        long move = 0;
        long next = highTable[pos];
        move |= next;
        next &= pieces;
        while (next != 0) {
            long contact = lowestBit(next);
            int contactPos = lowestBitPosition(contact);
            long mask = ~highTable[contactPos];
            move &= mask;
            if (board.own(contact)) move ^= contact;
            next &= mask;
            next = nextLowestBit(next);
        }
        next = lowTable[pos];
        move |= next;
        next &= pieces;
        while (next != 0) {
            long contact = highestBit(next);
            int contactPos = highestBitPosition(contact);
            long mask = ~lowTable[contactPos];
            move &= mask;
            if (board.own(contact)) move ^= contact;
            next &= mask;
            next = nextHighestBit(next);
        }
        return move;
    }

    private static int generateTargetMoves(Board board, long pieces, long[] table, int srcType, int counter, int[] moves) {
        long mask = ~board.ownPieces();
        while (pieces != 0) {
            int srcPos = lowestBitPosition(pieces);
            counter = createMove(moves, counter, board, srcPos, srcType, table[srcPos] & mask);
            pieces = nextLowestBit(pieces);
        }
        return counter;
    }

    private static int createPawnMove(int[] moves, int counter, int srcPos, int tgtPos, int tgtType, int flags) {
        if (flags == MOVE_PROMO) {
            moves[counter++] = createMove(srcPos, tgtPos, PAWN, tgtType, MOVE_PROMO | QUEEN);
            moves[counter++] = createMove(srcPos, tgtPos, PAWN, tgtType, MOVE_PROMO | ROCK);
            moves[counter++] = createMove(srcPos, tgtPos, PAWN, tgtType, MOVE_PROMO | BISHOP);
            moves[counter++] = createMove(srcPos, tgtPos, PAWN, tgtType, MOVE_PROMO | KNIGHT);
        } else if (tgtType == SPECIAL) {
            moves[counter++] = createMove(srcPos, tgtPos, PAWN, PAWN, MOVE_EP_CAP);
        } else {
            moves[counter++] = createMove(srcPos, tgtPos, PAWN, tgtType, flags);
        }
        return counter;
    }

    private static int createMove(int[] moves, int counter, Board board, int srcPos, int srcType, long mask) {
        long next = mask;
        while (next != 0) {
            int tgtPos = lowestBitPosition(next);
            int tgtType = board.type(tgtPos, ROCK, 0);
            moves[counter++] = createMove(srcPos, tgtPos, srcType, tgtType, 0);
            next = nextLowestBit(next);
        }
        return counter;
    }

    private static int createMove(int srcPos, int tgtPos, int srcType, int tgtType, int flags) {
        return flags << 24 | tgtType << 20 | srcType << 16 | tgtPos << 8 | srcPos;
    }

    public static boolean isPositionInAttack(Board board, int pos) {
        long pieces = board.pieces();
        if ((PAWN_ATTACK_TABLE[pos] & board.enemyPawns()) != 0) return true;
        if ((KNIGHT_TABLE[pos] & board.enemyKnights()) != 0) return true;
        if ((KING_TABLE[pos] & board.enemyKing()) != 0) return true;
        long rocksQueens = board.enemyRocks() | board.enemyQueens();
        if (isPositionInAttackHigh(pos, rocksQueens, pieces, ROCK_HIGH_TABLE)) return true;
        if (isPositionInAttackLow(pos, rocksQueens, pieces, ROCK_LOW_TABLE)) return true;
        long bishopsQueens = board.enemyBishops() | board.enemyQueens();
        if (isPositionInAttackHigh(pos, bishopsQueens, pieces, BISHOP_HIGH_TABLE)) return true;
        if (isPositionInAttackLow(pos, bishopsQueens, pieces, BISHOP_LOW_TABLE)) return true;
        return false;
    }

    private static boolean isPositionInAttackHigh(int pos, long targetPieces, long pieces, long[] highTable) {
        if ((highTable[pos] & targetPieces) != 0) {
            long next = highTable[pos] & pieces;
            while (next != 0) {
                long contact = lowestBit(next);
                if ((contact & targetPieces) != 0) return true;
                next &= ~highTable[lowestBitPosition(contact)];
                next = nextLowestBit(next);
            }
        }
        return false;
    }

    private static boolean isPositionInAttackLow(int pos, long targetPieces, long pieces, long[] lowTable) {
        if ((lowTable[pos] & targetPieces) != 0) {
            long next = lowTable[pos] & pieces;
            while (next != 0) {
                long contact = highestBit(next);
                if ((contact & targetPieces) != 0) return true;
                next &= ~lowTable[highestBitPosition(contact)];
                next = nextHighestBit(next);
            }
        }
        return false;
    }
}
