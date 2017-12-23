package com.fmotech.chess;

import static com.fmotech.chess.BitOperations.highestBit;
import static com.fmotech.chess.BitOperations.highestBitPosition;
import static com.fmotech.chess.BitOperations.lowestBit;
import static com.fmotech.chess.BitOperations.lowestBitPosition;
import static com.fmotech.chess.BitOperations.nextHighestBit;
import static com.fmotech.chess.BitOperations.nextLowestBit;
import static com.fmotech.chess.BitOperations.rotateLeft;
import static com.fmotech.chess.Board.BISHOP;
import static com.fmotech.chess.Board.KING;
import static com.fmotech.chess.Board.KNIGHT;
import static com.fmotech.chess.Board.PAWN;
import static com.fmotech.chess.Board.QUEEN;
import static com.fmotech.chess.Board.ROOK;
import static com.fmotech.chess.Board.SPECIAL;
import static com.fmotech.chess.Move.MOVE_CAST_H;
import static com.fmotech.chess.Move.MOVE_CAST_L;
import static com.fmotech.chess.Move.MOVE_EP;
import static com.fmotech.chess.Move.MOVE_EP_CAP;
import static com.fmotech.chess.Move.MOVE_PROMO;
import static com.fmotech.chess.MoveTables.BISHOP_HIGH_TABLE;
import static com.fmotech.chess.MoveTables.BISHOP_LOW_TABLE;
import static com.fmotech.chess.MoveTables.DIR1_TABLE;
import static com.fmotech.chess.MoveTables.DIR2_TABLE;
import static com.fmotech.chess.MoveTables.DIR3_TABLE;
import static com.fmotech.chess.MoveTables.KING_TABLE;
import static com.fmotech.chess.MoveTables.KNIGHT_TABLE;
import static com.fmotech.chess.MoveTables.PAWN_ATTACK_HIGH_TABLE;
import static com.fmotech.chess.MoveTables.ROOK_HIGH_TABLE;
import static com.fmotech.chess.MoveTables.ROOK_LOW_TABLE;

public class MoveGenerator {

    public static final int KING_MASK = 0x8 | KING;
    private static final long PAWN_RANK = 0x000000000000ff00L;
    private static final long RANK_7 = 0x00FF0000_00000000L;
    private static final long NONE = -1L;

    public static final int N = 7, S = 3, W = 5, E = 1, NW = 6, SW = 0, NE = 4, SE = 2;
    private static int[] SHIFT = { -7, -1, -9, -8, 7, 1, 9, 8 };
    private static long[] AVOID_WRAP = {
            0x00fefefefefefefeL,
            0x7f7f7f7f7f7f7f7fL,
            0x007f7f7f7f7f7f7fL,
            0x00ffffffffffffffL,
            0x7f7f7f7f7f7f7f00L,
            0xfefefefefefefefeL,
            0xfefefefefefefe00L,
            0xffffffffffffff00L };

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
            if (isValid(nextBoard)) {
                moves[index++] = moves[i];
            }
        }
        return index;
    }

    public static boolean isValid(Board board) {
        int kingPosition = lowestBitPosition(board.ownKing());
        return !isPositionInAttack(board, kingPosition);
    }

    public static boolean isInCheck(Board board) {
        return !isValid(board);
    }

    public static int generateDirtyMoves(Board board, int[] moves) {
        int counter = 0;
        counter = generatePawnMoves(board, counter, moves);
        counter = generateRooksMoves(board, NONE, counter, moves);
        counter = generateKnightMoves(board, counter, moves);
        counter = generateBishopsMoves(board, NONE, counter, moves);
        counter = generateQueensMoves(board, NONE, counter, moves);
        counter = generateKingMoves(board, counter, moves);
        return counter;
    }

    public static int generateDirtyCaptureMoves(Board board, int[] moves) {
        int counter = 0;
        counter = generatePawnAttackAndQueenPromotionsMoves(board, counter, moves);
        counter = generateRooksMoves(board, board.enemyPieces(), counter, moves);
        counter = generateKnightAttackMoves(board, counter, moves);
        counter = generateBishopsMoves(board, board.enemyPieces(), counter, moves);
        counter = generateQueensMoves(board, board.enemyPieces(), counter, moves);
        counter = generateKingAttackMoves(board, counter, moves);
        return counter;
    }

    private static int generateKingAttackMoves(Board board, int counter, int[] moves) {
        return generateTargetMoves(board, board.ownKing(), board.enemyPieces(), KING_TABLE, KING_MASK, counter, moves);
    }

    private static int generateKnightAttackMoves(Board board, int counter, int[] moves) {
        return generateTargetMoves(board, board.ownKnights(), board.enemyPieces(), KNIGHT_TABLE, KNIGHT, counter, moves);
    }

    private static int generatePawnAttackAndQueenPromotionsMoves(Board board, int counter, int[] moves) {
        long pawns = board.ownPawns();
        while (pawns != 0) {
            long pawn = lowestBit(pawns);
            int srcPos = lowestBitPosition(pawns);
            int promote = (pawn & RANK_7) != 0 ? MOVE_PROMO : 0;
            long next = pawn << 8;
            if ((pawn & RANK_7) != 0 && (board.pieces() & next) == 0) {
                int tgtPos = lowestBitPosition(next);
                moves[counter++] = Move.create(srcPos, tgtPos, PAWN, 0, MOVE_PROMO | QUEEN);
            }
            next = PAWN_ATTACK_HIGH_TABLE[srcPos] & board.enemyPieces();
            while (next != 0) {
                int tgtPos = lowestBitPosition(next);
                int capture = board.type(tgtPos, ROOK, SPECIAL);
                counter = createPawnMove(moves, counter, srcPos, tgtPos, capture, promote);
                next = nextLowestBit(next);
            }
            pawns = nextLowestBit(pawns);
        }
        return counter;
    }

    private static int generatePawnMoves(Board board, int counter, int[] moves) {
        long pawns = board.ownPawns();
        while (pawns != 0) {
            long pawn = lowestBit(pawns);
            int srcPos = lowestBitPosition(pawns);
            int promote = (pawn & RANK_7) != 0 ? MOVE_PROMO : 0;
            long next = pawn << 8;//PAWN[pos];
            if ((board.pieces() & next) == 0) {
                int tgtPos = lowestBitPosition(next);
                counter = createPawnMove(moves, counter, srcPos, tgtPos, 0, promote);
                if ((pawn & PAWN_RANK) != 0) {
                    next <<= 8;
                    tgtPos = lowestBitPosition(next);
                    if ((board.pieces() & next) == 0) {
                        counter = createPawnMove(moves, counter, srcPos, tgtPos, 0, MOVE_EP);
                    }
                }
            }
            next = PAWN_ATTACK_HIGH_TABLE[srcPos] & (board.enemyPieces() | board.enPassant());
            while (next != 0) {
                int tgtPos = lowestBitPosition(next);
                int capture = board.type(tgtPos, ROOK, SPECIAL);
                counter = createPawnMove(moves, counter, srcPos, tgtPos, capture, promote);
                next = nextLowestBit(next);
            }
            pawns = nextLowestBit(pawns);
        }
        return counter;
    }

    private static int generateRooksMoves(Board board, long filter, int counter, int[] moves) {
        long pieces = board.pieces();
        long ownPieces = board.ownPieces();
        long rooks = board.ownRooks();
        while (rooks != 0) {
            int pos = lowestBitPosition(rooks);
            long mask = generateRookMaks(pos, pieces, ownPieces);
            counter = createMove(moves, counter, board, pos, ROOK, mask & filter);
            rooks = nextLowestBit(rooks);
        }
        return counter;
    }

    public static long generateRookMaks(int pos, long pieces, long ownPieces) {
        return generateLinearMoveFor(pos, ROOK_HIGH_TABLE, ROOK_LOW_TABLE, pieces, ownPieces);
    }

    private static int generateKnightMoves(Board board, int counter, int[] moves) {
        return generateTargetMoves(board, board.ownKnights(), ~board.ownPieces(), KNIGHT_TABLE, KNIGHT, counter, moves);
    }

    public static long generateKnightMask(int pos, long pieces, long ownPieces) {
        return KNIGHT_TABLE[pos] & ~ownPieces;
    }

    private static int generateBishopsMoves(Board board, long filter, int counter, int[] moves) {
        long pieces = board.pieces();
        long ownPieces = board.ownPieces();
        long bishops = board.ownBishops();
        while (bishops != 0) {
            int pos = lowestBitPosition(bishops);
            long mask = generateBishopMask(pos, pieces, ownPieces);
            counter = createMove(moves, counter, board, pos, BISHOP, mask & filter);
            bishops = nextLowestBit(bishops);
        }
        return counter;
    }

    public static long generateBishopMask(int pos, long pieces, long ownPieces) {
        return generateLinearMoveFor(pos, BISHOP_HIGH_TABLE, BISHOP_LOW_TABLE, pieces, ownPieces);
    }

    private static int generateQueensMoves(Board board, long filter, int counter, int[] moves) {
        long pieces = board.pieces();
        long ownPieces = board.ownPieces();
        long queens = board.ownQueens();
        while (queens != 0) {
            int pos = lowestBitPosition(queens);
            long mask = generateQueenMask(pos, pieces, ownPieces);
            counter = createMove(moves, counter, board, pos, QUEEN, mask & filter);
            queens = nextLowestBit(queens);
        }
        return counter;
    }

    public static long generateQueenMask(int pos, long pieces, long ownPieces) {
        return generateRookMaks(pos, pieces, ownPieces) | generateBishopMask(pos, pieces, ownPieces);
    }

    private static int generateKingMoves(Board board, int counter, int[] moves) {
        long king = board.ownKing();
        int kingPos = lowestBitPosition(king);
        counter = generateTargetMoves(board, king, ~board.ownPieces(), KING_TABLE, KING_MASK, counter, moves);
        if (board.castleLow() && !isPositionInAttack(board, kingPos) && !isPositionInAttack(board, kingPos - 1)
                && !isPositionInAttack(board, kingPos - 2)) {
            moves[counter++] = Move.create(kingPos, kingPos - 2, KING_MASK, 0, MOVE_CAST_L);
        }
        if (board.castleHigh() && !isPositionInAttack(board, kingPos) && !isPositionInAttack(board, kingPos + 1)
                && !isPositionInAttack(board, kingPos + 2)) {
            moves[counter++] = Move.create(kingPos, kingPos + 2, KING_MASK, 0, MOVE_CAST_H);
        }
        return counter;
    }

    public static long generateKingMask(int pos, long pieces, long ownPieces) {
        return KING_TABLE[pos] & ~ownPieces;
    }

    private static long generateLinearMoveFor(int pos, long[] highTable, long[] lowTable, long pieces, long ownPieces) {
        long move = 0;
        long next = highTable[pos];
        move |= next;
        next &= pieces;
        while (next != 0) {
            long contact = lowestBit(next);
            int contactPos = lowestBitPosition(contact);
            long mask = ~highTable[contactPos];
            move &= mask;
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
            next &= mask;
            next = nextHighestBit(next);
        }
        return move & ~ownPieces;
    }

    private static int generateTargetMoves(Board board, long pieces, long filter, long[] table, int srcType, int counter, int[] moves) {
        while (pieces != 0) {
            int srcPos = lowestBitPosition(pieces);
            counter = createMove(moves, counter, board, srcPos, srcType, table[srcPos] & filter);
            pieces = nextLowestBit(pieces);
        }
        return counter;
    }

    private static int createPawnMove(int[] moves, int counter, int srcPos, int tgtPos, int tgtType, int flags) {
        if (flags == MOVE_PROMO) {
            moves[counter++] = Move.create(srcPos, tgtPos, PAWN, tgtType, MOVE_PROMO | QUEEN);
            moves[counter++] = Move.create(srcPos, tgtPos, PAWN, tgtType, MOVE_PROMO | ROOK);
            moves[counter++] = Move.create(srcPos, tgtPos, PAWN, tgtType, MOVE_PROMO | BISHOP);
            moves[counter++] = Move.create(srcPos, tgtPos, PAWN, tgtType, MOVE_PROMO | KNIGHT);
        } else if (tgtType == SPECIAL) {
            moves[counter++] = Move.create(srcPos, tgtPos, PAWN, PAWN, MOVE_EP_CAP);
        } else {
            moves[counter++] = Move.create(srcPos, tgtPos, PAWN, tgtType, flags);
        }
        return counter;
    }

    private static int createMove(int[] moves, int counter, Board board, int srcPos, int srcType, long mask) {
        long next = mask;
        while (next != 0) {
            int tgtPos = lowestBitPosition(next);
            int tgtType = board.type(tgtPos, ROOK, 0);
            moves[counter++] = Move.create(srcPos, tgtPos, srcType, tgtType, 0);
            next = nextLowestBit(next);
        }
        return counter;
    }

    public static boolean isPositionInAttack(Board board, int pos) {
        assert pos < 64;
        long pieces = board.pieces();
        if ((PAWN_ATTACK_HIGH_TABLE[pos] & board.enemyPawns()) != 0) return true;
        if ((KNIGHT_TABLE[pos] & board.enemyKnights()) != 0) return true;
        if ((KING_TABLE[pos] & board.enemyKing()) != 0) return true;
        long rooksQueens = board.enemyRooks() | board.enemyQueens();
        if (isPositionInAttackHigh(pos, rooksQueens, pieces, ROOK_HIGH_TABLE)) return true;
        if (isPositionInAttackLow(pos, rooksQueens, pieces, ROOK_LOW_TABLE)) return true;
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

    public static int slidingDirection(int pos, long prev) {
        int dir8 = 0;
        dir8 |= (DIR3_TABLE[pos] & prev) != 0 ? 0b100 : 0;
        dir8 |= (DIR2_TABLE[pos] & prev) != 0 ? 0b010 : 0;
        dir8 |= (DIR1_TABLE[pos] & prev) != 0 ? 0b001 : 0;
        return dir8;
    }

    public static long slidingAttacks(long slider, long empty, int dir8) {
        long fill = occludedFill(slider, empty, dir8);
        return shiftOne(fill, dir8);
    }

    private static long occludedFill(long gen, long pro, int dir8) {
        int r = SHIFT[dir8]; // {+-1,7,8,9}
        pro &= AVOID_WRAP[dir8];
        gen |= pro & rotateLeft(gen, r);
        pro &= rotateLeft(pro, r);
        gen |= pro & rotateLeft(gen, 2 * r);
        pro &= rotateLeft(pro, 2 * r);
        gen |= pro & rotateLeft(gen, 4 * r);
        return gen;
    }

    public static long shiftOne(long b, int dir8) {
        int r = SHIFT[dir8]; // {+-1,7,8,9}
        return rotateLeft(b, r) & AVOID_WRAP[dir8];
    }
}
