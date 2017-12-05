package com.fmotech.chess.utils;

import com.fmotech.chess.AI;
import com.fmotech.chess.Game;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Scanner;

import static org.apache.commons.lang3.StringUtils.substringAfter;
import static org.apache.commons.lang3.StringUtils.trim;

public class Tournament {

    public static void main(String[] args) throws Exception {
        AI.SILENT = true;
        int[] scores = new int[6];
        for (int j = 1; j <= 10; j++) {
            for (int i = 1; i <= 20; i++) {
                int result = playTscp(true, j);
                scores[result + 1]++;
                result = playTscp(false, j);
                scores[result + 1]++;
                result = playVice(true, j);
                scores[result + 4]++;
                result = playVice(false, j);
                scores[result + 4]++;
            }
        }
        System.out.println(Arrays.toString(scores));
    }

    public static int playVice(boolean whiteTurn, int time) throws Exception {
        Game game = new Game();
        Process engine = Runtime.getRuntime().exec(new String[] { "/Users/fran/Projects/vice/Ch81/vice" });
        Scanner engineIn = new Scanner(engine.getInputStream());
        PrintWriter engineOut = new PrintWriter(engine.getOutputStream());
        send(engineOut, "uci");
        expect(engineIn, "uciok");
        send(engineOut, "isready");
        expect(engineIn, "readyok");
        send(engineOut, "ucinewgame");
        System.out.println("[Movetime \"" + time + "\"]");
        if (!whiteTurn) {
            System.out.println("[White \"vice\"]");
            System.out.println("[Black \"chessy\"]");
            send(engineOut, game.uci());
            send(engineOut, "go movetime " + 1000 * time);
            String move = expect(engineIn, "bestmove");
            game.move(move);
        } else {
            System.out.println("[White \"chessy\"]");
            System.out.println("[Black \"vice\"]");
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

    public static int playTscp(boolean whiteTurn, int time) throws Exception {
        Game game = new Game();
        Process engine = Runtime.getRuntime().exec(new String[] { "tscp" });
        Scanner engineIn = new Scanner(engine.getInputStream());
        PrintWriter engineOut = new PrintWriter(engine.getOutputStream());
        send(engineOut, "xboard");
        send(engineOut, "new");
        send(engineOut, "st " + time);
        System.out.println("[Movetime \"" + time + "\"]");
        if (!whiteTurn) {
            System.out.println("[White \"tscp\"]");
            System.out.println("[Black \"chessy\"]");
            send(engineOut, "go");
            String move = expect(engineIn, "move");
            game.move(move);
        } else {
            System.out.println("[White \"chessy\"]");
            System.out.println("[Black \"tscp\"]");
        }

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
