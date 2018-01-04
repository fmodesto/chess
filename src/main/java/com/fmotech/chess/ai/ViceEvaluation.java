package com.fmotech.chess.ai;

import com.fmotech.chess.Board;

import static com.fmotech.chess.BitOperations.bitCount;
import static com.fmotech.chess.BitOperations.fileFill;
import static com.fmotech.chess.BitOperations.lowestBit;
import static com.fmotech.chess.BitOperations.lowestBitPosition;
import static com.fmotech.chess.BitOperations.nextLowestBit;
import static com.fmotech.chess.BitOperations.sparseBitCount;
import static com.fmotech.chess.ai.EvaluationUtils.ENEMY_SIDE;
import static com.fmotech.chess.ai.EvaluationUtils.OWN_SIDE;
import static com.fmotech.chess.ai.EvaluationUtils.PAWN_ISOLATED_TABLE;
import static com.fmotech.chess.ai.EvaluationUtils.PAWN_PASSED_TABLE;
import static com.fmotech.chess.ai.EvaluationUtils.rank;
import static com.fmotech.chess.ai.EvaluationUtils.toBoardPosition;

public class ViceEvaluation implements Evaluation {

    private static final byte[][] PAWN_TABLE = toBoardPosition(
            0,   0,   0,   0,   0,   0,   0,   0,
            20,  20,  20,  30,  30,  20,  20,  20,
            10,  10,  10,  20,  20,  10,  10,  10,
            5,   5,   5,  10,  10,   5,   5,   5,
            0,   0,  10,  20,  20,  10,   0,   0,
            5,   0,   0,   5,   5,   0,   0,   5,
            10,  10,   0, -10, -10,   0,  10,  10,
            0,   0,   0,   0,   0,   0,   0,   0
    );

    private static final byte[][] KNIGHT_TABLE = toBoardPosition(
            0,   0,   0,   0,   0,   0,   0,   0,
            0,   0,   5,  10,  10,   5,   0,   0,
            5,  10,  10,  20,  20,  10,  10,   5,
            5,  10,  15,  20,  20,  15,  10,   5,
            0,   0,  10,  20,  20,  10,   5,   0,
            0,   0,  10,  10,  10,  10,   0,   0,
            0,   0,   0,   5,   5,   0,   0,   0,
            0, -10,   0,   0,   0,   0, -10,   0
    );

    private static final byte[][] BISHOP_TABLE = toBoardPosition(
            0,   0,   0,   0,   0,   0,   0,   0,
            0,   0,   0,  10,  10,   0,   0,   0,
            0,   0,  10,  15,  15,  10,   0,   0,
            0,  10,  15,  20,  20,  15,  10,   0,
            0,  10,  15,  20,  20,  15,  10,   0,
            0,   0,  10,  15,  15,  10,   0,   0,
            0,   0,   0,  10,  10,   0,   0,   0,
            0,   0, -10,   0,   0, -10,   0,   0
    );

    private static final byte[][] ROOK_TABLE = toBoardPosition(
            0,   0,   5,  10,  10,   5,   0,   0,
            25,  25,  25,  25,  25,  25,  25,  25,
            0,   0,   5,  10,  10,   5,   0,   0,
            0,   0,   5,  10,  10,   5,   0,   0,
            0,   0,   5,  10,  10,   5,   0,   0,
            0,   0,   5,  10,  10,   5,   0,   0,
            0,   0,   5,  10,  10,   5,   0,   0,
            0,   0,   5,  10,  10,   5,   0,   0
    );

    private static final byte[][] KING_OPENING = toBoardPosition(
            -70, -70, -70, -70, -70, -70, -70, -70,
            -70, -70, -70, -70, -70, -70, -70, -70,
            -70, -70, -70, -70, -70, -70, -70, -70,
            -70, -70, -70, -70, -70, -70, -70, -70,
            -70, -70, -70, -70, -70, -70, -70, -70,
            -50, -50, -50, -50, -50, -50, -50, -50,
            -30, -30, -30, -30, -30, -30, -30, -30,
            0,   5,   5, -10, -10,   0,  10,   5
    );

    private static final byte[][] KING_ENDING = toBoardPosition(
            -50, -10,   0,   0,   0,   0, -10, -50,
            -10,   0,  10,  10,  10,  10,   0, -10,
            0,  10,  20,  20,  20,  20,  10,   0,
            0,  10,  20,  40,  40,  20,  10,   0,
            0,  10,  20,  40,  40,  20,  10,   0,
            0,  10,  20,  20,  20,  20,  10,   0,
            -10,   0,  10,  10,  10,  10,   0, -10,
            -50, -10,   0,   0,   0,   0, -10, -50
    );

    private static final int[][] PAWN_PASSED = new int[][] { { 0, 5, 10, 20, 35, 60, 100, 200 }, { 200, 100, 60, 35, 20, 10, 5, 0 } };
    private static final int PAWN_ISOLATED = -10;
    private static final int QUEEN_OPEN = 5;
    private static final int QUEEN_SEMI = 3;
    private static final int ROOK_OPEN = 10;
    private static final int ROOK_SEMI = 5;
    private static final int BISHOP_PAIR = 30;

    private static final int QUEEN = 1000;
    private static final int ROOK = 550;
    private static final int BISHOP = 325;
    private static final int KNIGHT = 325;
    private static final int PAWN = 100;
    private static final int GAME_ENDING = ROOK + KNIGHT + BISHOP + PAWN + PAWN;

    @Override
    public int evaluateBoardPosition(Board board, int alpha, int beta) {
        long ownOpen = ~fileFill(board.ownPawns());
        long enemyOpen = ~fileFill(board.enemyPawns());
        long open = (ownOpen & enemyOpen);

        int score = 0;
        score += evalQueen(OWN_SIDE, board.ownQueens(), open, ownOpen);
        score += evalRook(OWN_SIDE, board.ownRooks(), open, ownOpen);
        score += evalBishop(OWN_SIDE, board.ownBishops());
        score += evalKnight(OWN_SIDE, board.ownKnights());
        score += evalPawns(OWN_SIDE, board.ownPawns(), board.enemyPawns());
        score += evalKing(OWN_SIDE, board, board.ownKing());

        score -= evalQueen(ENEMY_SIDE, board.enemyQueens(), open, enemyOpen);
        score -= evalRook(ENEMY_SIDE, board.enemyRooks(), open, enemyOpen);
        score -= evalBishop(ENEMY_SIDE, board.enemyBishops());
        score -= evalKnight(ENEMY_SIDE, board.enemyKnights());
        score -= evalPawns(ENEMY_SIDE, board.enemyPawns(), board.ownPawns());
        score -= evalKing(ENEMY_SIDE, board, board.enemyKing());

        return score;
    }

    private int evalKing(int side, Board board, long piece) {
        int pos = lowestBitPosition(piece);
        if (gameEnding(side, board))
            return KING_ENDING[side][pos];
        else
            return KING_OPENING[side][pos];
    }

    private boolean gameEnding(int side, Board board) {
        int pieceScore = 0;
        long mask = side == OWN_SIDE ? board.ownPieces() : board.enemyPieces();
        pieceScore += PAWN * bitCount(board.pawns() & mask);
        pieceScore += KNIGHT * sparseBitCount(board.knights() & mask);
        pieceScore += BISHOP * sparseBitCount(board.bishops() & mask);
        pieceScore += ROOK * sparseBitCount(board.rooks() & mask);
        pieceScore += QUEEN * sparseBitCount(board.queens() & mask);
        return pieceScore <= GAME_ENDING;
    }

    private int evalBishop(int side, long pieces) {
        int score = 0;
        int count = 0;
        while (pieces != 0) {
            int pos = lowestBitPosition(pieces);
            score += BISHOP;
            score += BISHOP_TABLE[side][pos];
            count += 1;
            pieces = nextLowestBit(pieces);
        }
        if (count > 1) {
            score += BISHOP_PAIR;
        }
        return score;
    }

    private int evalRook(int side, long pieces, long open, long semi) {
        int score = 0;
        long next = pieces;
        while (next != 0) {
            long piece = lowestBit(next);
            int pos = lowestBitPosition(next);
            score += ROOK;
            score += ROOK_TABLE[side][pos];
            if ((piece & open) != 0)
                score += ROOK_OPEN;
            else if ((piece & semi) != 0)
                score += ROOK_SEMI;
            next = nextLowestBit(next);
        }
        return score;
    }

    private int evalQueen(int side, long pieces, long open, long semi) {
        int score = 0;
        long next = pieces;
        while (next != 0) {
            long piece = lowestBit(next);
            score += QUEEN;
            if ((piece & open) != 0) {
                score += QUEEN_OPEN;
            } else if ((piece & semi) != 0) {
                score += QUEEN_SEMI;
            }
            next = nextLowestBit(next);
        }
        return score;
    }

    private int evalKnight(int side, long pieces) {
        int score = 0;
        while (pieces != 0) {
            int pos = lowestBitPosition(pieces);
            score += KNIGHT;
            score += KNIGHT_TABLE[side][pos];
            pieces = nextLowestBit(pieces);
        }
        return score;
    }

    private int evalPawns(int side, long own, long enemy) {
        int score = 0;
        long next = own;
        while (next != 0) {
            int pos = lowestBitPosition(next);
            score += PAWN;
            score += PAWN_TABLE[side][pos];
            if ((PAWN_ISOLATED_TABLE[pos] & own) == 0) {
                score += PAWN_ISOLATED;
            }
            if ((PAWN_PASSED_TABLE[side][pos] & enemy) == 0) {
                score += PAWN_PASSED[side][rank(pos)];
            }
            next = nextLowestBit(next);
        }
        return score;
    }
}
