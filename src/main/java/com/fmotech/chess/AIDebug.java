package com.fmotech.chess;

import static com.fmotech.chess.FenFormatter.moveToFen;
import static com.fmotech.chess.SimpleEvaluation.evaluateBoardPosition;

public class AIDebug {

    private static final int MIN_VALUE = -1000000;
    private static final int MAX_VALUE = 1000000;

    public static void main(String[] args) {
        Board b = Board.INIT;
        for (int i = 1; i < 200; i++) {
            b = move(b);
        }
    }

    private static Board move(Board board) {
        long result = negaMax(4, board, MIN_VALUE, MAX_VALUE);
        int move = (int) (result >>> 32);
        System.out.print(moveToFen(board, move) + " {" + (int) (result & 0xFFFFFFFFL) + "} ");
        if (move == 0) System.exit(0);
        return board.move(move).nextTurn();
    }

    public static int bestMove(Board board, int depth) {
        long result = negaMax(depth, board, MIN_VALUE, MAX_VALUE);
        return (int) (result >>> 32);
    }

    private static long negaMax(int depth, Board board, int alpha, int beta) {
        if (depth == 0) return quiescentNegaMax(32, board, alpha, beta);

        int bestValue = MIN_VALUE;
        long bestMove = 0;

        int[] moves = board.moves();
        int c = MoveGenerator.generateDirtyMoves(board, moves);

        for (int i = 0; i < c; i++) {
            Board next = board.move(moves[i]);
            if (MoveGenerator.isValid(next)) {
                int value = -((int) (negaMax(depth - 1, next.nextTurn(), -beta, -alpha) & 0xFFFFFFFFL));
                if (value > bestValue) {
                    bestValue = value;
                    bestMove = moves[i];
                }
                alpha = Math.max(alpha, value);
                if (alpha >= beta) break;
            }
        }
        return bestMove << 32 | (bestValue & 0xFFFFFFFFL);
    }

    private static int quiescentNegaMax(int depth, Board board, int alpha, int beta) {
        if (depth == 0) return evaluateBoardPosition(board);

        int value = evaluateBoardPosition(board);
        if (value >= beta)
            return beta;
        if (value > alpha)
            alpha = value;

        int[] moves = board.moves();
        int c = MoveGenerator.generateDirtyAttackMoves(board, moves);

        for (int i = 0; i < c; i++) {
            Board next = board.move(moves[i]);
            if (MoveGenerator.isValid(next)) {
                value = -quiescentNegaMax(depth - 1, next.nextTurn(), -beta, -alpha);
                if (value > alpha) {
                    if (value >= beta)
                        return beta;
                    alpha = value;
                }
            }
        }
        return alpha;
    }
}
