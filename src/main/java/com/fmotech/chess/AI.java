package com.fmotech.chess;

import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;

import static com.fmotech.chess.Evaluation.evaluateBoardPosition;
import static com.fmotech.chess.FenFormatter.moveToFen;
import static com.fmotech.chess.MoveGenerator.isInCheck;
import static com.fmotech.chess.PvData.ALPHA;
import static com.fmotech.chess.PvData.BETA;
import static com.fmotech.chess.PvData.CLOSE;
import static com.fmotech.chess.PvData.EXACT;
import static com.fmotech.chess.PvData.OPEN;
import static com.fmotech.chess.PvData.UNKNOWN;
import static com.fmotech.chess.PvData.create;
import static com.fmotech.chess.PvData.depth;
import static com.fmotech.chess.PvData.move;
import static com.fmotech.chess.PvData.score;
import static com.fmotech.chess.PvData.scoreType;
import static com.fmotech.chess.PvData.status;
import static java.lang.Float.max;
import static java.lang.Integer.signum;
import static java.lang.Math.abs;

public class AI {

    private static final int MIN_VALUE = -32000;
    private static final int MAX_VALUE = 32000;

    public static boolean SILENT = false;
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
                if (!SILENT)
                    explainMove(board, score, depth, time);
                depth++;
                if (MAX_VALUE - abs(score(data)) <= 512) {
                    break;
                }
            }
        } catch (Timeout e) {
        }
        if (!SILENT)
            System.out.println(board.ply() + " " + board);
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

    private int negaMax(int depth, Board board, int alpha, int beta) {
        long hash = board.hash();
        long data = table.get(hash);

        int initAlpha = alpha;

        long status = status(data);
        int ply = board.ply();
        if ((status == OPEN || board.fifty() >= 100) && ply != initialPly)
            return 0;

        if (depth(data) >= depth - ply && scoreType(data) != UNKNOWN) {
            if (scoreType(data) != BETA && score(data) <= alpha)
                return alpha;
            if (scoreType(data) != ALPHA && score(data) >= beta)
                return beta;
        }

        if (ply == depth) {
            alpha = /*isChecked(board) ? beta :*/ quiescentSearch(board, alpha, beta);
            recordPv(hash, CLOSE, ply, 0, alpha, move(data), initAlpha, beta);
            return alpha;
        }

        maintenance();

        table.put(hash, data | OPEN);

        int[] moves = board.moves();
        int c = MoveGenerator.generateDirtyMoves(board, moves);
        int validMoves = 0;
        boolean followPv = sortMoves(c, moves, move(table.get(hash)));
        boolean foundPv = false;
        int bestMove = 0;

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
                table.put(hash, create(status | BETA, ply, depth - ply, beta, moves[i]));
                return beta;
            }
            if (value > alpha) {
                alpha = value;
                bestMove = moves[i];
                foundPv = true;
            }
        }

        if (validMoves == 0)
            alpha = isInCheck(board) ? MIN_VALUE + ply : 0;

        recordPv(hash, status, ply, depth - ply, alpha, bestMove, initAlpha, beta);
        return alpha;
    }

    private void recordPv(long hash, long status, int ply, int depth, int score, int move, int alpha, int beta) {
        if (score == alpha)
            table.put(hash, create(status | ALPHA, ply, depth, score, move));
        else if (score == beta)
            table.put(hash, create(status | BETA, ply, depth, score, move));
        else
            table.put(hash, create(status | EXACT, ply, depth, score, move));
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
