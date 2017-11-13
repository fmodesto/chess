package com.fmotech.chess;

import java.time.LocalDateTime;

import static com.fmotech.chess.BitOperations.*;
import static com.fmotech.chess.DebugUtils.CHESS;
import static com.fmotech.chess.DebugUtils.toPosition;
import static com.fmotech.chess.MoveTables.*;
import static java.time.LocalDateTime.now;
import static java.time.temporal.ChronoUnit.MILLIS;

public class MoveGenerator {

    private static final long PAWN_RANK = 0x000000000000ff00L;

    public static void main(String[] args) {
//        LocalDateTime start = now();
//        Board board = FenFormatter.fromFen("rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 1 8")
////                .move(w("a2"), w("a3")).nextTurn()
////                .move(b("a7"), b("a6")).nextTurn()
////                .move(w("a1"), w("a2")).nextTurn()
//                ;
//        System.out.println(FenFormatter.toFen(board));
//        DebugUtils.debug(FEN, board);
//        long moves = generateMoves(2, true, board.whiteTurn(), board);
//        System.out.printf("%10d in %6d ms\n", moves, MILLIS.between(start, now()));


        execute(FenFormatter.fromFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"));
        execute(FenFormatter.fromFen("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq -"));
        execute(FenFormatter.fromFen("8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - -"));
        execute(FenFormatter.fromFen("r3k2r/Pppp1ppp/1b3nbN/nP6/BBP1P3/q4N2/Pp1P2PP/R2Q1RK1 w kq - 0 1"));
        execute(FenFormatter.fromFen("rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 1 8"));
        execute(FenFormatter.fromFen("r4rk1/1pp1qppp/p1np1n2/2b1p1B1/2B1P1b1/P1NP1N2/1PP1QPPP/R4RK1 w - - 0 10"));
    }

    private static void execute(Board board) {
        DebugUtils.debug(DebugUtils.CHESS, board);
        System.out.println(FenFormatter.toFen(board));
        for (int i = 1; i <= 5; i++) {
            LocalDateTime start = now();
            long moves = generateMoves(i, false, true, board);
            System.out.printf("%10d in %6d ms\n", moves, MILLIS.between(start, now()));
        }
    }

    public static long generateMoves(int level, boolean first, boolean color, Board board) {
        if (board.whiteTurn() != color) {
            throw new IllegalStateException("ERROR");
        }
        if (level == 0) return 1;
        long scenarios = 0;
        long[] moves = board.moves();
        int counter = generateDirtyMoves(board, moves);
        for (int i = 0; i < counter; i += 2) {
            long src = moves[i];
            long next = moves[i + 1];
            boolean promotion = ((board.ownPawns() & src) & 0x00FF0000_00000000L) != 0;
            while (next != 0) {
                long tgt = lowestBit(next);
                if (promotion) {
                    int kingPosition = lowestBitPosition(board.ownKing());
                    Board nextBoard = board.move(src, tgt, Board.QUEEN);
                    if (!isPositionInAttack(nextBoard, kingPosition)) {
                        scenarios += generateMoves(level - 1, false, !color, nextBoard.nextTurn());
                    }
                    nextBoard = board.move(src, tgt, Board.ROCK);
                    if (!isPositionInAttack(nextBoard, kingPosition)) {
                        scenarios += generateMoves(level - 1, false, !color, nextBoard.nextTurn());
                    }
                    nextBoard = board.move(src, tgt, Board.BISHOP);
                    if (!isPositionInAttack(nextBoard, kingPosition)) {
                        scenarios += generateMoves(level - 1, false, !color, nextBoard.nextTurn());
                    }
                    nextBoard = board.move(src, tgt, Board.KNIGHT);
                    if (!isPositionInAttack(nextBoard, kingPosition)) {
                        scenarios += generateMoves(level - 1, false, !color, nextBoard.nextTurn());
                    }
                } else {
                    Board nextBoard = board.move(src, tgt);
                    int kingPosition = lowestBitPosition(nextBoard.ownKing());
                    if (!isPositionInAttack(nextBoard, kingPosition)) {
                        long s = generateMoves(level - 1, false, !color, nextBoard.nextTurn());
                        if (first) {
                            System.out.println(toPosition(board.whiteTurn(), src) + toPosition(board.whiteTurn(), tgt) + ": " + s);
                        }
                        scenarios += s;
                    }
                }
                next = nextLowestBit(next);
            }
        }
        return scenarios;
    }

    public static int generateDirtyMoves(Board board, long[] moves) {
        int counter = 0;
        counter = generatePawnMoves(board, counter, moves);
        counter = generateRocksMoves(board, counter, moves);
        counter = generateKnightMoves(board, counter, moves);
        counter = generateBishopsMoves(board, counter, moves);
        counter = generateQueensMoves(board, counter, moves);
        counter = generateKingMoves(board, counter, moves);
        return counter;
    }

    private static int generatePawnMoves(Board board, int counter, long[] moves) {
        long pieces = board.pieces() | board.enPassant();
        long pawns = board.ownPawns();
        while (pawns != 0) {
            long pawn = lowestBit(pawns);
            int pos = lowestBitPosition(pawns);
            long move = 0;
            long next = PAWN[pos];
            if ((pieces & next) == 0) {
                move |= next;
                if ((pawn & PAWN_RANK) != 0) {
                    next <<= 8;
                    if ((pieces & next) == 0) {
                        move |= next;
                    }
                }
            }
            next = PAWN_ATTACK[pos];
            while (next != 0) {
                long pawnAttack = lowestBit(next);
                if ((pawnAttack & pieces) != 0 && board.enemy(pawnAttack)) {
                    move |= pawnAttack;
                }
                next = nextLowestBit(next);
            }
            moves[counter++] = pawn;
            moves[counter++] = move;
            pawns = nextLowestBit(pawns);
        }
        return counter;
    }

    private static int generateRocksMoves(Board board, int counter, long[] moves) {
        long rocks = board.ownRocks();
        while (rocks != 0) {
            long rock = lowestBit(rocks);
            int pos = lowestBitPosition(rocks);
            moves[counter++] = rock;
            moves[counter++] = generateLinearMoveFor(board, pos, ROCK_HIGH, ROCK_LOW);
            rocks = nextLowestBit(rocks);
        }
        return counter;
    }

    private static int generateKnightMoves(Board board, int counter, long[] moves) {
        return generateTargetMoves(board, board.ownKnights(), KNIGHT, counter, moves);
    }

    private static int generateBishopsMoves(Board board, int counter, long[] moves) {
        long bishops = board.ownBishops();
        while (bishops != 0) {
            long bishop = lowestBit(bishops);
            int pos = lowestBitPosition(bishops);
            moves[counter++] = bishop;
            moves[counter++] = generateLinearMoveFor(board, pos, BISHOP_HIGH, BISHOP_LOW);
            bishops = nextLowestBit(bishops);
        }
        return counter;
    }

    private static int generateQueensMoves(Board board, int counter, long[] moves) {
        long queens = board.ownQueens();
        while (queens != 0) {
            long queen = lowestBit(queens);
            int pos = lowestBitPosition(queens);
            moves[counter++] = queen;
            moves[counter++] = generateLinearMoveFor(board, pos, ROCK_HIGH, ROCK_LOW)
                    | generateLinearMoveFor(board, pos, BISHOP_HIGH, BISHOP_LOW);
            queens = nextLowestBit(queens);
        }
        return counter;
    }

    private static int generateKingMoves(Board board, int counter, long[] moves) {
        long king = board.ownKing();
        int kingPos = lowestBitPosition(king);
        counter = generateTargetMoves(board, king, KING, counter, moves);
        if (board.castleLow() && !isPositionInAttack(board, kingPos) && !isPositionInAttack(board, kingPos - 1)
                && !isPositionInAttack(board, kingPos - 2)) {
            moves[counter - 1] |= king >>> 2;
        }
        if (board.castleHigh() && !isPositionInAttack(board, kingPos) && !isPositionInAttack(board, kingPos + 1)
                && !isPositionInAttack(board, kingPos + 2)) {
            moves[counter - 1] |= king << 2;
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

    private static int generateTargetMoves(Board board, long pieces, long[] table, int counter, long[] moves) {
        long mask = ~board.ownPieces();
        while (pieces != 0) {
            long piece = lowestBit(pieces);
            int pos = lowestBitPosition(pieces);
            moves[counter++] = piece;
            moves[counter++] = table[pos] & mask;
            pieces = nextLowestBit(pieces);
        }
        return counter;
    }

    public static boolean isPositionInAttack(Board board, int pos) {
        long pieces = board.pieces();
        if ((PAWN_ATTACK[pos] & board.enemyPawns()) != 0) return true;
        if ((KNIGHT[pos] & board.enemyKnights()) != 0) return true;
        long rocksQueens = board.enemyRocks() | board.enemyQueens();
        if (isPositionInAttackHigh(pos, rocksQueens, pieces, ROCK_HIGH)) return true;
        if (isPositionInAttackLow(pos, rocksQueens, pieces, ROCK_LOW)) return true;
        long bishopsQueens = board.enemyBishops() | board.enemyQueens();
        if (isPositionInAttackHigh(pos, bishopsQueens, pieces, BISHOP_HIGH)) return true;
        if (isPositionInAttackLow(pos, bishopsQueens, pieces, BISHOP_LOW)) return true;
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
