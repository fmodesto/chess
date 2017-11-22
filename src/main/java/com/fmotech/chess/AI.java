package com.fmotech.chess;

import static com.fmotech.chess.BitOperations.highInt;
import static com.fmotech.chess.BitOperations.joinInts;
import static com.fmotech.chess.BitOperations.lowInt;
import static com.fmotech.chess.FenFormatter.moveToFen;
import static com.fmotech.chess.SimpleEvaluation.evaluateBoardPosition;

public class AI {

    private static final int MAX_DEPTH = 16;
    private static final int MIN_VALUE = -1000000;
    private static final int MAX_VALUE = 1000000;
    private final long timeout;

    private long[][] pv = new long[MAX_DEPTH][MAX_DEPTH];
    private int[] pvLength = new int[MAX_DEPTH];
    private int nodes;

    public AI(int millis) {
        this.timeout = System.currentTimeMillis() + millis;
    }

    public static class Timeout extends RuntimeException {}

    public static void main(String[] args) {
        Board board = FenFormatter.fromFen("8/1kp5/p1p1R3/6P1/1p1r4/7P/6K1/8 w - - 0 39");
        for (int i = 0; i < 100; i++) {
            board = move(board);
        }
        System.out.println(board);
    }

    private static Board move(Board board) {
        AI ai = new AI(20000);
        int move = ai.think(board);
        return board.move(move).nextTurn();
    }

    public int think(Board board) {
        int move = 0;
        try {
            for (int i = 1; i < MAX_DEPTH; i++) {
                negaMax(0, i, board, MIN_VALUE, MAX_VALUE);
                move = lowInt(pv[0][0]);
                explainMove(board);
            }
        } catch (Timeout e) {
            System.out.println(nodes);
        }
        return move;
    }

    private void explainMove(Board board) {
        for (int i = 0; i < pvLength[0]; i++) {
            long play = pv[0][i];
            System.out.print(moveToFen(board, lowInt(play)) + " " + highInt(play) + " ");
            board = board.move(lowInt(play)).nextTurn();
        }
        System.out.println();
    }

    private int negaMax(int ply, int depth, Board board, int alpha, int beta) {
        if (ply == depth) return quiescentNegaMax(ply, board, alpha, beta);

        nodes += 1;
        if ((nodes & 1023) == 0)
            checkTime();

        pvLength[ply] = ply;
        int[] moves = board.moves();
        int c = MoveGenerator.generateDirtyMoves(board, moves);

        for (int i = 0; i < c; i++) {
            Board next = board.move(moves[i]);
            if (!MoveGenerator.isValid(next))
                continue;

            int value = -negaMax(ply + 1, depth, next.nextTurn(), -beta, -alpha);
            if (value > alpha) {
                if (value >= beta)
                    return beta;
                alpha = value;
                updatePv(ply, moves, i, value);
            }
        }
        return alpha;
    }

    private int quiescentNegaMax(int ply, Board board, int alpha, int beta) {
        if (ply > MAX_DEPTH - 2) return evaluateBoardPosition(board);

        nodes += 1;
        if ((nodes & 1023) == 0)
            checkTime();

        int value = evaluateBoardPosition(board);
        if (value >= beta)
            return beta;
        if (value > alpha)
            alpha = value;

        pvLength[ply] = ply;
        int[] moves = board.moves();
        int c = MoveGenerator.generateDirtyAttackMoves(board, moves);

        for (int i = 0; i < c; i++) {
            Board next = board.move(moves[i]);
            if (MoveGenerator.isValid(next)) {
                value = -quiescentNegaMax(ply + 1, next.nextTurn(), -beta, -alpha);
                if (value > alpha) {
                    if (value >= beta)
                        return beta;
                    alpha = value;
                    updatePv(ply, moves, i, value);
                }
            }
        }
        return alpha;
    }

    private void updatePv(int ply, int[] moves, int i, int value) {
        pv[ply][ply] =  joinInts(value, moves[i]);
        for (int j = ply + 1; j < pvLength[ply + 1]; j++) {
            pv[ply][j] = pv[ply + 1][j];
        }
        pvLength[ply] = pvLength[ply + 1];
    }

    private void checkTime() {
        if (System.currentTimeMillis() > timeout) {
            throw new Timeout();
        }
    }
}
