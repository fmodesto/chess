package com.fmotech.chess;

import java.util.Arrays;

import static com.fmotech.chess.BitOperations.lowestBitPosition;
import static com.fmotech.chess.BitOperations.nextLowestBit;
import static com.fmotech.chess.Board.PAWN;
import static com.fmotech.chess.Board.ROOK;
import static org.apache.commons.lang3.StringUtils.substring;

public class FenFormatter {

    private static final int[] FROM_PROMO = createFromPromos();
    private static final String[] TO_TILE_WHITE = createToTilesWhite();
    private static final String[] TO_TILE_BLACK = createToTilesBlack();
    private static final String[] TO_PROMO = createToPromos();

    public static Board fromFen(String fen) {
        int index = 0;
        while (Character.isSpaceChar(fen.charAt(index))) index++;
        long b = 1L << 63;
        long pawns = 0, rooks = 0, knights = 0, bishops = 0, queens = 0, kings = 0, color = 0, castle = 0, enPassant = 0;
        while (!Character.isSpaceChar(fen.charAt(index))) {
            char c = fen.charAt(index++);
            if (Character.isDigit(c)) {
                b >>>= c - '0';
            } else if (c != '/') {
                char p = Character.toLowerCase(c);
                if (Character.isLowerCase(c)) color |= b;
                if (p == 'p') pawns |= b;
                else if (p == 'r') rooks |= b;
                else if (p == 'n') knights |= b;
                else if (p == 'b') bishops |= b;
                else if (p == 'q') queens |= b;
                else if (p == 'k') kings |= b;
                b >>>= 1;
            }
        }
        while (Character.isSpaceChar(fen.charAt(index))) index++;
        boolean whiteTurn = 'w' == fen.charAt(index++);
        while (Character.isSpaceChar(fen.charAt(index))) index++;
        while (!Character.isSpaceChar(fen.charAt(index))) {
            char c = fen.charAt(index++);
            if (c == 'K' && (kings & 1L << 3) != 0) castle |= 1L & rooks;
            if (c == 'Q' && (kings & 1L << 3) != 0) castle |= 1L << 7 & rooks;
            if (c == 'k' && (kings & 1L << 59) != 0) castle |= 1L << 56 & rooks;
            if (c == 'q' && (kings & 1L << 59) != 0) castle |= 1L << 63 & rooks;
        }
        while (Character.isSpaceChar(fen.charAt(index))) index++;
        char c = fen.charAt(index++);
        if (c != '-') {
            char d = fen.charAt(index++);
            int p = 8 * (d - '1') + (7 - (Character.toLowerCase(c) - 'a'));
            enPassant |= (1L << p) & (whiteTurn ? pawns << 8 : pawns >>> 8);
            if (whiteTurn) color |= enPassant;
        }
        int ply = 0;
        int fifty = 0;
        if (fen.length() > index) {
            while (Character.isSpaceChar(fen.charAt(index))) index++;
            int x = 0;
            while (fen.length() > index && Character.isDigit(fen.charAt(index))) {
                x *= 10;
                x += fen.charAt(index++) - '0';
            }
            while (Character.isSpaceChar(fen.charAt(index))) index++;
            int y = 0;
            while (fen.length() > index && Character.isDigit(fen.charAt(index))) {
                y *= 10;
                y += fen.charAt(index++) - '0';
            }
            ply = 2 * (y - 1);
            fifty = ply - x + (whiteTurn ? 0 : 1);
        }
        Board board = Board.of(ply, fifty, color, pawns, rooks, knights, bishops, queens, kings, enPassant, castle);
        return whiteTurn ? board : board.nextTurn();
    }

    public static String toFen(Board board) {
        String moves = " " + board.fifty() + " " + board.fullMove();

        boolean whiteTurn = board.whiteTurn();
        board = whiteTurn ? board : board.nextTurn();

        char[] array = new char[64];
        fill(array, board.ownPawns(), 'P');
        fill(array, board.ownRooks(), 'R');
        fill(array, board.ownKnights(), 'N');
        fill(array, board.ownBishops(), 'B');
        fill(array, board.ownQueens(), 'Q');
        fill(array, board.ownKing(), 'K');
        fill(array, board.enemyPawns(), 'p');
        fill(array, board.enemyRooks(), 'r');
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

        return sb.toString() + moves;
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

    public static String moveToFen(Board board, int move) {
        try {
            int src = move & 0xFF;
            int tgt = (move >>> 8) & 0xFF;
            int promo = (move >>> 24) & 0x07;
            if (board.whiteTurn())
                return TO_TILE_WHITE[src] + TO_TILE_WHITE[tgt] + TO_PROMO[promo];
            else
                return TO_TILE_BLACK[src] + TO_TILE_BLACK[tgt] + TO_PROMO[promo];
        } catch (Exception e) {
            System.out.println(board);
            System.out.println(move);
            throw e;
        }
    }

    public static int moveFromFen(Board board, String move) {
        int src = tile(board.whiteTurn(), substring(move, 0, 2));
        int tgt = tile(board.whiteTurn(), substring(move, 2, 4));
        int promotion = move.length() == 5 ? FROM_PROMO[move.charAt(4)] : 0;
        int srcType = board.type(src, ROOK, 0);
        int tgtType = board.type(tgt, ROOK, srcType == PAWN ? PAWN : 0);
        return Move.create(src, tgt, srcType, tgtType, promotion);
    }

    private static int tile(boolean whiteTurn, String tile) {
        if (whiteTurn)
            return 8 * (tile.charAt(1) - '1') + (7 - (tile.charAt(0) - 'a'));
        else
            return 8 * (7 - (tile.charAt(1) - '1')) + (7 - (tile.charAt(0) - 'a'));
    }

    private static int[] createFromPromos() {
        int[] table = new int[256];
        table['q'] = table['Q'] = Board.QUEEN;
        table['r'] = table['R'] = Board.ROOK;
        table['b'] = table['B'] = Board.BISHOP;
        table['n'] = table['N'] = Board.KNIGHT;
        return table;
    }

    private static String[] createToTilesWhite() {
        String[] table = new String[64];
        for (int i = 0; i < 64; i++) {
            table[i] = "" + (char) ((7 - i % 8) + 'a') + (char) ((i / 8) + '1');
        }
        return table;
    }

    private static String[] createToTilesBlack() {
        String[] table = new String[64];
        for (int i = 0; i < 64; i++) {
            table[i] = "" + (char) ((7 - i % 8) + 'a') + (char) ((7 - i / 8) + '1');
        }
        return table;
    }

    private static String[] createToPromos() {
        String[] table = new String[8];
        Arrays.fill(table, "");
        table[Board.QUEEN] = "q";
        table[Board.ROOK] = "r";
        table[Board.BISHOP] = "b";
        table[Board.KNIGHT] = "n";
        return table;
    }
}
