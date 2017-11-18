package com.fmotech.chess.utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.lang.Integer.parseInt;
import static org.apache.commons.lang3.StringUtils.split;

public class Sorter {

    public enum GameResult {
        WHITE,
        BLACK,
        DRAW,
        UNKNOWN
    }

    public static class Game {
        int whiteRating;
        int blackRating;
        GameResult result;
        String moves;

        public Game(GameResult result, int whiteRating, int blackRating, String moves) {
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
                    ", moves='" + moves.hashCode() + '\'' +
                    '}';
        }
    }

    public static void main(String[] args) throws IOException {
        Map<String, List<Game>> games = Files.newBufferedReader(Paths.get("zip.txt"), StandardCharsets.UTF_8).lines()
                .map(e -> split(e, "\t"))
                .map(e -> new Game(GameResult.valueOf(e[0]), parseInt(e[1]), parseInt(e[2]), e[3]))
                .collect(Collectors.groupingBy(e -> e.moves));

        List<Game> gg = games.entrySet().stream()
                .map(Map.Entry::getValue)
//                .filter(e -> e.stream().map(f -> f.result).distinct().count() == 1)
                .map(Sorter::aggregate)
                .collect(Collectors.toList());

        List<String> lines = gg.stream()
                .map(e -> e.result + "\t" + e.whiteRating + "\t" + e.blackRating + "\t" + e.moves)
                .collect(Collectors.toList());

        Files.write(Paths.get("games.txt"), lines);
    }

    private static Game aggregate(List<Game> games) {
        if (games.size() == 1) return games.get(0);
        long res = games.stream().map(e -> e.result).distinct().count();
        int white = (int) Math.round(games.stream().mapToInt(e -> e.blackRating).average().getAsDouble());
        int black = (int) Math.round(games.stream().mapToInt(e -> e.blackRating).average().getAsDouble());
        return new Game(res > 1 ? GameResult.UNKNOWN : games.get(0).result, white, black, games.get(0).moves);
    }
}
