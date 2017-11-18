package com.fmotech.chess.utils;

import com.fmotech.chess.Board;
import com.fmotech.chess.DebugUtils;
import com.fmotech.chess.MoveGenerator;
import com.fmotech.chess.utils.PgnFormatter.Game;
import com.fmotech.chess.utils.PgnFormatter.GameResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static com.fmotech.chess.DebugUtils.*;
import static java.lang.Integer.parseInt;
import static org.apache.commons.lang3.StringUtils.*;

public class RecordedGamePlay {

    private static final long FILE_1 = computeLeft();

    public static void main(String[] args) throws IOException {
        Files.lines(Paths.get("src/test/resources/games.txt"))
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
            System.out.println(e.getMessage());
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
        boolean mate = rawMove.contains("#");
        boolean promotion = rawMove.contains("=");
        String move = remove(remove(remove(remove(rawMove, 'x'), '+'), '#'), '=');
        if (Character.isLowerCase(move.charAt(0))) move = "P" + move;
        char type = move.charAt(0);
        char promo = move.charAt(move.length() - 1);
        if (promotion) move = substring(move, 0, -1);
        long src = move(whiteTurn, substring(move, 1, -2));
        long tgt = move(whiteTurn, substring(move, -2));
        int[] moves = board.moves();
        int counter = MoveGenerator.generateValidMoves(board, moves);
        int m;
        if (whiteTurn && "O-O".equals(move) || !whiteTurn && "O-O-O".equals(move)) {
            m = findMove(board.ownKing(), board.ownKing() >>> 2, counter, moves);
        } else if (whiteTurn && "O-O-O".equals(move) || !whiteTurn && "O-O".equals(move)) {
            m = findMove(board.ownKing(), board.ownKing() << 2, counter, moves);
        } else if (type == 'K') {
            m = findMove(src & board.ownKing(), tgt, counter, moves);
        } else if (type == 'Q') {
            m = findMove(src & board.ownQueens(), tgt, counter, moves);
        } else if (type == 'B') {
            m = findMove( src & board.ownBishops(), tgt, counter, moves);
        } else if (type == 'N') {
            m = findMove( src & board.ownKnights(), tgt, counter, moves);
        } else if (type == 'R') {
            m = findMove( src & board.ownRocks(), tgt, counter, moves);
        } else if (type == 'P' && promotion) {
            m = findMove( src & board.ownPawns(), tgt, promo, counter, moves);
        } else if (type == 'P') {
            m = findMove( src & board.ownPawns(), tgt, counter, moves);
        } else {
            m = 0; // Pass?
        }
        return board.move(m).nextTurn();
    }

    private static long move(boolean whiteTurn, String move) {
        if (move.length() == 0) {
            return -1;
        }
        return whiteTurn ? createMask(move) : Long.reverse(createMask(move));
    }

    private static long createMask(String move) {
        if (move.length() == 1 && Character.isDigit(move.charAt(0))) {
            return 0xFFL << (8L * (move.charAt(0) - '1'));
        } else if (move.length() == 1) {
            return FILE_1 >>> (move.charAt(0) - 'a');
        } else {
            return 1L << ((7 - (move.charAt(0) - 'a')) + 8L * (move.charAt(1) - '1'));
        }
    }

    private static int findMove(long srcMask, long tgtMask, char promo, int counter, int[] moves) {
        int move = findMove(srcMask, tgtMask, counter, moves) & 0xF8FFFFFF;
        switch (promo) {
            case 'R':
                return move | Board.ROCK << 24;
            case 'B':
                return move | Board.BISHOP << 24;
            case 'N':
                return move | Board.KNIGHT << 24;
            default:
                return move | Board.QUEEN << 24;
        }
    }

    private static int findMove(long srcMask, long tgtMask, int counter, int[] moves) {
        for (int i = 0; i < counter; i++) {
            long src = 1L << (moves[i] & 0xFF);
            long tgt = 1L << ((moves[i] >> 8) & 0xFF);
            if ((src & srcMask) != 0 && (tgt & tgtMask) != 0) {
                return moves[i];
            }
        }
        throw new IllegalStateException("Move not valid");
    }

    private static long computeLeft() {
        long left = 0;
        for (long i = 0; i < 8; i++) {
            left |= 1L << ((i * 8) + 7);
        }
        return left;
    }
}
