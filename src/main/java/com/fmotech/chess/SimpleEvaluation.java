package com.fmotech.chess;

import java.util.Arrays;
import java.util.Random;

import static com.fmotech.chess.BitOperations.lowestBitPosition;
import static com.fmotech.chess.BitOperations.nextLowestBit;

public class SimpleEvaluation {

    private static byte[] PAWN = new byte[] {
            0, 0, 0, 0, 0, 0, 0, 0,
            1, 2, 2, -4, -4, 2, 2, 1,
            1, -1, -2, 0, 0, -2, -1, 1,
            0, 0, 0, 4, 4, 0, 0, 0,
            1, 1, 2, 5, 5, 2, 1, 1,
            2, 2, 4, 6, 6, 4, 2, 2,
            10, 10, 10, 10, 10, 10, 10, 10,
            0, 0, 0, 0, 0, 0, 0, 0 };

    private static byte[] ROCK = new byte[] {
            0, 0, 0, 1, 1, 0, 0, 0,
            -1, 0, 0, 0, 0, 0, 0, -1,
            -1, 0, 0, 0, 0, 0, 0, -1,
            -1, 0, 0, 0, 0, 0, 0, -1,
            -1, 0, 0, 0, 0, 0, 0, -1,
            -1, 0, 0, 0, 0, 0, 0, -1,
            1, 2, 2, 2, 2, 2, 2, 1,
            0, 0, 0, 0, 0, 0, 0, 0 };

    private static byte[] KNIGHT = new byte[] {
            -10, -8, -6, -6, -6, -6, -8, -10,
            -8, -4, 0, 1, 1, 0, -4, -8,
            -6, 1, 2, 3, 3, 2, 1, -6,
            -6, 0, 3, 4, 4, 3, 0, -6,
            -6, 1, 3, 4, 4, 3, 1, -6,
            -6, 0, 2, 3, 3, 2, 0, -6,
            -8, -4, 0, 0, 0, 0, -4, -8,
            -10, -8, -6, -6, -6, -6, -8, -10 };

    private static byte[] BISHOP = new byte[] {
            -4, -2, -2, -2, -2, -2, -2, -4,
            -2, 1, 0, 0, 0, 0, 1, -2,
            -2, 2, 2, 2, 2, 2, 2, -2,
            -2, 0, 2, 2, 2, 2, 0, -2,
            -2, 1, 1, 2, 2, 1, 1, -2,
            -2, 0, 1, 2, 2, 1, 0, -2,
            -2, 0, 0, 0, 0, 0, 0, -2,
            -4, -2, -2, -2, -2, -2, -2, -4 };

    private static byte[] QUEEN = new byte[] {
            -4, -2, -2, -1, -1, -2, -2, -4,
            -2, 0, 0, 0, 0, 1, 0, -2,
            -2, 0, 1, 1, 1, 1, 1, -2,
            -1, 0, 1, 1, 1, 1, 0, 0,
            -1, 0, 1, 1, 1, 1, 0, -1,
            -2, 0, 1, 1, 1, 1, 0, -2,
            -2, 0, 0, 0, 0, 0, 0, -2,
            -4, -2, -2, -1, -1, -2, -2, -4 };

    private static byte[] KING = new byte[] {
            4, 6, 2, 0, 0, 2, 6, 4,
            4, 4, 0, 0, 0, 0, 4, 4,
            -2, -4, -4, -4, -4, -4, -4, -2,
            -4, -6, -6, -8, -8, -6, -6, -4,
            -6, -8, -8, -10, -10, -8, -8, -6,
            -6, -8, -8, -10, -10, -8, -8, -6,
            -6, -8, -8, -10, -10, -8, -8, -6,
            -6, -8, -8, -10, -10, -8, -8, -6
    };

    public static int evaluateBoardCount(Board board) {
        int score = 0;
        score += eval(900, board.ownKing(), board.enemyKing());
        score += eval(90, board.ownQueens(), board.enemyQueens());
        score += eval(50, board.ownRocks(), board.enemyRocks());
        score += eval(30, board.ownBishops(), board.enemyBishops());
        score += eval(30, board.ownKnights(), board.enemyKnights());
        score += eval(10, board.ownPawns(), board.enemyPawns());
        return score;
    }

    public static int evaluateBoardPosition(Board board) {
        int score = 0;
        score += eval(1800, board.ownKing(), board.enemyKing(), KING);
        score += eval(180, board.ownQueens(), board.enemyQueens(), QUEEN);
        score += eval(100, board.ownRocks(), board.enemyRocks(), ROCK);
        score += eval(60, board.ownBishops(), board.enemyBishops(), BISHOP);
        score += eval(60, board.ownKnights(), board.enemyKnights(), KNIGHT);
        score += eval(20, board.ownPawns(), board.enemyPawns(), PAWN);
        return score;
    }

    private static int eval(int points, long own, long enemy, byte[] table) {
        int sum = 0;
        while (own != 0) {
            sum += points + table[lowestBitPosition(own)];
            own = nextLowestBit(own);
        }
        while (enemy != 0) {
            sum -= points + table[63 - lowestBitPosition(enemy)];
            enemy = nextLowestBit(enemy);
        }
        return sum;
    }

    private static int eval(int points, long own, long enemy) {
        return points * (BitOperations.bitCount(own) - BitOperations.bitCount(enemy));
    }
}
