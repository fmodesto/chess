package com.fmotech.chess.utils;

import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

import static org.apache.commons.lang3.StringUtils.substringAfter;
import static org.apache.commons.lang3.StringUtils.substringBefore;

public class Vice {

    public static void main(String[] args) throws Exception {
        Files.lines(Paths.get("src/test/resources/wacnew.epd"))
                .map(e -> substringBefore(e, " "))
                .map(Vice::eval)
                .forEach(System.out::println);
    }

    private static String eval(String fen) {
        try {
            Process engine = Runtime.getRuntime().exec(new String[]{"/Users/fran/Projects/vice/Ch81/vice"});
            Scanner engineIn = new Scanner(engine.getInputStream());
            PrintWriter engineOut = new PrintWriter(engine.getOutputStream());
            send(engineOut, "vice");
            send(engineOut, "setboard " + fen + " w - - 0 1");
            send(engineOut, "eval");
            String value = expect(engineIn, "Eval:");
            send(engineOut, "quit");
            return "assertEquals(" + value + ", eval(\"" + fen + "\"));";
        } catch (Exception e) {
            return "Error";
        }
    }

    private static String expect(Scanner receive, String text) {
        while (true) {
            String line = receive.nextLine();
            if (line.startsWith(text))
                return substringAfter(line, text);
        }
    }

    private static void send(PrintWriter send, String command) {
        send.println(command);
        send.flush();
    }
}
