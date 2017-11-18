package com.fmotech.chess;

import static com.fmotech.chess.BitOperations.lowestBitPosition;
import static com.fmotech.chess.BitOperations.nextLowestBit;
import static org.apache.commons.lang3.StringUtils.normalizeSpace;

public class FenFormatter {

    public static Board fromFen(String fen) {
        String[] parts = normalizeSpace(fen).split("\\s+");
        boolean whiteTurn = "w".equals(parts[1]);
        long b = 1L << 63;
        long pawns = 0, rocks = 0, knights = 0, bishops = 0, queens = 0, kings = 0, color = 0, castle = 0, enPassant = 0;
        for (char c : parts[0].toCharArray()) {
            if (Character.isDigit(c)) {
                b >>>= c - '0';
            } else if (c != '/') {
                char p = Character.toLowerCase(c);
                if (Character.isLowerCase(c)) color |= b;
                if (p == 'p') pawns |= b;
                else if (p == 'r') rocks |= b;
                else if (p == 'n') knights |= b;
                else if (p == 'b') bishops |= b;
                else if (p == 'q') queens |= b;
                else if (p == 'k') kings |= b;
                b >>>= 1;
            }
        }
        for (char c : parts[2].toCharArray()) {
            if (c == 'K' && (kings & 1L << 3) != 0) castle |= 1L & rocks;
            if (c == 'Q' && (kings & 1L << 3) != 0) castle |= 1L << 7 & rocks;
            if (c == 'k' && (kings & 1L << 59) != 0) castle |= 1L << 56 & rocks;
            if (c == 'q' && (kings & 1L << 59) != 0) castle |= 1L << 63 & rocks;
        }
        if (!"-".equals(parts[3])) {
            int p = 8 * (parts[3].charAt(1) - '1') + (7 - (Character.toLowerCase(parts[3].charAt(0)) - 'a'));
            enPassant |= (1L << p) & (whiteTurn ? pawns << 8 : pawns >>> 8);
            if (whiteTurn) color |= enPassant;
        }
        Board board = Board.of(color, pawns, rocks, knights, bishops, queens, kings, enPassant, castle);
        return whiteTurn ? board : board.nextTurn();
    }

    public static String toFen(Board board) {
        boolean whiteTurn = board.whiteTurn();
        board = whiteTurn ? board : board.nextTurn();

        char[] array = new char[64];
        fill(array, board.ownPawns(), 'P');
        fill(array, board.ownRocks(), 'R');
        fill(array, board.ownKnights(), 'N');
        fill(array, board.ownBishops(), 'B');
        fill(array, board.ownQueens(), 'Q');
        fill(array, board.ownKing(), 'K');
        fill(array, board.enemyPawns(), 'p');
        fill(array, board.enemyRocks(), 'r');
        fill(array, board.enemyKnights(), 'n');
        fill(array, board.enemyBishops(), 'b');
        fill(array, board.enemyQueens(), 'q');
        fill(array, board.enemyKing(), 'k');

        int p = 0;
        StringBuilder sb = new StringBuilder(64);
        for (int i = 0; i < 64; i++) {
            if (i != 0 && i % 8 == 0) p = write(p, sb, '/');
            if (array[i] != 0) p = write(p, sb, array[i]);
            else p += 1;
        }
        write(p, sb, ' ');
        sb.append(whiteTurn ? "w" : "b");
        sb.append(" ");

        if (board.castle() != 0) {
            if ((board.castle() & 1L) != 0) sb.append("K");
            if ((board.castle() & 1L << 7) != 0) sb.append("Q");
            if ((board.castle() & 1L << 56) != 0) sb.append("k");
            if ((board.castle() & 1L << 63) != 0) sb.append("q");
        } else {
            sb.append("-");
        }
        sb.append(" ");

        if (board.enPassant() != 0) {
            int e = lowestBitPosition(board.enPassant());
            sb.append((char) ((7 - e % 8) + 'a')).append((char) ((e / 8) + '1'));
        } else {
            sb.append("-");
        }
        sb.append(" 0 1");
        return sb.toString();
    }

    private static void fill(char[] board, long pieces, char character) {
        while (pieces != 0) {
            board[63 - lowestBitPosition(pieces)] = character;
            pieces = nextLowestBit(pieces);
        }
    }

    private static int write(int p, StringBuilder sb, char c) {
        if (p != 0) sb.append(p);
        sb.append(c);
        return 0;
    }
}
