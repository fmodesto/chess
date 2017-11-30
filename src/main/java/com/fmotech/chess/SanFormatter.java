package com.fmotech.chess;

import static org.apache.commons.lang3.StringUtils.remove;
import static org.apache.commons.lang3.StringUtils.substring;

public class SanFormatter {

    private static final long FILE_1 = computeLeft();

    public static int moveFromSan(Board board, String rawMove) {
        String move = remove(remove(remove(remove(rawMove, 'x'), '+'), '#'), '=');
        boolean promotion = rawMove.contains("=");
        boolean whiteTurn = board.whiteTurn();
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
        return m;
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
