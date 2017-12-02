package com.fmotech.chess;

import static com.fmotech.chess.BitOperations.lowestBitPosition;
import static com.fmotech.chess.BitOperations.nextLowestBit;
import static com.fmotech.chess.FenFormatter.moveFromFen;

public class SimpleEvaluation {

    public static void main(String[] args) {
        System.out.println(evaluateBoardPosition(Board.INIT));
        System.out.println(evaluateBoardPosition(Board.INIT.move(moveFromFen(Board.INIT, "e2e4"))));
    }

    private static byte[] PAWN = new byte[] {
            0,   0,   0,   0,   0,   0,   0,   0,
            0,   0,   0, -40, -40,   0,   0,   0,
            1,   2,   3, -10, -10,   3,   2,   1,
            2,   4,   6,   8,   8,   6,   4,   2,
            3,   6,   9,  12,  12,   9,   6,   3,
            4,   8,  12,  16,  16,  12,   8,   4,
            5,  10,  15,  20,  20,  15,  10,   5,
            0,   0,   0,   0,   0,   0,   0,   0
    };

    private static byte[] KNIGHT = new byte[] {
            -10, -30, -10, -10, -10, -10, -30, -10,
            -10,   0,   0,   0,   0,   0,   0, -10,
            -10,   0,   5,   5,   5,   5,   0, -10,
            -10,   0,   5,  10,  10,   5,   0, -10,
            -10,   0,   5,  10,  10,   5,   0, -10,
            -10,   0,   5,   5,   5,   5,   0, -10,
            -10,   0,   0,   0,   0,   0,   0, -10,
            -10, -10, -10, -10, -10, -10, -10, -10
    };

    private static byte[] BISHOP = new byte[] {
            -10, -10, -20, -10, -10, -20, -10, -10,
            -10,   0,   0,   0,   0,   0,   0, -10,
            -10,   0,   5,   5,   5,   5,   0, -10,
            -10,   0,   5,  10,  10,   5,   0, -10,
            -10,   0,   5,  10,  10,   5,   0, -10,
            -10,   0,   5,   5,   5,   5,   0, -10,
            -10,   0,   0,   0,   0,   0,   0, -10,
            -10, -10, -10, -10, -10, -10, -10, -10
    };

    private static byte[] KING = new byte[] {
              0,  20,  40, -20,   0, -20,  40,  20,
            -20, -20, -20, -20, -20, -20, -20, -20,
            -40, -40, -40, -40, -40, -40, -40, -40,
            -40, -40, -40, -40, -40, -40, -40, -40,
            -40, -40, -40, -40, -40, -40, -40, -40,
            -40, -40, -40, -40, -40, -40, -40, -40,
            -40, -40, -40, -40, -40, -40, -40, -40,
            -40, -40, -40, -40, -40, -40, -40, -40
    };

    public static int evaluateBoardPosition(Board board) {
        int score = 0;
        score += eval(0, board.ownKing(), board.enemyKing(), KING);
        score += eval(900, board.ownQueens(), board.enemyQueens());
        score += eval(500, board.ownRocks(), board.enemyRocks());
        score += eval(300, board.ownBishops(), board.enemyBishops(), BISHOP);
        score += eval(300, board.ownKnights(), board.enemyKnights(), KNIGHT);
        score += eval(100, board.ownPawns(), board.enemyPawns(), PAWN);
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
