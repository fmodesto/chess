package com.fmotech.chess;

import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static java.time.LocalDateTime.now;
import static org.apache.commons.lang3.StringUtils.leftPad;

@SuppressWarnings("unused")
public class DebugUtils {

    public static final String CHESS = "♙♖♘♗♕♔♟♜♞♝♛♚";
    public static final String FEN = "PRNBQKprnbqk";

    public static Board createWhiteBoard(String r8, String r7, String r6, String r5, String r4, String r3, String r2, String r1) {
        return createBoardFor(true, r8, r7, r6, r5, r4, r3, r2, r1);
    }

    public static Board createBlackBoard(String r8, String r7, String r6, String r5, String r4, String r3, String r2, String r1) {
        return createBoardFor(false, r8, r7, r6, r5, r4, r3, r2, r1);
    }

    private static Board createBoardFor(boolean whiteTurn, String r8, String r7, String r6, String r5, String r4, String r3, String r2, String r1) {
        String raw = r8 + " " + r7 + " " + r6 + " " + r5 + " " + r4 + " " + r3 + " " + r2 + " " + r1;
        long pawns = 0, rooks = 0, knights = 0, bishops = 0, queens = 0, kings = 0, color = 0, enPassant = 0, castle = 0;

        int idx = 0;
        for (int i = raw.length() - 1; i >= 0; i -= 2) {
            char c = raw.charAt(i);
            long b = 1L << idx++;
            int p = FEN.indexOf(c);
            color |= p < 6 ? 0: b;
            p += p < 6 ? 0 : -6;
            if (p == 0) pawns |= b;
            else if (p == 1) rooks |= b;
            else if (p == 2) knights |= b;
            else if (p == 3) bishops |= b;
            else if (p == 4) queens |= b;
            else if (p == 5) kings |= b;
        }
        int s = 0;
        while ((s = StringUtils.indexOf(raw, "·", s)) != -1) {
            if (s == 7) castle |= 1L << 63;
            else if (s == 9) castle |= 1L << 56;
            else if (s == 119) castle |= 1L << 7;
            else if (s == 121) castle |= 1L;
            else enPassant |= 1L << (63 - (s / 2));
            s += 1;
        }
        if ((enPassant & 0x0000_FF00_0000_0000L) != 0) color |= enPassant;
        Board board = Board.of(0, 0, color, pawns, rooks, knights, bishops, queens, kings, enPassant, castle);
        return whiteTurn ? board : board.nextTurn();
    }

    public static void debugPerft(Board board, int level) {
        int[] moves = board.moves();
        int counter = MoveGenerator.generateValidMoves(board, moves);
        long total = 0;
        for (int i = 0; i < counter; i++) {
            long c = MoveGenerator.countMoves(level - 1, board.move(moves[i]).nextTurn());
            total += c;
            System.out.println(FenFormatter.moveToFen(board, moves[i]) + ": " + c);
        }
        System.out.println("Total: " + total);
    }

    public static void debug(String symbols, int type, long board) {
        debug(symbols, Board.of(0, 0, test(type, 0b100) ? board : 0, test(type, 0b010) ? board : 0, test(type, 0b001) ? board : 0));
    }

    public static void debugRev(String symbols, int type, long board) {
        debug(symbols, type, BitOperations.reverse(board));
    }

    private static boolean test(int type, int mask) {
        return (type & mask) != 0;
    }

    public static void debug(String symbols, Board board) {
        String[] ranks = drawBoard(symbols, board).split("\n");
        for (int i = 0; i < 8; i++) {
            System.out.println(8 - i + " |" + ranks[i]);
        }
        System.out.println("  +---------------");
        System.out.println("   a b c d e f g h");
    }

    private static String drawBoard(String symbols, Board board) {
        String[] fen = FenFormatter.toFen(board).split(" ");
        StringBuilder sb = new StringBuilder();
        for (char c : fen[0].toCharArray()) {
            if (Character.isDigit(c)) {
                int p = c - '0';
                while (p-- > 0) {
                    sb.append("  ");
                }
            } else if (c == '/') {
                sb.replace(sb.length() - 1, sb.length(),"\n");
            } else {
                sb.append(c).append(" ");
            }
        }
        for (char c : fen[2].toCharArray()) {
            if (c == 'K') sb.replace(121, 122, "·");
            else if (c == 'Q') sb.replace(119, 120, "·");
            else if (c == 'k') sb.replace(9, 10, "·");
            else if (c == 'q') sb.replace(7, 8, "·");
        }
        if (!"-".equals(fen[3])) {
            int p = 16 * (8 - (fen[3].charAt(1) - '0')) + 2 * (fen[3].charAt(0) - 'a');
            sb.replace(p, p + 1, "·");
        }
        return StringUtils.replaceChars(sb.toString(), FEN, symbols);
    }

    public static String toHexString(long l) {
        return leftPad(Long.toHexString(l), 16, '0');
    }

    public static String toHexString(int i) {
        return leftPad(Integer.toHexString(i), 8, '0');
    }

    public interface Thunk { void apply(); }
    public static long timeExecuting(Thunk thunk) {
        LocalDateTime start = now();
        thunk.apply();
        return ChronoUnit.MILLIS.between(start, now());
    }

    public static String debug(long... bitBoards) {
        StringBuilder sb = new StringBuilder();
        for (int i = 7; i >= 0; i--) {
            for (int k = 0; k < bitBoards.length; k++) {
                int rank = (int) (bitBoards[k] >>> (8 * i)) & 0xFF;
                for (int j = 1; j < 256; j *= 2) {
                    sb.append(((rank & j) != 0 ? "X " : "· "));
                }
                sb.append("   ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
