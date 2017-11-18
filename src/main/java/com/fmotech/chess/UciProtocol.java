package com.fmotech.chess;

import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.fmotech.chess.FenFormatter.fromFen;
import static com.fmotech.chess.FenFormatter.moveFromFen;
import static com.fmotech.chess.FenFormatter.moveToFen;
import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.apache.commons.lang3.StringUtils.lowerCase;
import static org.apache.commons.lang3.StringUtils.split;
import static org.apache.commons.lang3.StringUtils.substringAfter;
import static org.apache.commons.lang3.StringUtils.substringBefore;

@SuppressWarnings("unused")
public class UciProtocol {

    private static Board board;
    private static Random random = new Random();

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
            board = board.move(moveFromFen(board, move)).nextTurn();
        }
        send(moves);
    }

    public static void go(String parameter) {
        int[] moves = board.moves();
        int c = MoveGenerator.generateValidMoves(board, moves);
        send("bestmove " + moveToFen(board, moves[random.nextInt(c)]));
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
