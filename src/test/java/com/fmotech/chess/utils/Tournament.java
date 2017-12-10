package com.fmotech.chess.utils;

import com.fmotech.chess.AI;
import com.fmotech.chess.Game;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.List;
import java.util.Scanner;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.split;
import static org.apache.commons.lang3.StringUtils.substringAfter;
import static org.apache.commons.lang3.StringUtils.substringBefore;
import static org.apache.commons.lang3.StringUtils.trim;

public class Tournament {

    public static void main(String[] args) throws Exception {
        AI.SILENT = true;
        for (int i = 1; i <= 5; i++) {
            int time = i;
            loadGame().forEach(e -> {
                try {
                    playVice(true, time, e.left, e.right);
                    playVice(false, time, e.left, e.right);
                    playTscp(true, time, e.left, e.right);
                    playTscp(false, time, e.left, e.right);
                } catch (Exception ex) {}
            });
            }
        }

    private static List<ImmutablePair<String, String[]>> loadGame() throws Exception {
        return new BufferedReader(new InputStreamReader(Tournament.class.getResourceAsStream("/silversuite.pgn")))
                .lines()
                .map(e -> ImmutablePair.of(trim(substringBefore(e, "@")),
                        split(trim(substringAfter(e, "@")), " ")))
                .collect(toList());
    }

    public static int playVice(boolean whiteTurn, int time, String opening, String[] moves) throws Exception {
        Game game = new Game();
        Process engine = Runtime.getRuntime().exec(new String[] { "/Users/fran/Projects/vice/Ch81/vice" });
        Scanner engineIn = new Scanner(engine.getInputStream());
        PrintWriter engineOut = new PrintWriter(engine.getOutputStream());
        send(engineOut, "uci");
        expect(engineIn, "uciok");
        send(engineOut, "isready");
        expect(engineIn, "readyok");
        send(engineOut, "ucinewgame");
        System.out.println("[Opening \"" + opening + "\"]");
        System.out.println("[Movetime \"" + time + "\"]");
        if (!whiteTurn) {
            System.out.println("[White \"vice\"]");
            System.out.println("[Black \"chessy\"]");
        } else {
            System.out.println("[White \"chessy\"]");
            System.out.println("[Black \"vice\"]");
        }

        for (String move : moves)
            game.move(move);

        if (game.whiteTurn() && !whiteTurn) {
            send(engineOut, game.uci());
            send(engineOut, "go movetime " + 1000 * time);
            String move = expect(engineIn, "bestmove");
            game.move(move);
        }

        while (!game.isGameOver()) {
            String move = game.thinkMove(1000 * time, 32);
            game.move(move);
            if (game.isGameOver())
                continue;
            send(engineOut, game.uci());
            send(engineOut, "go movetime " + 1000 * time);
            move = expect(engineIn, "bestmove");
            game.move(move);
        }
        send(engineOut, "quit");
        swallow(engineIn);
        String result = game.result();
        System.out.println(result);
        System.out.println();
        return result.startsWith("1/2-1/2") ? 0 : whiteTurn && result.startsWith("1-0") || !whiteTurn && result.startsWith("0-1") ? 1 : -1;
    }

    public static int playTscp(boolean whiteTurn, int time, String opening, String[] moves) throws Exception {
        Game game = new Game();
        Process engine = Runtime.getRuntime().exec(new String[] { "tscp" });
        Scanner engineIn = new Scanner(engine.getInputStream());
        PrintWriter engineOut = new PrintWriter(engine.getOutputStream());
        send(engineOut, "xboard");
        send(engineOut, "new");
        send(engineOut, "st " + time);
        System.out.println("[Opening \"" + opening + "\"]");
        System.out.println("[Movetime \"" + time + "\"]");
        if (!whiteTurn) {
            System.out.println("[White \"tscp\"]");
            System.out.println("[Black \"chessy\"]");
        } else {
            System.out.println("[White \"chessy\"]");
            System.out.println("[Black \"tscp\"]");
        }

        send(engineOut, "force");
        for (String move : moves) {
            send(engineOut, move);
            game.move(move);
        }

        if (game.whiteTurn() && whiteTurn) {
            String move = game.thinkMove(1000 * time, 32);
            game.move(move);
            send(engineOut, move);
        }
        send(engineOut, "go");
        game.move(expect(engineIn, "move"));

        while (!game.isGameOver()) {
            String move = game.thinkMove(1000 * time, 32);
            game.move(move);
            if (game.isGameOver())
                continue;
            send(engineOut, move);
            move = expect(engineIn, "move");
            game.move(move);
        }
        System.out.println();
        send(engineOut, "quit");
        swallow(engineIn);
        String result = game.result();
        System.out.println(result);
        System.out.println();
        return result.startsWith("1/2-1/2") ? 0 : whiteTurn && result.startsWith("1-0") || !whiteTurn && result.startsWith("0-1") ? 1 : -1;
    }

    private static String expect(Scanner receive, String move) {
        while (true) {
            String line = receive.nextLine();
//            if (line.contains("move") || line.contains("info"))
//                System.out.println("oponent: " + line);
            if (line.startsWith(move))
                return trim(substringAfter(line, move));
        }
    }

    private static void send(PrintWriter send, String command) {
//        System.out.println("chessy: " + command);
        send.println(command);
        send.flush();
    }

    private static void swallow(Scanner receive) {
        while (receive.hasNextLine()) {
            String line = receive.nextLine();
//            System.out.println(line);
        }
    }
}
