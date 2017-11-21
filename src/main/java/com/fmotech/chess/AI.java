package com.fmotech.chess;

import java.util.Random;

import static com.fmotech.chess.FenFormatter.moveToFen;
import static com.fmotech.chess.SimpleEvaluation.evaluateBoardPosition;

public class AI {

    private static final int MIN_VALUE = -1000000;
    private static final int MAX_VALUE = 1000000;
    private static Random random = new Random();

    public static void main(String[] args) {
        Board b = Board.INIT;
        for (int i = 0; i < 100; i++) {
            b = move(b);
        }
    }

    private static Board move(Board board) {
        System.out.println(board);
        int move1 = (int) (negaMax(5, board, true, MIN_VALUE, MAX_VALUE) >>> 32);
        int move2 = minMax(5, board, true);
        System.out.println("---");
        if (move1 == 0 || move1 != move2) {
            System.err.println("ERROR");
            System.exit(0);
        }
        return board.move(move1).nextTurn();
    }

    static long c = 0;

    public static int bestMove(Board board) {
        long result = negaMax(5, board, true, MIN_VALUE, MAX_VALUE);
        return (int) (result >>> 32);
    }

    private static long negaMax(int depth, Board board, boolean firstTime) {
        if (depth == 0) return evaluateBoardPosition(board);

        c += 1;
        int bestScore = MIN_VALUE;
        long bestMove = 0;

        int[] moves = board.moves();
        int c = MoveGenerator.generateDirtyMoves(board, moves);
//        if (firstTime) {
////            shuffle(moves, c);
//        }

        String scores = "";
        for (int i = 0; i < c; i++) {
            Board next = board.move(moves[i]);
            if (MoveGenerator.isValid(next)) {
                int score = -(int) (negaMax(depth - 1, next.nextTurn(), false) & 0xFFFFFFFFL);
                scores += " " + score;// + ":" + moveToFen(board, moves[i]);
                if (score > bestScore) {
                    bestScore = score;
                    bestMove = moves[i];
                }
            }
        }

        if (firstTime)
            System.out.println("NegaMaxDepth: " + depth + "." + scores + ". Choosing " + moveToFen(board, (int) bestMove) + " " + bestScore);
        return bestMove << 32 | (bestScore & 0xFFFFFFFFL);
    }

    private static long negaMax(int depth, Board board, boolean firstTime, int alpha, int beta) {
        if (depth == 0) return evaluateBoardPosition(board);

        c += 1;
        int bestValue = MIN_VALUE;
        long bestMove = 0;

        int[] moves = board.moves();
        int c = MoveGenerator.generateDirtyMoves(board, moves);
//        if (firstTime) {
//            shuffle(moves, c);
//        }
//        Arrays.sort(moves, 0, c);

        String scores = "";
        for (int i = 0; i < c; i++) {
            Board next = board.move(moves[i]);
            if (MoveGenerator.isValid(next)) {
                int value = -((int) (negaMax(depth - 1, next.nextTurn(), false, -beta, -alpha) & 0xFFFFFFFFL));
                scores += " " + value;// + ":" + moveToFen(board, moves[i]);
                if (value > bestValue) {
                    bestValue = value;
                    bestMove = moves[i];
                }
                alpha = Math.max(alpha, value);
                if (alpha >= beta) break;
            }//
        }
/*
[Event "Scid vs. Mac"]
[Site "?"]
[Date "2017.11.19"]
[Round "1"]
[White "FmoChess"]
[Black "Fmochess2"]
[Result "*"]
[Movetime "10"]

1.f3 e5 2.c3 Qf6 3.d4 exd4 4.Qxd4 Qxd4 5.cxd4 b6 6.Bf4 c5 7.dxc5 Bxc5 8.Be5 f6 9.Bf4 Bd4 10.a3 Ne7 11.Nh3 Kd8 12.Nd2 Ng6 13.Bxb8 Rxb8 14.Rb1 Nf8 15.g3 Bc5 16.Ra1 Ne6 17.Nf4 Nxf4 18.gxf4 Ke7 19.Rd1 Be3 20.h3 Bb7 21.f5 Bg5 22.h4 Be3 23.Rh2 Rbe8 24.a4 Bf4 25.Rg2 b5 26.Nb1 g5 27.hxg5 bxa4 28.gxf6+ Kxf6 29.Rd3 Bc6 30.e4 Bc7 31.Re3 Bb6 32.Rd3 d5 33.Rg3 Bc7 34.Rg2 dxe4 35.fxe4 Bxe4 36.Kd1 Bf4 37.Nc3 Bxg2 38.Bxg2 Rhg8 39.Nd5+ Ke5 40.Nxf4 Kxf4 41.Bh3 Kg5 42.Rg3+ Kh5 43.Ra3 Kh4 44.Bf1 Rg1 45.Kd2 Rxf1 46.Rxa4+ Kg3 47.Ra3+ Kh4 48.Rxa7 Kg3 49.Rxh7 Rf2+ 50.Kc3 Rf3+ 51.Kd4 Rf2 52.Kd5 Rxf5+ 53.Kc6 Ra8 54.Rg7+ Kh4 55.Rc7 Re5 56.Kb7 Raa5 57.Rc4+ Kh3 58.Kc6 Rh5 59.Kc7 Rad5 60.Rc3+ Kg4 61.Rc1 Rhf5 62.b4 Kh4 63.Kc8 Kh5 64.Kc7 Kh6 65.Rc2 *
[Event "Scid vs. Mac"]
[Site "?"]
[Date "2017.11.19"]
[Round "1"]
[White "FmoChess"]
[Black "Fmochess2"]
[Result "*"]
[Movetime "10"]

1.f3 e5 2.c3 Qf6 3.d4 exd4 4.Qxd4 Qxd4 5.cxd4 b6 6.Bf4 c5 7.dxc5 Bxc5 8.Be5 f6 9.Bf4 Bd4 10.a3 Ne7 11.Nh3 Kd8 12.Nd2 Ng6 13.Bxb8 Rxb8 14.Rb1 Nf8 15.g3 Bc5 16.Ra1 Ne6 17.Nf4 Nxf4 18.gxf4 Ke7 19.Rd1 Be3 20.h3 Bb7 21.f5 Bg5 22.h4 Be3 23.Rh2 Rbe8 24.a4 Bf4 25.Rg2 b5 26.Nb1 g5 27.hxg5 bxa4 28.gxf6+ Kxf6 29.Rd3 Bc6 30.e4 Bc7 31.Re3 Bb6 32.Rd3 d5 33.Rg3 Bc7 34.Rg2 dxe4 35.fxe4 Bxe4 36.Kd1 Bf4 37.Nc3 Bxg2 38.Bxg2 Rhg8 39.Nd5+ Ke5 40.Nxf4 Kxf4 41.Bh3 Kg5 42.Rg3+ Kh5 43.Ra3 Kh4 44.Bf1 Rg1 45.Kd2 Rxf1 46.Rxa4+ Kg3 47.Ra3+ Kh4 48.Rxa7 Kg3 49.Rxh7 Rf2+ 50.Kc3 Rf3+ 51.Kd4 Rf2 52.Kd5 Rxf5+ 53.Kc6 Ra8 54.Rg7+ Kh4 55.Rc7 Re5 56.Kb7 Raa5 57.Rc4+ Kh3 58.Kc6 Rh5 59.Kc7 Rad5 60.Rc3+ Kg4 61.Rc1 Rhf5 62.b4 Kh4 63.Kc8 Kh5 64.Kc7 Kh6 65.Rc2 Rf6 66.Re2 Rfd6 67.Re3 Ra6 68.Re2 Ra3 69.Rg2 Re3 70.Rg1 Rd2 71.Rf1 Kg6 72.Ra1 Kh7 73.Rh1+ Kg7 74.Kb7 Re6 75.Rg1+ Kh7 76.Ka8 Ree2 77.Kb7 Rd7+ 78.Kc8 Rd4 79.b5 Ra2 80.Rc1 Kg7 81.Rg1+ Kh7 82.Rh1+ Kg6 83.Re1 Rc2+ 84.Kb8 Kf5 85.Ra1 Rh2 86.Kc7 Rhh4 87.Rf1+ Kg4 88.Rb1 Kf4 89.Kb8 Kf5 90.Rc1 Rh5 91.b6 Ra4 92.Rg1 Rf4 93.Ka7 Rh6 94.Rc1 Rf2 95.Rc7 Rf1 96.Kb7 Ra1 97.Rc4 Re6 98.Rc5+ Kg6 99.Kc7 Kf6 100.Rh5 Rb1 101.Ra5 Rexb6 102.Ra3 R1b5 103.Ra1 Ke7 104.Ra8 Rg6 105.Ra3 Rd6 106.Ra4 Rf6 107.Ra2 Rff5 108.Ra6 Rf8 109.Ra7 Rh5 110.Ra3 Rh7 111.Ra5 Ke6+ 112.Kb6 Rd7 113.Ra3 Rh8 114.Ra7 Re8 115.Ra3 Rde7 116.Ra5 Rd7 117.Ra1 Rh8 118.Ra4 Kf7 119.Kc5 Rh3 120.Ra5 Rh4 121.Ra8 Rh2 122.Kc4 Rf2 123.Kc3 Kg6 124.Rc8 Rf5 125.Rc6+ Kg5 126.Re6 Rdd5 127.Re3 Rfe5 128.Rxe5+ Rxe5 129.Kd4 Kf4 130.Kd3 Kg3 131.Kc4 Kf2 132.Kd4 Re7 133.Kd5 Kg1 134.Kc4 Re6 135.Kc5 Re5+ 136.Kc4 Re7 137.Kb4 Rf7 138.Ka4 Kh1 139.Ka3 Rf1 140.Ka4 Rf4+ 141.Ka5 Rf6 142.Kb4 Rh6 143.Ka3 Rh7 144.Kb4 Rh3 145.Kc4 Ra3 146.Kb4 Rf3 147.Kc5 *

 */
        if (firstTime)
            System.out.println("NegaMaxDepth: " + depth + "." + scores + ". Choosing " + moveToFen(board, (int) bestMove) + " " + bestValue);
        return bestMove << 32 | (bestValue & 0xFFFFFFFFL);
    }

    private static int minMax(int depth, Board board, boolean firstTime) {
        if (board.whiteTurn()) {
            return (int) (max(depth, board, firstTime) >>> 32);
        } else {
            return (int) (min(depth, board, firstTime) >>> 32);
        }
    }

    private static long max(int depth, Board board, boolean firstTime) {
        if (depth == 0) return board.whiteTurn() ? evaluateBoardPosition(board) : -evaluateBoardPosition(board);

        c += 1;
        long bestScore = MIN_VALUE;
        long bestMove = 0;
        StringBuilder sb = new StringBuilder();
        sb.append("MaxDepth: ").append(depth).append(".");

        int[] moves = board.moves();
        int c = MoveGenerator.generateDirtyMoves(board, moves);

        for (int i = 0; i < c; i++) {
            Board next = board.move(moves[i]);
            if (MoveGenerator.isValid(next)) {
                int score = (int) (min(depth - 1, next.nextTurn(), false) & 0xFFFFFFFFL);
                sb.append(" ").append(score);
                if (score > bestScore) {
                    bestScore = score;
                    bestMove = moves[i];
                }
            }
        }
        sb.append(". Choosing move: ").append(moveToFen(board, (int) bestMove)).append(" ").append(String.format("%-4d", bestScore));
        if (firstTime)
            System.out.println(sb);
        return bestMove << 32 | (bestScore & 0xFFFFFFFFL);
    }

    private static long min(int depth, Board board, boolean firstTime) {
        if (depth == 0) return board.whiteTurn() ? evaluateBoardPosition(board) : -evaluateBoardPosition(board);

        c += 1;
        long bestScore = MAX_VALUE;
        long bestMove = 0;
        StringBuilder sb = new StringBuilder();
        sb.append("MinDepth: ").append(depth).append(".");

        int[] moves = board.moves();
        int c = MoveGenerator.generateDirtyMoves(board, moves);

        for (int i = 0; i < c; i++) {
            Board next = board.move(moves[i]);
            if (MoveGenerator.isValid(next)) {
                int score = (int) (max(depth - 1, next.nextTurn(), false) & 0xFFFFFFFFL);
                sb.append(" ").append(score);
                if (score < bestScore) {
                    bestScore = score;
                    bestMove = moves[i];
                }
            }
        }
        sb.append(". Choosing move: ").append(moveToFen(board, (int) bestMove)).append(" ").append(String.format("%-4d", bestScore));
        if (firstTime)
            System.out.println(sb);
        return bestMove << 32 | (bestScore & 0xFFFFFFFFL);
    }

    private static int positionEvaluation(Board board) {
        int[] moves = board.moves();
        int c = MoveGenerator.generateValidMoves(board, moves);
        shuffle(moves, c);
        int bestScore = MIN_VALUE;
        int bestMove = 0;
        for (int i = 0; i < c; i++) {
            Board next = board.move(moves[i]);
            int score = SimpleEvaluation.evaluateBoardCount(next);
            if (score > bestScore) {
                bestScore = score;
                bestMove = moves[i];
            }
        }
        return bestMove;
    }

    private static void shuffle(int[] moves, int c) {
        for (int i = c - 1; i > 0; i--) {
            int index = random.nextInt(i + 1);
            int tmp = moves[i];
            moves[i] = moves[index];
            moves[index] = tmp;
        }
    }

    private static int randomMove(Board board) {
        int[] moves = board.moves();
        int c = MoveGenerator.generateValidMoves(board, moves);
        return moves[random.nextInt(c)];
    }
}
