package com.fmotech.chess.game;

import com.fmotech.chess.Board;
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
import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.apache.commons.lang3.StringUtils.lowerCase;
import static org.apache.commons.lang3.StringUtils.split;
import static org.apache.commons.lang3.StringUtils.substringAfter;
import static org.apache.commons.lang3.StringUtils.substringBefore;
import static org.apache.commons.lang3.StringUtils.substringBetween;
import static org.apache.commons.lang3.StringUtils.trim;

@SuppressWarnings("unused")
public class UciProtocol {

    private static final String ID = UUID.randomUUID().toString();

    private static Map<String, Method> commands;
    private static PrintStream logs;
    private static Game game = new Game();

    public static void execute() {
        commands = Arrays.stream(UciProtocol.class.getMethods())
                .filter(e -> Modifier.isStatic(e.getModifiers()))
                .filter(e -> e.getParameterTypes().length == 1 && e.getParameterTypes()[0] == String.class)
                .collect(Collectors.toMap(e -> StringUtils.lowerCase(e.getName()), Function.identity()));
        Method noOp = commands.get("noop");
        init();

        uci("");
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
        send("id name Chessy 0.1");
        send("id author Francisco Modesto");
        send("id rnd " + ID);
        send("uciok");
    }

    public static void ucinewgame(String parameter) {
        game.resetAI();
    }

    public static void isReady(String parameter) {
        send("readyok");
    }

    public static void position(String parameter) {
        String initial = defaultString(substringBefore(parameter, "moves"), parameter);
        String moves = substringAfter(parameter, "moves");
        game.resetBoard(initial.startsWith("fen") ? fromFen(substringAfter(initial, "fen")) : Board.INIT);
        for (String fenMove : split(moves, " ")) {
            game.move(fenMove);
        }
    }

    public static void go(String parameter) {
        int depth = parse(parameter, "depth", 64);
        int movesToGo = parse(parameter, "movestogo", 30);
        int moveTime = parse(parameter, "movetime", 0);
        int time = game.whiteTurn() ? parse(parameter, "wtime", 0) : parse(parameter, "btime", 0);
        int inc = game.whiteTurn() ? parse(parameter, "winc", 0) : parse(parameter, "binc", 0);

        if (moveTime > 0) {
            time = moveTime;
            movesToGo = 1;
        }

        if (time > 0) {
            time = Math.max(1, time / movesToGo - 50 + inc);
        }
        System.out.println("Time to use: " + time);
        send("bestmove " + game.thinkMove(time, depth));
    }

    private static int parse(String parameter, String name, int defaultValue) {
        if (!parameter.contains(name + " "))
            return defaultValue;
        return Integer.parseInt(trim(defaultString(substringBetween(parameter, name + " ", " "), substringAfter(parameter, name + " "))));
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
