package com.fmotech.chess;

import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.fmotech.chess.DebugUtils.b;
import static com.fmotech.chess.DebugUtils.w;
import static com.fmotech.chess.FenFormatter.fromFen;
import static org.apache.commons.lang3.StringUtils.*;

@SuppressWarnings("unused")
public class UciProtocol {

    public static final int[] PROMOS = createPromos();
    private static Board board;

    private static int[] createPromos() {
        int[] data = new int[256];
        data['q'] = data['Q'] = Board.QUEEN;
        data['r'] = data['R'] = Board.ROCK;
        data['b'] = data['B'] = Board.BISHOP;
        data['n'] = data['N'] = Board.KNIGHT;
        return data;
    }

    private static Map<String, Method> commands;

    public static void main(String[] args) throws InvocationTargetException, IllegalAccessException {
        commands = Arrays.stream(UciProtocol.class.getMethods())
                .filter(e -> Modifier.isStatic(e.getModifiers()))
                .filter(e -> e.getParameterTypes().length == 1 && e.getParameterTypes()[0] == String.class)
                .collect(Collectors.toMap(e -> StringUtils.lowerCase(e.getName()), Function.identity()));
        Method noOp = commands.get("noop");

        System.out.println("FmoChess v0.1");
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNext()) {
            String input = scanner.nextLine();
            String command = lowerCase(substringBefore(input, " "));
            String parameters = substringAfter(input, " ");
            invoke(noOp, command, parameters);
        }
    }

    private static void invoke(Method noOp, String command, String parameters) {
        try {
            commands.getOrDefault(command, noOp).invoke(null, parameters);
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }

    public static void uci(String parameter) {
        send("id name FmoChess 0.1");
        send("id author Francisco Modesto");
        send("id rnd " + UUID.randomUUID().toString());
        send("uciok");
    }

    public static void isReady(String parameter) {
        send("readyok");
    }

    public static void position(String parameter) {
        String initial = defaultString(substringBefore(parameter, "moves"), parameter);
        String moves = substringAfter(parameter, "moves");
        board = initial.startsWith("fen") ? fromFen(substringAfter(initial, "fen")) : Board.INIT;
        for (String move : split(moves, " ")) {
            boolean turn = board.whiteTurn();
            long src = mask(turn, substring(move, 0, 2));
            long tgt = mask(turn, substring(move, 2, 4));
            if (move.length() == 4) {
                board = board.move(src, tgt).nextTurn();
            } else {
                board = board.move(src, tgt, PROMOS[move.charAt(4)]).nextTurn();
            }
        }
        send(moves);
    }

    public static void go(String parameter) {
        long[] moves = board.moves();
        int c = MoveGenerator.generateValidMoves(board, moves);

        send("bestmove a1a8");
    }

    private static long mask(boolean turn, String move) {
        return turn ? w(move) : b(move);
    }

    public static void quit(String parameter) {
        System.exit(0);
    }

    public static void noop(String parameter) {
    }

    private static void send(String response) {
        System.out.println(response);
    }
}
