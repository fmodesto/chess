package com.fmotech.chess;

import static com.fmotech.chess.BitOperations.lowestBitPosition;
import static com.fmotech.chess.BitOperations.nextLowestBit;
import static com.fmotech.chess.FenFormatter.moveFromFen;

public class SimpleEvaluation {

    public static void main(String[] args) {
//        System.out.println(evaluateBoardPosition(Board.INIT));
//        System.out.println(evaluateBoardPosition(Board.INIT.nextTurn()));
//        System.out.println(evaluateBoardPosition(Board.INIT.move(moveFromFen(Board.INIT, "e2e4"))));
//        System.out.println(evaluateBoardPosition(Board.fen("rnb1kbnr/ppp2ppp/4pq2/3p4/8/2NBPN2/PPPP1PPP/R1BQK2R b KQkq - 0 5")));
//        System.out.println(evaluateBoardPosition(Board.fen("rnb1kbnr/ppp2ppp/4pq2/3p4/8/2NBPN2/PPPP1PPP/R1BQ1K1R b KQkq - 0 5")));
    }

    private static byte[] PAWN = toBoardPosition(
            0,   0,   0,   0,   0,   0,   0,   0,
           20,  20,  20,  30,  30,  20,  20,  20,
           10,  10,  10,  20,  20,  10,  10,  10,
            5,   5,   5,  10,  10,   5,   5,   5,
            0,   0,  10,  20,  20,  10,   0,   0,
            5,   0,   0,   5,   5,   0,   0,   5,
           10,  10,   0, -10, -10,   0,  10,  10,
            0,   0,   0,   0,   0,   0,   0,   0
    );

    private static byte[] KNIGHT = toBoardPosition(
            0,   0,   0,   0,   0,   0,   0,   0,
            0,   0,   5,  10,  10,   5,   0,   0,
            5,  10,  10,  20,  20,  10,  10,   5,
            5,  10,  15,  20,  20,  15,  10,   5,
            0,   0,  10,  20,  20,  10,   5,   0,
            0,   0,  10,  10,  10,  10,   0,   0,
            0,   0,   0,   5,   5,   0,   0,   0,
            0, -10,   0,   0,   0,   0, -10,   0
    );

    private static byte[] BISHOP = toBoardPosition(
            0,   0,   0,   0,   0,   0,   0,   0,
            0,   0,   0,  10,  10,   0,   0,   0,
            0,   0,  10,  15,  15,  10,   0,   0,
            0,  10,  15,  20,  20,  15,  10,   0,
            0,  10,  15,  20,  20,  15,  10,   0,
            0,   0,  10,  15,  15,  10,   0,   0,
            0,   0,   0,  10,  10,   0,   0,   0,
            0,   0, -10,   0,   0, -10,   0,   0
    );

    private static byte[] ROCK = toBoardPosition(
            0,   0,   5,  10,  10,   5,   0,   0,
           25,  25,  25,  25,  25,  25,  25,  25,
            0,   0,   5,  10,  10,   5,   0,   0,
            0,   0,   5,  10,  10,   5,   0,   0,
            0,   0,   5,  10,  10,   5,   0,   0,
            0,   0,   5,  10,  10,   5,   0,   0,
            0,   0,   5,  10,  10,   5,   0,   0,
            0,   0,   5,  10,  10,   5,   0,   0
    );

    private static byte[] toBoardPosition(int... data) {
//        for (int i = 0; i < 64; i++) {
//            if (i % 8 == 0) System.out.println();
//            System.out.print(String.format("%3d, ", data[i]));
//        }
//        System.out.println();
        byte[] array = new byte[data.length];
        for (int i = 1; i <= data.length; i++) {
            array[data.length - i] = (byte) data[i - 1];
        }
        return array;
    }

    public static int evaluateBoardPosition(Board board) {
        int score = 0;
//        score += eval(10000, board.ownKing(), board.enemyKing(), KING);
        score += eval(1000, board.ownQueens(), board.enemyQueens());
        score += eval(550, board.ownRocks(), board.enemyRocks(), ROCK);
        score += eval(325, board.ownBishops(), board.enemyBishops(), BISHOP);
        score += eval(325, board.ownKnights(), board.enemyKnights(), KNIGHT);
        score += eval(100, board.ownPawns(), board.enemyPawns(), PAWN);
        return score;
    }

    private static int eval(int points, long own, long enemy, byte[] table) {
        int sum = 0;
        enemy = BitOperations.reverse(enemy);
        while (own != 0) {
            sum += points + table[lowestBitPosition(own)];
            own = nextLowestBit(own);
        }
        while (enemy != 0) {
            sum -= points + table[lowestBitPosition(enemy)];
            enemy = nextLowestBit(enemy);
        }
        return sum;
    }

    private static int eval(int points, long own, long enemy) {
        return points * (BitOperations.bitCount(own) - BitOperations.bitCount(enemy));
    }
}
