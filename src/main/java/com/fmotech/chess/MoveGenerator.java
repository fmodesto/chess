package com.fmotech.chess;

import java.util.HashMap;
import java.util.Map;

import static com.fmotech.chess.BitOperations.lowestBit;
import static com.fmotech.chess.BitOperations.lowestBitPosition;
import static com.fmotech.chess.BitOperations.nextLowestBit;
import static com.fmotech.chess.Board.BISHOP;
import static com.fmotech.chess.Board.KING;
import static com.fmotech.chess.Board.KNIGHT;
import static com.fmotech.chess.Board.PAWN;
import static com.fmotech.chess.Board.QUEEN;
import static com.fmotech.chess.Board.ROOK;
import static com.fmotech.chess.FenFormatter.moveToFen;
import static com.fmotech.chess.MoveTables.BATT3;
import static com.fmotech.chess.MoveTables.BATT4;
import static com.fmotech.chess.MoveTables.BXRAY3;
import static com.fmotech.chess.MoveTables.BXRAY4;
import static com.fmotech.chess.MoveTables.DIR;
import static com.fmotech.chess.MoveTables.MASK;
import static com.fmotech.chess.MoveTables.RATT1;
import static com.fmotech.chess.MoveTables.RATT2;
import static com.fmotech.chess.MoveTables.RXRAY1;
import static com.fmotech.chess.MoveTables.RXRAY2;
import static com.fmotech.chess.Utils.BIT;
import static com.fmotech.chess.Utils.RANK;
import static com.fmotech.chess.Utils.TEST;

public class MoveGenerator {

    public static final int KING_MASK = 0x8 | KING;

    public static void main(String[] args) {
        System.out.println(countMoves(3, Board.INIT, false));
    }

    public static Map<String, Long> moves = new HashMap<>();
    public static long countMoves(int depth, Board board, boolean div) {
        long count = 0L;
        int[] moves = generateMoves(board);
        if (depth == 1) return moves[0] - 1;
        if (div) MoveGenerator.moves.clear();
        for (int i = 1; i < moves[0]; i++) {
            int move = moves[i];
            long n = countMoves(depth - 1, board.move(move).nextTurn(), false);
            count += n;
            if (div) {
                System.out.println(moveToFen(board, move) + " " + n + " " + move);
                MoveGenerator.moves.put(moveToFen(board, move), n);
            }
        }
        return count;
    }

    public static int[] generateMoves(Board board) {
        int king = lowestBitPosition(board.ownKing());
        long check = attackingPieces(king, board);
        long pin = pinnedPieces(king, board);
        return generate(king, check, pin, true, true, true, board);
    }

    public static boolean isInCheck(Board board) {
        int king = lowestBitPosition(board.ownKing());
        return positionAttacked(king, board);
    }

    public static int[] generate(int king, long check, long pin, boolean capture, boolean nonCapture, boolean allPromotions, Board board) {
        int[] moves = board.moves();
        moves[0] = 1;

        if (check != 0) {
            generateCheckEscape(moves, king, check, pin, allPromotions, board);
            return moves;
        }
        if (capture)
            generateCaptureMoves(moves, king, check, pin, allPromotions, board);
        if (nonCapture)
            generateNonCaptureMoves(moves, king, check, pin, allPromotions, board);
        return moves;
    }

    private static void generateCheckEscape(int[] moves, int king, long check, long pin, boolean allPromotions, Board board) {
        generateKingMoves(moves, king, ~board.ownPieces(), board);

        if (nextLowestBit(check) != 0) return; // Can't capture several pieces

        int target = lowestBitPosition(check);
        int type = board.type(check);

        long capturers = defendingPieces(target, board) & ~pin;
        while (capturers != 0) {
            int from = lowestBitPosition(capturers);
            int piece = board.type(lowestBit(capturers));
            if (piece == PAWN) {
                registerPawnMove(moves, from, target, type, allPromotions);
            } else {
                moves[moves[0]++] = Move.create(from, target, piece, type, 0);
            }
            capturers = nextLowestBit(capturers);
        }

        if (board.enemyEnPassantPawn() == check) {
            int enPassantPosition = lowestBitPosition(board.enPassant());
            capturers = MoveTables.PAWN_ATTACK[1][enPassantPosition] & board.ownPawns() & ~pin;
            while (capturers != 0) {
                int from = lowestBitPosition(capturers);
                moves[moves[0]++] = Move.create(from, enPassantPosition, PAWN, PAWN, 0);
                capturers = nextLowestBit(capturers);
            }
        }

        if ((check & (MoveTables.KNIGHT[king] | MoveTables.KING[king])) != 0) return; // Can't block

        long segment = getSegment(king, target, board.pieces());
        while (segment != 0) {
            int to = lowestBitPosition(segment);
            long blockers = reach(to, board) & ~pin;
            while (blockers != 0) {
                int from = lowestBitPosition(blockers);
                int piece = board.type(lowestBit(blockers));
                moves[moves[0]++] = Move.create(from, to, piece, 0, 0);
                blockers = nextLowestBit(blockers);
            }
            int from = to - 8;
            if (from >= 0 && from < 64) {
                if ((BIT(from) & board.ownPawns() & ~pin) != 0)
                    registerPawnMove(moves, from, to, 0, allPromotions);

                int jump = from - 8;
                if (RANK(to) == 3 && (board.pieces() & BIT(from)) == 0
                        && (BIT(jump) & board.ownPawns() & ~pin) != 0)
                    moves[moves[0]++] = Move.create(jump, to, PAWN, 0, 0);
            }
            segment = nextLowestBit(segment);
        }
    }

    private static long getSegment(int from, int to, long pieces) {
        int dir = DIR(from, to);
        if (dir == 1) return RATT1(from, pieces) & RATT1(to, pieces) & ~pieces;
        else if (dir == 2) return RATT2(from, pieces) & RATT2(to, pieces) & ~pieces;
        else if (dir == 3) return BATT3(from, pieces) & BATT3(to, pieces) & ~pieces;
        else return BATT4(from, pieces) & BATT4(to, pieces) & ~pieces;
    }

    private static void generateKingMoves(int[] moves, int king, long mask, Board board) {
        long enemy = board.enemyPieces();

        long next = MoveTables.KING[king] & mask;
        while (next != 0) {
            int to = lowestBitPosition(next);
            long bit = lowestBit(next);
            if (!positionAttacked(to, board))
                moves[moves[0]++] = Move.create(king, to, KING_MASK, (bit & enemy) != 0 ? board.type(bit) : 0, 0);
            next = nextLowestBit(next);
        }
    }

    public static void generateNonCaptureMoves(int[] moves, int king, long check, long pin, boolean allPromotions, Board board) {
        long next = board.ownPawns();
        while (next != 0) {
            int from = lowestBitPosition(next);
            long mask = (pin & lowestBit(next)) != 0 ? ~MASK(from, king) : 0;
            int to = from + 8;
            if (!TEST(to, board.pieces() | mask)) {
                registerPawnMove(moves, from, to, 0, allPromotions);
                if (RANK(from) == 1) {
                    to += 8;
                    if (!TEST(to, board.pieces() | mask))
                        moves[moves[0]++] = Move.create(from, to, PAWN, 0, 0);
                }
            }
            next = nextLowestBit(next);
        }

        next = board.ownKnights();
        while (next != 0) {
            int from = lowestBitPosition(next);
            long mask = (pin & lowestBit(next)) != 0 ? MASK(from, king) : -1;
            long target = MoveTables.KNIGHT[from] & ~board.pieces() & mask;
            registerMoves(moves, from, KNIGHT, target);
            next = nextLowestBit(next);
        }

        next = board.ownBishops();
        while (next != 0) {
            int from = lowestBitPosition(next);
            long mask = (pin & lowestBit(next)) != 0 ? MASK(from, king) : -1;
            long target = bishopMove(from, board.pieces()) & ~board.pieces() & mask;
            registerMoves(moves, from, BISHOP, target);
            next = nextLowestBit(next);
        }

        next = board.ownRooks();
        while (next != 0) {
            int from = lowestBitPosition(next);
            long mask = (pin & lowestBit(next)) != 0 ? MASK(from, king) : -1;
            long target = rookMove(from, board.pieces()) & ~board.pieces() & mask;
            registerMoves(moves, from, ROOK, target);
            if (board.castle() != 0 && check == 0) {
                if (board.castleLow() && (from == 0) && (RATT1(0, board.pieces()) & BIT(3)) != 0
                        && !(positionAttacked(2, board) | positionAttacked(1, board))) {
                    moves[moves[0]++] = Move.create(3, 1, KING_MASK, 0, 0);
                }
                if (board.castleHigh() && (from == 7) && (RATT1(7, board.pieces()) & BIT(3)) != 0
                        && !(positionAttacked(4, board) | positionAttacked(5, board))) {
                    moves[moves[0]++] = Move.create(3, 5, KING_MASK, 0, 0);
                }
            }
            next = nextLowestBit(next);
        }

        next = board.ownQueens();
        while (next != 0) {
            int from = lowestBitPosition(next);
            long mask = (pin & lowestBit(next)) != 0 ? MASK(from, king) : -1;
            long target = queenMove(from, board.pieces()) & ~board.pieces() & mask;
            registerMoves(moves, from, QUEEN, target);
            next = nextLowestBit(next);
        }

        generateKingMoves(moves, king, ~board.pieces(), board);
    }

    public static void generateCaptureMoves(int[] moves, int king, long check, long pin, boolean allPromotions, Board board) {
        long next = board.ownPawns();
        while (next != 0) {
            int from = lowestBitPosition(next);
            long mask = (pin & lowestBit(next)) != 0 ? MASK(from, king) : -1;
            long target = MoveTables.PAWN_ATTACK[0][from] & (board.enPassant() | board.enemyPieces()) & mask;
            if ((target & board.enPassant()) != 0) {
                long ray = RATT1(from, board.pieces() ^ board.enemyEnPassantPawn());
                if ((ray & BIT(king)) != 0 && (ray & board.enemyRooksQueens()) != 0) {
                    target ^= board.enPassant();
                }
            }
            boolean promotion = RANK(from) == 6;
            if (target != 0 && promotion && allPromotions) {
                registerAttackMoves(moves, from, PAWN, target, QUEEN, board);
                registerAttackMoves(moves, from, PAWN, target, ROOK, board);
                registerAttackMoves(moves, from, PAWN, target, BISHOP, board);
                registerAttackMoves(moves, from, PAWN, target, KNIGHT, board);
            } else {
                registerAttackMoves(moves, from, PAWN, target, promotion ? QUEEN : 0, board);
            }
            next = nextLowestBit(next);
        }

        next = board.ownKnights();
        while (next != 0) {
            int from = lowestBitPosition(next);
            long mask = (pin & lowestBit(next)) != 0 ? MASK(from, king) : -1;
            long target = MoveTables.KNIGHT[from] & board.enemyPieces() & mask;
            registerAttackMoves(moves, from, KNIGHT, target, 0, board);
            next = nextLowestBit(next);
        }

        next = board.ownBishops();
        while (next != 0) {
            int from = lowestBitPosition(next);
            long mask = (pin & lowestBit(next)) != 0 ? MASK(from, king) : -1;
            long target = bishopMove(from, board.pieces()) & board.enemyPieces() & mask;
            registerAttackMoves(moves, from, BISHOP, target, 0, board);
            next = nextLowestBit(next);
        }

        next = board.ownRooks();
        while (next != 0) {
            int from = lowestBitPosition(next);
            long mask = (pin & lowestBit(next)) != 0 ? MASK(from, king) : -1;
            long target = rookMove(from, board.pieces()) & board.enemyPieces() & mask;
            registerAttackMoves(moves, from, ROOK, target, 0, board);
            next = nextLowestBit(next);
        }

        next = board.ownQueens();
        while (next != 0) {
            int from = lowestBitPosition(next);
            long mask = (pin & lowestBit(next)) != 0 ? MASK(from, king) : -1;
            long target = queenMove(from, board.pieces()) & board.enemyPieces() & mask;
            registerAttackMoves(moves, from, QUEEN, target, 0, board);
            next = nextLowestBit(next);
        }

        generateKingMoves(moves, king, board.enemyPieces(), board);
    }

    private static void registerMoves(int[] moves, int from, int piece, long target) {
        while (target != 0) {
            int to = lowestBitPosition(target);
            moves[moves[0]++] = Move.create(from, to, piece, 0, 0);
            target = nextLowestBit(target);
        }
    }

    private static void registerPawnMove(int[] moves, int from, int to, int capture, boolean allPromotions) {
        boolean promotion = RANK(from) == 6;
        if (promotion && allPromotions) {
            moves[moves[0]++] = Move.create(from, to, PAWN, capture, QUEEN);
            moves[moves[0]++] = Move.create(from, to, PAWN, capture, ROOK);
            moves[moves[0]++] = Move.create(from, to, PAWN, capture, BISHOP);
            moves[moves[0]++] = Move.create(from, to, PAWN, capture, KNIGHT);
        } else {
            moves[moves[0]++] = Move.create(from, to, PAWN, capture, promotion ? QUEEN : 0);
        }
    }

    private static void registerAttackMoves(int[] moves, int from, int piece, long target, int promotion, Board board) {
        while (target != 0) {
            int to = lowestBitPosition(target);
            long capture = lowestBit(target);
            moves[moves[0]++] = Move.create(from, to, piece, board.type(capture), promotion);
            target = nextLowestBit(target);
        }
    }

    @SuppressWarnings("RedundantIfStatement")
    public static boolean positionAttacked(int position, Board board) {
        long pieces = board.pieces() ^ board.ownKing();
        if ((MoveTables.PAWN_ATTACK[0][position] & board.enemyPawns()) != 0) return true;
        if ((MoveTables.KNIGHT[position] & board.enemyKnights()) != 0) return true;
        if ((MoveTables.KING[position] & board.enemyKing()) != 0) return true;
        if ((RATT1(position, pieces) & board.enemyRooksQueens()) != 0) return true;
        if ((RATT2(position, pieces) & board.enemyRooksQueens()) != 0) return true;
        if ((BATT3(position, pieces) & board.enemyBishopsQueens()) != 0) return true;
        if ((BATT4(position, pieces) & board.enemyBishopsQueens()) != 0) return true;
        return false;
    }

    private static long defendingPieces(int position, Board board) {
        return MoveTables.PAWN_ATTACK[1][position] & board.ownPawns()
                | MoveTables.KNIGHT[position] & board.ownKnights()
                | rookMove(position, board.pieces()) & board.ownRooksQueens()
                | bishopMove(position, board.pieces()) & board.ownBishopsQueens();
    }

    public static long attackingPieces(int position, Board board) {
        return MoveTables.PAWN_ATTACK[0][position] & board.enemyPawns()
                | MoveTables.KNIGHT[position] & board.enemyKnights()
                | rookMove(position, board.pieces()) & board.enemyRooksQueens()
                | bishopMove(position, board.pieces()) & board.enemyBishopsQueens();
    }

    private static long reach(int position, Board board) {
        return MoveTables.KNIGHT[position] & board.ownKnights()
                | rookMove(position, board.pieces()) & board.ownRooksQueens()
                | bishopMove(position, board.pieces()) & board.ownBishopsQueens();
    }

    public static long pinnedPieces(int position, Board board) {
        return pinnedPieces(position, board, board.ownPieces(), board.enemyPieces());
    }

    public static long pinnedPieces(int position, Board board, long own, long enemy) {
        long pin = 0L;
        long next = RXRAY1(position, board.pieces()) & board.rooksQueens() & enemy;
        while (next != 0) {
            int pinner = lowestBitPosition(next);
            pin |= RATT1(pinner, board.pieces()) & RATT1(position, board.pieces()) & own;
            next = nextLowestBit(next);
        }
        next = RXRAY2(position, board.pieces()) & board.rooksQueens() & enemy;
        while (next != 0) {
            int pinner = lowestBitPosition(next);
            pin |= RATT2(pinner, board.pieces()) & RATT2(position, board.pieces()) & own;
            next = nextLowestBit(next);
        }
        next = BXRAY3(position, board.pieces()) & board.bishopsQueens() & enemy;
        while (next != 0) {
            int pinner = lowestBitPosition(next);
            pin |= BATT3(pinner, board.pieces()) & BATT3(position, board.pieces()) & own;
            next = nextLowestBit(next);
        }
        next = BXRAY4(position, board.pieces()) & board.bishopsQueens() & enemy;
        while (next != 0) {
            int pinner = lowestBitPosition(next);
            pin |= BATT4(pinner, board.pieces()) & BATT4(position, board.pieces()) & own;
            next = nextLowestBit(next);
        }
        return pin;
    }

    public static long rookMove(int position, long pieces) {
        return (RATT1(position, pieces) | RATT2(position, pieces));
    }

    public static long bishopMove(int position, long pieces) {
        return (BATT3(position, pieces) | BATT4(position, pieces));
    }

    public static long queenMove(int position, long pieces) {
        return (RATT1(position, pieces) | RATT2(position, pieces) | BATT3(position, pieces) | BATT4(position, pieces));
    }
}
