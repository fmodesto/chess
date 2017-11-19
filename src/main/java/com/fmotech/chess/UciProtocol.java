package com.fmotech.chess;

import org.apache.commons.lang3.StringUtils;

import java.io.PrintStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.fmotech.chess.FenFormatter.fromFen;
import static com.fmotech.chess.FenFormatter.moveFromFen;
import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.apache.commons.lang3.StringUtils.lowerCase;
import static org.apache.commons.lang3.StringUtils.split;
import static org.apache.commons.lang3.StringUtils.substringAfter;
import static org.apache.commons.lang3.StringUtils.substringBefore;

@SuppressWarnings("unused")
public class UciProtocol {

    private static final String ID = UUID.randomUUID().toString();

    private static Map<String, Method> commands;
    private static PrintStream logs;
    private static Board board;

    public static void main(String[] args) {
        commands = Arrays.stream(UciProtocol.class.getMethods())
                .filter(e -> Modifier.isStatic(e.getModifiers()))
                .filter(e -> e.getParameterTypes().length == 1 && e.getParameterTypes()[0] == String.class)
                .collect(Collectors.toMap(e -> StringUtils.lowerCase(e.getName()), Function.identity()));
        Method noOp = commands.get("noop");
        init();

        send("FmoChess v0.1");
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNext()) {
            String input = scanner.nextLine();
            logs.println(">> " + input);
            String command = lowerCase(substringBefore(input, " "));
            String parameters = substringAfter(input, " ");
            invoke(noOp, command, parameters);
        }
    }

    private static void init() {
        try {
            logs = new PrintStream(Files.newOutputStream(Paths.get("fmoChess-" + ID + ".log")));
        } catch (Exception e) {
            logs = System.err;
        }
    }

    private static void invoke(Method noOp, String command, String parameters) {
        try {
            long now = System.currentTimeMillis();
            commands.getOrDefault(command, noOp).invoke(null, parameters);
            logs.println("processing time: " + (System.currentTimeMillis() - now));
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }

    public static void uci(String parameter) {
        send("id name FmoChess 0.1");
        send("id author Francisco Modesto");
        send("id rnd " + ID);
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
    }

    public static void go(String parameter) {
        send("bestmove " + FenFormatter.moveToFen(board, AI.bestMove(board)));
    }

    public static void quit(String parameter) {
        logs.close();
        System.exit(0);
    }

    public static void noop(String parameter) {
    }

    private static void send(String response) {
        System.out.println(response);
        logs.println("<< " + response);
    }
}
