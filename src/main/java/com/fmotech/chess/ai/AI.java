package com.fmotech.chess.ai;

import com.fmotech.chess.BitOperations;
import com.fmotech.chess.Board;
import com.fmotech.chess.Move;
import com.fmotech.chess.MoveGenerator;
import com.fmotech.chess.game.Console;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;

import java.util.Arrays;

import static com.fmotech.chess.FenFormatter.moveToFen;
import static com.fmotech.chess.Move.scoreMvvLva;
import static com.fmotech.chess.MoveGenerator.isInCheck;
import static com.fmotech.chess.ai.Evaluation.evaluateBoardPosition;
import static com.fmotech.chess.ai.PvData.ALPHA;
import static com.fmotech.chess.ai.PvData.BETA;
import static com.fmotech.chess.ai.PvData.EXACT;
import static com.fmotech.chess.ai.PvData.OPEN;
import static com.fmotech.chess.ai.PvData.UNKNOWN;
import static com.fmotech.chess.ai.PvData.create;
import static com.fmotech.chess.ai.PvData.depth;
import static com.fmotech.chess.ai.PvData.move;
import static com.fmotech.chess.ai.PvData.score;
import static com.fmotech.chess.ai.PvData.scoreType;
import static com.fmotech.chess.ai.PvData.status;
import static java.lang.Float.max;
import static java.lang.Integer.reverse;
import static java.lang.Integer.signum;
import static java.lang.Math.abs;

public class AI {

    private static final int MIN_VALUE = -32000;
    private static final int MAX_VALUE = 32000;
    public static final int MAX_DEPTH = 64;

    private final FixSizeTable table = new FixSizeTable(256);
    private final Long2LongOpenHashMap pv = new Long2LongOpenHashMap();
    private final LongOpenHashSet visited = new LongOpenHashSet();
    private final KillerMoves killers = new KillerMoves();
    private final HistoryHeuristic history = new HistoryHeuristic();

    private long timeout;
    private int nodes;
    private int initialPly;
    private int failHigh = 0;
    private int failHighFirst = 0;
    private int failFoundPvCount = 0;
    private int foundPvCount = 0;

    private final Board board;

    public AI(Board board, long[] hashes) {
        this.board = board;
        for (int i = board.ply() - 1; i >= board.ply() - board.fifty(); i--) {
            visited.add(hashes[i]);
        }
    }

    public static class Timeout extends RuntimeException {}

    public int think(int millis, int maxDepth) {
        this.timeout = System.currentTimeMillis() + (millis <= 0 ? Integer.MAX_VALUE : millis);
        maxDepth = Math.min(maxDepth, MAX_DEPTH);
        int move = 0;
        int depth = 1;
        try {
            initialPly = board.ply();
            while (depth <= maxDepth) {
                long time = System.currentTimeMillis();
                int score = negaMax(board.ply() + depth, board, MIN_VALUE, MAX_VALUE, false);
                time = System.currentTimeMillis() - time;
                long data = pv.get(board.hash());
                move = move(data);
                explainMove(board, score, depth, time);
                depth++;
                if (isMateScore(score(data))) {
                    break;
                }
            }
        } catch (Timeout e) {
        }
        Console.debug(board.ply() + " " + board);
        return move;
    }

    public boolean isMateScore(int score) {
        return MAX_VALUE - abs(score) <= 512;
    }

    private void explainMove(Board board, int score, int depth, long time) {
        String sc = "cp " + score;
        if (isMateScore(score)) {
            int sign = signum(score);
            sc = "mate " + sign * (MAX_VALUE - abs(score) - board.ply());
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < depth; i++) {
            int move = move(pv.get(board.hash()));
            if (move == 0) break;
            sb.append(moveToFen(board, move)).append(" ");
            board = board.move(move).nextTurn();
        }
        Console.info("info score %s depth %d nodes %d time %d pv %s", sc, depth, nodes, time, sb);
        Console.debug("ordering %f pvs %f hash %d/%d pv %d", failHighFirst / max(1F, failHigh), (1 - (failFoundPvCount / max(1F, foundPvCount))), table.overwrite(), table.size(), pv.size());
    }

    private int negaMax(int totalDepth, Board board, int alpha, int beta, boolean allowNull) {
        long hash = board.hash();

        int initAlpha = alpha;
        int ply = board.ply();
        int depth = totalDepth - ply;

        boolean open = visited.contains(hash);
        if ((open || board.fifty() >= 100) && ply != initialPly) {
            return -50;
        }

        if (ply >= totalDepth) {
            return quiescentSearch(board, alpha, beta);
        }

        long data = pv.getOrDefault(hash, table.get(hash));
        if (depth(data) >= depth && scoreType(data) != UNKNOWN) {
            if (scoreType(data) == EXACT)
                return score(data);
            if (scoreType(data) == ALPHA && score(data) <= alpha)
                return alpha;
            if (scoreType(data) == BETA && score(data) >= beta)
                return beta;
        }

        maintenance();

        if (allowNull && !pv.containsKey(hash) && depth > 3 && hasMajorPieces(board) && !isInCheck(board)) {
            int value = -negaMax(totalDepth - 2, board.nextTurn(), -beta, -beta + 1, false);
            if (!isMateScore(value) && value >= beta) return beta;
        }

        visited.add(hash);

        int[] moves = board.moves();
        int c = MoveGenerator.generateDirtyMoves(board, moves);
        int validMoves = 0;
        sortMoves(c, moves, ply, move(data), killers.getPrimaryKiller(ply), killers.getSecundaryKiller(ply));
        int bestMove = 0;

        for (int i = 0; i < c; i++) {
            Board next = board.move(moves[i]);
            if (!MoveGenerator.isValid(next))
                continue;

            validMoves += 1;
            int value;
            if (alpha != initAlpha) {
                foundPvCount++;
                value = -negaMax(totalDepth, next.nextTurn(), -alpha - 1, -alpha, true);
                if ((value > alpha) && (value < beta)) {
                    // Check for failure.
                    value = -negaMax(totalDepth, next.nextTurn(), -beta, -alpha, true);
                    failFoundPvCount++;
                }
            } else {
                value = -negaMax(totalDepth, next.nextTurn(), -beta, -alpha, true);
            }
            if (value >= beta) {
                if (validMoves == 1) failHighFirst++;
                failHigh++;
                table.put(hash, create(BETA, ply, depth, beta, moves[i]));
                if (!Move.isCapture(moves[i])) {
                    killers.addKiller(ply, moves[i]);
                    history.addMove(ply, totalDepth, moves[i]);
                }
                if (!open) visited.remove(hash);
                return beta;
            }
            if (value > alpha) {
                alpha = value;
                bestMove = moves[i];
            }
        }

        if (!open) visited.remove(hash);

        if (validMoves == 0) {
            alpha = isInCheck(board) ? MIN_VALUE + ply : 0;
            table.put(hash, create(EXACT, ply, depth, alpha, 0));
            return alpha;
        }

        if (alpha != initAlpha) {
            table.put(hash, create(EXACT, ply, depth, alpha, bestMove));
        } else {
            table.put(hash, create(ALPHA, ply, depth, alpha, bestMove));
        }
        if (bestMove != 0) {
            pv.put(hash, create(EXACT, ply, depth, alpha, bestMove));
        }
        return alpha;
    }

    private boolean hasMajorPieces(Board board) {
        return (board.pieces() ^ (board.kings() | board.pawns())) != 0;
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

        sortQuiscentMoves(c, moves);
        for (int i = 0; i < c; i++) {
            if (Move.evalCapture(moves[i]) < 0 && See.see(board, moves[i]) < 0)
                continue;

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
        if ((nodes & 0x0FFF) == 0)
            checkTime();
    }

    public static long maxHeuristic = 0;
    static long[] scoredMoves = new long[256];
    private void sortMoves(int size, int[] moves, int ply, int pv, int k1, int k2) {
        for (int i = 0; i < size; i++) {
            int score = 0;
            if (moves[i] == pv)
                score = -100000000;
            else if (Move.isCapture(moves[i]))
                score = -(10000000 + scoreMvvLva(moves[i]));
            else if (moves[i] == k1)
                score = -1000000;
            else if (moves[i] == k2)
                score = -999999;
            else
                score = -history.scoreMove(ply, moves[i]);
            scoredMoves[i] = BitOperations.joinInts(score, moves[i]);
        }
        Arrays.sort(scoredMoves, 0, size);
        for (int i = 0; i < size; i++) {
            moves[i] = BitOperations.lowInt(scoredMoves[i]);
        }
    }

    private void sortQuiscentMoves(int size, int[] moves) {
        for (int i = 0; i < size; i++) {
            int score = -scoreMvvLva(moves[i]);
            scoredMoves[i] = BitOperations.joinInts(score, moves[i]);
        }
        Arrays.sort(scoredMoves, 0, size);
        for (int i = 0; i < size; i++) {
            moves[i] = BitOperations.lowInt(scoredMoves[i]);
        }
    }

    private void checkTime() {
        if (System.currentTimeMillis() > timeout) {
            throw new Timeout();
        }
    }
}
