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
import static com.fmotech.chess.ai.PvData.move;
import static com.fmotech.chess.ai.PvData.ply;
import static com.fmotech.chess.ai.PvData.score;
import static com.fmotech.chess.ai.PvData.scoreType;
import static java.lang.Integer.signum;
import static java.lang.Math.abs;

public class AI {

    private static final int INFINITE = 32000;
    public static final int MAX_DEPTH = 64;
    private static final int NO_MOVE = 0;

    private final Long2LongOpenHashMap pv = new Long2LongOpenHashMap();
    private final FixSizeTable table = new FixSizeTable(256);
    private final LongOpenHashSet visited = new LongOpenHashSet();
    private final KillerMoves killers = new KillerMoves();
    private final HistoryHeuristic history = new HistoryHeuristic();

    private long timeout;
    private int nodes;
    private int initialPly;

    public static class Timeout extends RuntimeException {}

    public void setPreviousPositions(int counter, long[] hashes) {
        visited.clear();
        for (int i = 0; i < counter; i++) {
            visited.add(hashes[i]);
        }
    }

    public int think(int millis, int maxDepth, Board board) {
        this.timeout = System.currentTimeMillis() + (millis <= 0 ? Integer.MAX_VALUE : millis);
        clearForSearch(board);
        maxDepth = Math.min(maxDepth, MAX_DEPTH);
        int move = 0;
        int depth = 1;
        try {
            initialPly = board.ply();
            while (depth <= maxDepth) {
                long time = System.currentTimeMillis();
                int score = alphaBeta(-INFINITE, INFINITE, depth, board, false);
                time = System.currentTimeMillis() - time;
                long data = pv.get(board.hash());
                move = move(data);
                explainMove(board, score, depth, time);
                depth++;
            }
        } catch (Timeout e) {
        }
        Console.debug(board.ply() + " " + board);
        return move;
    }

    public boolean isMateScore(int score) {
        return INFINITE - abs(score) <= 512;
    }

    private void explainMove(Board board, int score, int depth, long time) {
        String sc = "cp " + score;
        if (isMateScore(score)) {
            int sign = signum(score);
            sc = "mate " + sign * (INFINITE - abs(score) - board.ply());
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < depth; i++) {
            int move = move(pv.get(board.hash()));
            if (move == 0) break;
            sb.append(moveToFen(board, move)).append(" ");
            board = board.move(move).nextTurn();
        }
        Console.info("info score %s depth %d nodes %d time %d pv %s", sc, depth, nodes, time, sb);
    }

    private void clearForSearch(Board board) {
        history.clear();
        killers.clear();
        updatePv(board);
        nodes = 0;
    }

    private void updatePv(Board board) {
        int counter = 0;
        while (move(pv.get(board.hash())) != 0 && counter < 32) {
            scoredMoves[counter++] = board.hash();
            scoredMoves[counter++] = pv.get(board.hash());
            board = board.move(move(pv.get(board.hash()))).nextTurn();
        }
        pv.clear();
        for (int i = 0; i < counter; i += 2) {
            pv.put(scoredMoves[i], scoredMoves[i+1]);
        }
    }

    private int alphaBeta(int alpha, int beta, int depth, Board board, boolean allowNull) {
        if (depth <= 0) {
            return quiescence(alpha, beta, board);
        }

        maintenance();

        int ply = board.ply();
        if (ply != initialPly && isDraw(board))
            return 0;

        long hash = board.hash();
        boolean open = visited.contains(hash);

        boolean inCheck = isInCheck(board);
        if (inCheck)
            depth += 1;

        long data = pv.getOrDefault(hash, table.get(hash));
        if (PvData.isValid(data) && PvData.depth(data) >= depth) {
            if (scoreType(data) == EXACT)
                return fixScore(ply, data);
            if (scoreType(data) == ALPHA && score(data) <= alpha)
                return alpha;
            if (scoreType(data) == BETA && score(data) >= beta)
                return beta;
        }

        if (allowNull && !inCheck && hasMajorPieces(board) && depth >= 4) {
            int score = -alphaBeta(-beta, -beta + 1, depth - 4, board.nextTurn(), false);
            if (score >= beta)
                return beta;
        }

        int pvMove = PvData.move(data);

        int[] moves = board.moves();
        int c = MoveGenerator.generateDirtyMoves(board, moves);
        sortMoves(c, moves, ply, pvMove, killers.getPrimaryKiller(ply), killers.getSecundaryKiller(ply));

        int legal = 0;
        int oldAlpha = alpha;
        int bestMove = NO_MOVE;

        int bestScore = -INFINITE;

        visited.add(hash);
        for (int i = 0; i < c; i++) {
            Board next = board.move(moves[i]);
            if (!MoveGenerator.isValid(next))
                continue;

            legal += 1;
            int score = -alphaBeta(-beta, -alpha, depth - 1, next.nextTurn(), true);

            if (score >= beta) {
                if (!Move.isCapture(moves[i])) killers.addKiller(board.ply(), moves[i]);
                if (!open) visited.remove(hash);
                table.put(hash, PvData.create(PvData.BETA, ply, depth, beta, moves[i]));
                return beta;
            }

            if (score > bestScore) {
                alpha = score;
                bestScore = score;
                bestMove = moves[i];
                if (!Move.isCapture(moves[i])) history.addMove(ply, depth, moves[i]);
            }
        }

        if (!open) visited.remove(hash);

        if (legal == 0) {
            return inCheck ? -INFINITE + ply : 0;
        }

        if (alpha != oldAlpha) {
            table.put(hash, PvData.create(PvData.EXACT, ply, depth, bestScore, bestMove));
            pv.put(hash, PvData.create(PvData.EXACT, ply, depth, bestScore, bestMove));
        } else {
            table.put(hash, PvData.create(PvData.ALPHA, ply, depth, alpha, bestMove));
        }
        return alpha;
    }

    private int fixScore(int ply, long data) {
        int score = score(data);
        if (!isMateScore(score))
            return score;

        return score < 0 ? score - ply(data) + ply : score + ply(data) - ply;
    }

    private int quiescence(int alpha, int beta, Board board) {
        maintenance();

        if (isDraw(board))
            return 0;

        int score = evaluateBoardPosition(board, alpha, beta);
        if (score >= beta)
            return beta;

        if (score > alpha)
            alpha = score;

        int[] moves = board.moves();
        int c = MoveGenerator.generateDirtyCaptureMoves(board, moves);

        sortQuiscentMoves(c, moves);
        for (int i = 0; i < c; i++) {
            if (Move.evalCapture(moves[i]) < 0 && See.see(board, moves[i]) < 0)
                continue;

            Board next = board.move(moves[i]);
            if (!MoveGenerator.isValid(next))
                continue;

            score = -quiescence(-beta, -alpha, next.nextTurn());

            if (score >= beta)
                return beta;

            if (score > alpha)
                alpha = score;
        }
        return alpha;
    }

    private boolean isDraw(Board board) {
        return visited.contains(board.hash()) || board.fifty() >= 100;
    }

    private boolean hasMajorPieces(Board board) {
        return (board.pieces() ^ (board.kings() | board.pawns())) != 0;
    }

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

    private void maintenance() {
        nodes += 1;
        if ((nodes & 0x0FFF) == 0)
            checkTime();
    }

    private void checkTime() {
        if (System.currentTimeMillis() > timeout) {
            throw new Timeout();
        }
    }

    public void reset() {
        pv.clear();
        table.clear();
        visited.clear();
        history.clear();
        nodes = 0;
    }
}
