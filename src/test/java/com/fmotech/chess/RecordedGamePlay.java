package com.fmotech.chess;

import com.fmotech.chess.utils.PgnFormatter.Game;
import com.fmotech.chess.utils.PgnFormatter.GameResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static com.fmotech.chess.DebugUtils.*;
import static java.lang.Integer.parseInt;
import static org.apache.commons.lang3.StringUtils.*;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class RecordedGamePlay {

    private static final long FILE_1 = computeLeft();

    public static void main(String[] args) throws IOException {
        Files.lines(Paths.get("src/test/resources/all.txt"))
//                .limit(10)
                .map(e -> split(e, "\t"))
                .map(e -> new Game(GameResult.valueOf(e[0]), parseInt(e[1]), parseInt(e[2]), split(e[3], " ")))
                .forEach(RecordedGamePlay::play);
    }

    private static void play(Game game) {
        try {
            Board board = Board.INIT;
            for (String move : game.moves) {
                board = move(board, move);
            }
        } catch (Exception e) {
            for (int i = 0; i < game.moves.length; i++) {
                if (i % 2 == 0) {
                    System.out.print((1+(i/2)) + ". ");
                }
                System.out.print(game.moves[i] + " ");
            }
            System.out.println();
        }
    }

    private static void play2(Game game) {
        Board board = Board.INIT;
            DebugUtils.debug(FEN, board);
        for (String move : game.moves) {
            try {

                System.out.println(move);
                board = move(board, move);
                DebugUtils.debug(FEN, board);
            } catch (Exception e) {
                System.out.println(move);
                System.out.println(game);
                for (int i = 0; i < game.moves.length; i++) {
                    if (i % 2 == 0) {
                        System.out.print((1+(i/2)) + ". ");
                    }
                    System.out.print(game.moves[i] + " ");
                }
                System.out.println();
                System.exit(0);
            }
        }
    }
//8/8/8/8/8/5K2/4p2R/5k2 b - - 0 1 dfefffffffffffff 2080200000000000 80000000000000 2010200000000000
    private static Board move(Board board, String rawMove) {
        boolean whiteTurn = board.whiteTurn();
        boolean capture = rawMove.contains("x");
        boolean check = rawMove.contains("+");
        boolean mater = rawMove.contains("#");
        boolean promotion = rawMove.contains("=");
        String move = remove(remove(remove(remove(rawMove, 'x'), '+'), '#'), '=');
        if (Character.isLowerCase(move.charAt(0))) move = "P" + move;
        char type = move.charAt(0);
        char promo = move.charAt(move.length() - 1);
        if (promotion) move = substring(move, 0, -1);
        long src = move(whiteTurn, substring(move, 1, -2));
        long tgt = move(whiteTurn, substring(move, -2));
        long[] moves = board.moves();
        int counter = MoveGenerator.generateValidMoves(board, moves);
        long[] m = new long[] { 0, 0 };
        if (whiteTurn && "O-O".equals(move) || !whiteTurn && "O-O-O".equals(move)) {
            fillMove(m, board.ownKing(), board.ownKing() >>> 2, counter, moves, whiteTurn);
        } else if (whiteTurn && "O-O-O".equals(move) || !whiteTurn && "O-O".equals(move)) {
            fillMove(m, board.ownKing(), board.ownKing() << 2, counter, moves, whiteTurn);
        } else if (type == 'K') {
            fillMove(m, src & board.ownKing(), tgt, counter, moves, whiteTurn);
        } else if (type == 'Q') {
            fillMove(m, src & board.ownQueens(), tgt, counter, moves, whiteTurn);
        } else if (type == 'B') {
            fillMove(m, src & board.ownBishops(), tgt, counter, moves, whiteTurn);
        } else if (type == 'N') {
            fillMove(m, src & board.ownKnights(), tgt, counter, moves, whiteTurn);
        } else if (type == 'R') {
            fillMove(m, src & board.ownRocks(), tgt, counter, moves, whiteTurn);
        } else if (type == 'P') {
            fillMove(m, src & board.ownPawns(), tgt, counter, moves, whiteTurn);
        }
        if (promotion && promo == 'Q') {
            return board.move(m[0], m[1], Board.QUEEN).nextTurn();
        } else if (promotion && promo == 'R') {
            return board.move(m[0], m[1], Board.ROCK).nextTurn();
        } else if (promotion && promo == 'B') {
            return board.move(m[0], m[1], Board.BISHOP).nextTurn();
        } else if (promotion && promo == 'N') {
            return board.move(m[0], m[1], Board.KNIGHT).nextTurn();
        } else {
            return board.move(m[0], m[1]).nextTurn();
        }
    }

    private static long move(boolean whiteTurn, String move) {
        if (move.length() == 0) {
            return -1;
        } else if (move.length() == 1 && Character.isDigit(move.charAt(0))) {
            long mask = 0xFFL << (8L * (move.charAt(0) - '1'));
            return whiteTurn ? mask : Long.reverse(mask);
        } else if (move.length() == 1) {
            long mask = FILE_1 >>> (move.charAt(0) - 'a');
            return whiteTurn ? mask : Long.reverse(mask);
        } else {
            return whiteTurn ? w(move) : b(move);
        }
    }

    private static void fillMove(long[] result, long srcMask, long tgtMask, int counter, long[] moves, boolean whiteTurn) {
        for (int i = 0; i < counter; i += 2) {
            if ((moves[i] & srcMask) != 0 && (moves[i + 1] & tgtMask) != 0) {
                long src = moves[i] & srcMask;
                long tgt = moves[i + 1] & tgtMask;
                if (BitOperations.nextLowestBit(src) == 0 && BitOperations.nextLowestBit(tgt) == 0) {
                    result[0] = src;
                    result[1] = tgt;
                    return;
                }
            }
        }
        throw new IllegalStateException("Move not valid");
    }

    private static void assertContainsMove(boolean whiteTurn, long src, long tgt, long[] moves, int counter) {
        fail("Move not found: " + DebugUtils.toPosition(whiteTurn, src) + DebugUtils.toPosition(whiteTurn, tgt));
    }

    private static long computeLeft() {
        long left = 0;
        for (int i = 0; i < 8; i++) {
            left |= 1L << ((8 * i) + 7);
        }
        return left;
    }
}
