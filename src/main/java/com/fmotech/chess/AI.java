package com.fmotech.chess;

import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;

import static com.fmotech.chess.FenFormatter.moveToFen;
import static com.fmotech.chess.PvData.BETA;
import static com.fmotech.chess.PvData.CLOSE;
import static com.fmotech.chess.PvData.EXACT;
import static com.fmotech.chess.PvData.OPEN;
import static com.fmotech.chess.PvData.create;
import static com.fmotech.chess.PvData.move;
import static com.fmotech.chess.PvData.score;
import static com.fmotech.chess.PvData.status;
import static com.fmotech.chess.SimpleEvaluation.evaluateBoardPosition;
import static java.lang.Float.max;
import static java.lang.Integer.signum;
import static java.lang.Math.abs;

public class AI {

    private static final int MIN_VALUE = -32000;
    private static final int MAX_VALUE = 32000;

    private final long timeout;
    private final int maxDepth;
    private int nodes;
    private int initialPly;
    private Long2LongOpenHashMap table = new Long2LongOpenHashMap();

    private int failHigh = 0;
    private int failHighFirst = 0;
    private int failFoundPvCount = 0;
    private int foundPvCount = 0;
    private final Board board;

    public AI(int millis, int maxDepth, Board board, long[] hashes) {
        this.timeout = System.currentTimeMillis() + (millis <= 0 ? Integer.MAX_VALUE : millis);
        this.maxDepth = maxDepth;
        this.board = board;
        for (int i = board.ply() - 2; i >= board.ply() - board.fifty(); i--) {
            table.put(hashes[i], OPEN);
        }
    }

    public static class Timeout extends RuntimeException {}

    public int think() {
        int move = 0;
        int depth = 1;
        try {
            initialPly = board.ply();
            while (depth <= maxDepth) {
                long time = System.currentTimeMillis();
                int score = negaMax(board.ply() + depth, board, MIN_VALUE, MAX_VALUE);
                time = System.currentTimeMillis() - time;
                long data = table.get(board.hash());
                move = move(data);
                explainMove(board, score, depth, time);
                depth++;
                if (MAX_VALUE - abs(score(data)) <= 512) {
                    break;
                }
            }
        } catch (Timeout e) {
        }
        depth--;
        System.out.println(depth + " " + nodes + " " + foundPvCount);
        return move;
    }

    private void explainMove(Board board, int score, int depth, long time) {
        String sc = "cp " + score;
        if (MAX_VALUE - abs(score) <= 512) {
            int sign = signum(score);
            sc = "mate " + sign * (MAX_VALUE - abs(score) - board.ply());
        }
        System.out.printf("info score %s depth %d nodes %d time %d pv ", sc, depth, nodes, time);

        for (int i = 0; i < depth; i++) {
            long play = table.get(board.hash());
            int move = move(play);
            if (move == 0) break;
            System.out.print(moveToFen(board, move) + " ");
            board = board.move(move).nextTurn();
        }
        System.out.println("\t_ordering " + failHighFirst / max(1F, failHigh)
                + " _pvs " + (1 - (failFoundPvCount / max(1F, foundPvCount)))
                + " _table " + table.size());
    }

    private int eval(Board board) {
        return board.whiteTurn() ? evaluateBoardPosition(board) : -evaluateBoardPosition(board);
    }

    private int negaMax(int depth, Board board, int alpha, int beta) {
        long hash = board.hash();
        long data = table.get(hash);

        long status = status(data);
        if ((status == OPEN || board.fifty() >= 100) && board.ply() != initialPly)
            return 0;

        if (board.ply() == depth) {
            alpha = quiescentSearch(board, alpha, beta);
            table.put(hash, create(CLOSE | EXACT, board.ply(), 0, alpha, move(table.get(hash))));
            return alpha;
        }

        maintenance();

        table.put(hash, data | OPEN);

        int[] moves = board.moves();
        int c = MoveGenerator.generateDirtyMoves(board, moves);
        int validMoves = 0;
        boolean followPv = sortMoves(c, moves, move(table.get(hash)));
        boolean foundPv = false;

        for (int i = 0; i < c; i++) {
            if (!followPv)
                sortMoves(i, c, moves);
            followPv = false;

            Board next = board.move(moves[i]);
            if (!MoveGenerator.isValid(next))
                continue;

            validMoves += 1;
            int value;
            if (foundPv) {
                foundPvCount++;
                value = -negaMax(depth, next.nextTurn(), -alpha - 1, -alpha);
                if ((value > alpha) && (value < beta)) {
                    // Check for failure.
                    value = -negaMax(depth, next.nextTurn(), -beta, -alpha);
                    failFoundPvCount++;
                }
            } else {
                value = -negaMax(depth, next.nextTurn(), -beta, -alpha);
            }
            if (value >= beta) {
                if (validMoves == 1) failHighFirst++;
                failHigh++;
                table.put(hash, create(status | BETA, board.ply(), depth - board.ply(), beta, moves[i]));
                return beta;
            }
            if (value > alpha) {
                alpha = value;
                table.put(board.hash(), create(OPEN | EXACT, board.ply(), depth - board.ply(), alpha, moves[i]));
                foundPv = true;
            }
        }

        table.put(hash, create(status | EXACT, board.ply(), depth - board.ply(), alpha, move(table.get(hash))));
        if (validMoves == 0) {
            boolean check = MoveGenerator.isChecked(board);
            return check ? MIN_VALUE + board.ply() : 0;
        }
        return alpha;
    }

    private int quiescentSearch(Board board, int alpha, int beta) {
        maintenance();

        if (board.fifty() >= 100 || status(table.get(board.hash())) == OPEN)
            return 0;

        int value = evaluateBoardPosition(board);

        if (value >= beta)
            return beta;
        if (value > alpha)
            alpha = value;

        int[] moves = board.moves();
        int c = MoveGenerator.generateDirtyCaptureMoves(board, moves);
        int validMoves = 0;

        for (int i = 0; i < c; i++) {
            sortMoves(i, c, moves);

            Board next = board.move(moves[i]);
            if (!MoveGenerator.isValid(next))
                continue;

            validMoves += 1;
            value = -quiescentSearch(next.nextTurn(), -beta, -alpha);
            if (value >= beta) {
                if (validMoves == 1) failHighFirst++;
                failHigh++;
                return beta;
            }
            if (value > alpha) {
                alpha = value;
            }
        }

        return alpha;
    }

    private void maintenance() {
        nodes += 1;
        if ((nodes & 0xFFFF) == 0)
            checkTime();
    }

    private void sortMoves(int from, int to, int[] moves) {
        int index = from;
        int best = (moves[from] >>> 16) & 0xFF;
        for (int i = from + 1; i < to; i++) {
            if (((moves[i] >>> 16) & 0xFF) > best) {
                best = (moves[i] >>> 16) & 0xFF;
                index = i;
            }
        }

        int t = moves[from];
        moves[from] = moves[index];
        moves[index] = t;
    }

    private boolean sortMoves(int size, int[] moves, int pv) {
        if (pv == 0) return false;
        for (int i = 0; i < size; i++) {
            if (moves[i] == pv) {
                moves[i] = moves[0];
                moves[0] = pv;
            }
        }
        return moves[0] == pv;
    }

    private void checkTime() {
        if (System.currentTimeMillis() > timeout) {
            throw new Timeout();
        }
    }
}
