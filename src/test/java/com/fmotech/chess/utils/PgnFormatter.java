package com.fmotech.chess.utils;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.countMatches;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.normalizeSpace;
import static org.apache.commons.lang3.StringUtils.substringAfter;
import static org.apache.commons.lang3.StringUtils.substringBefore;
import static org.apache.commons.lang3.StringUtils.substringBetween;
import static org.apache.commons.lang3.StringUtils.trim;

public class PgnFormatter {

    public enum GameResult {
        WHITE,
        BLACK,
        DRAW,
        UNKNOWN
    }

    public static class Game {
        public int whiteRating;
        public int blackRating;
        public GameResult result;
        public String[] moves;

        public Game(GameResult result, int whiteRating, int blackRating, String[] moves) {
            this.whiteRating = whiteRating;
            this.blackRating = blackRating;
            this.result = result;
            this.moves = moves;
        }

        @Override
        public String toString() {
            return "Game{" +
                    "whiteRating=" + whiteRating +
                    ", blackRating=" + blackRating +
                    ", result=" + result +
                    ", moves=" + Arrays.toString(moves) +
                    '}';
        }
    }

    public static void main(String[] args) throws IOException {
        BufferedReader reader = Files.newBufferedReader(Paths.get("src/test/resources/data_kingbase.pgn"), StandardCharsets.ISO_8859_1);
        List<Game> games = new ArrayList<>(1 << 20);

        int whiteRating = 0;
        int blackRating = 0;
        String result = null;
        String game = "";
        String line;

        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get("zip.txt"))) {
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty() && !game.isEmpty()) {
                    Game e = createGame(result, whiteRating, blackRating, game);
                    if (e != null) {
                        writer.write(e.result + "\t" + e.whiteRating + "\t" + e.blackRating + "\t" + Arrays.stream(e.moves).collect(Collectors.joining(" ")) + "\n");
                    }
                    else System.err.println("Ignoring " + game);
                    game = "";
                } else if (line.startsWith("[WhiteElo \"")) {
                    whiteRating = parseInt(substringBetween(line, "\""));
                } else if (line.startsWith("[BlackElo \"")) {
                    blackRating = parseInt(substringBetween(line, "\""));
                } else if (line.startsWith("[Result \"")) {
                    result = substringBetween(line, "\"");
                } else if (!line.startsWith("[")) {
                    game += line + " ";
                }
            }
        }
    }

    private static int parseInt(String text) {
        try {
            return text.contains(":") ? Integer.parseInt(substringBefore(text, ":")) : Integer.parseInt(text);
        } catch (Exception e) {
            return -1;
        }
    }

    private static Game createGame(String result, int whiteRating, int blackRating, String game) {
        game = normalizeSpace(trim(game));
        GameResult res = parseGameResult(result);
        if (res == null || !game.endsWith(result) || isEmpty(game)) {
            return null;
        }
        String[] moves = splitMoves(substringBefore(game, result));
        if (moves == null || moves.length == 0) {
            return null;
        }
        return new Game(res, whiteRating, blackRating, moves);
    }

    private static GameResult parseGameResult(String result) {
        switch (result) {
            case "1-0":
                return GameResult.WHITE;
            case "0-1":
                return GameResult.BLACK;
            case "1/2-1/2":
                return GameResult.DRAW;
            case "*":
                return GameResult.UNKNOWN;
            default:
                return null;
        }
    }

    private static String[] splitMoves(String game) {
        Pattern move = Pattern.compile("(\\d+\\.)");
        String[] moves = StringUtils.split(move.matcher(game).replaceAll("\n$1 "), "\n");
        List<String> list = new ArrayList<>();
        int s = 1;
        for (int i = 1; i <= moves.length; i++) {
            if (!trim(normalizeSpace(moves[i - 1])).startsWith(i + ".")) {
                return null;
            }
            String m = trim(substringAfter(moves[i - 1], "."));
            if ((i != moves.length && countMatches(m, ' ') != 1) || countMatches(m, ' ') > 1) {
                return null;
            }
            if (m.indexOf(' ') != -1) {
                list.add(substringBefore(m, " "));
                list.add(substringAfter(m, " "));
            } else {
                list.add(m);
            }
        }
        return list.toArray(new String[list.size()]);
    }
}
