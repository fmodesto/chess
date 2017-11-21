package com.fmotech.chess;

import static com.fmotech.chess.FenFormatter.moveToFen;
import static com.fmotech.chess.SimpleEvaluation.evaluateBoardPosition;

public class AIDebug {

    private static final int MIN_VALUE = -1000000;
    private static final int MAX_VALUE = 1000000;

    public static void main(String[] args) {
        Board b = Board.INIT;
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < 100; i++) {
            b = move(i, b, sb);
        }
    }

    private static Board move(int num, Board board, StringBuilder sb) {
        long result = negaMax(5, board, MIN_VALUE, MAX_VALUE);
        int move = (int) (result >>> 32);
        long[] moves = explainMove(board, result, 4);
        showMoves(board, moves, sb);

        System.out.println(num + " " + board.move(move) + " " + moveToFen(board, move) + ":" + sb);
        if (move == 0) {
            System.err.println("ERROR");
            System.exit(0);
        }
        System.out.println("-------------------------");
        return board.move(move).nextTurn();
    }

    public static int bestMove(Board board, int depth) {
        long result = negaMax(depth, board, MIN_VALUE, MAX_VALUE);
        return (int) (result >>> 32);
    }

    private static long[] explainMove(Board original, long result, int depth) {
        long[] results = new long[depth+1];
        results[0] = result;
        Board board = original.move((int) (result >>> 32));
        for (int i = depth; i > 0; i--) {
            board = board.nextTurn();
            long res = negaMax(i, board, MIN_VALUE, MAX_VALUE);
            results[depth - i + 1] = res;
            board = board.move((int) (res >>> 32));
        }
        return results;
    }

    private static void showMoves(Board board, long[] moves, StringBuilder sb) {
        for (int i = 0; i < moves.length; i++) {
            Board next = board.move((int) (moves[i] >>> 32)).nextTurn();
            sb.append(" ");
            if (i == 1) sb.append("(").append(moveToFen(board.nextTurn(), (int) (moves[0] >>> 32))).append(" ");
            sb.append(moveToFen(board, (int) (moves[i] >>> 32))).append(" {").append(score(next)).append("}");
            System.out.printf("Score at move %5s: %-8d Board: %d%n", moveToFen(board, (int) (moves[i] >>> 32)), (int) (moves[i] & 0xFFFFFFFFL), score(next));
            board = next;
        }
        sb.append(")");
    }
/*
[Event "?"]
[Site "?"]
[Date "????.??.??"]
[Round "?"]
[White "?"]
[Black "?"]
[Result "*"]

1.e4 d5 2.d4
    ( 2.exd5 Qxd5 )
2...dxe4 3.d5 *

 */
    private static int score(Board board) {
        return board.whiteTurn() ? evaluateBoardPosition(board) : -evaluateBoardPosition(board);
    }

    private static long negaMax(int depth, Board board, int alpha, int beta) {
        if (depth == 0) return evaluateBoardPosition(board);

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
}
