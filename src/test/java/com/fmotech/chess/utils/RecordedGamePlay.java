package com.fmotech.chess.utils;

import com.fmotech.chess.Board;
import com.fmotech.chess.utils.PgnFormatter.Game;
import com.fmotech.chess.utils.PgnFormatter.GameResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static com.fmotech.chess.SanFormatter.moveFromSan;
import static java.lang.Integer.parseInt;
import static org.apache.commons.lang3.StringUtils.split;

public class RecordedGamePlay {

    public static void main(String[] args) throws IOException {
        Files.lines(Paths.get("src/test/resources/raw/games.txt"))
//                .limit(10)
                .map(e -> split(e, "\t"))
                .map(e -> new Game(GameResult.valueOf(e[0]), parseInt(e[1]), parseInt(e[2]), split(e[3], " ")))
                .forEach(RecordedGamePlay::play);
    }

    private static void play(Game game) {
        try {
            Board board = Board.INIT;
            for (String move : game.moves) {
                board = board.move(moveFromSan(board, move)).nextTurn();
            }
        } catch (Exception e) {
            debugMove(game, e);
        }
    }

    private static void debugMove(Game game, Exception e) {
        System.out.println(e.getMessage());
        for (int i = 0; i < game.moves.length; i++) {
            if (i % 2 == 0) {
                System.out.print((1+(i/2)) + ". ");
            }
            System.out.print(game.moves[i] + " ");
        }
        System.out.println();
    }
}
