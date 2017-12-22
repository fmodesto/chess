package com.fmotech.chess.utils;

import com.fmotech.chess.Board;
import com.fmotech.chess.FenFormatter;
import com.fmotech.chess.utils.PgnFormatter.Game;
import com.fmotech.chess.utils.PgnFormatter.GameResult;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static com.fmotech.chess.SanFormatter.moveFromSan;
import static java.lang.Integer.parseInt;
import static org.apache.commons.lang3.StringUtils.split;

public class RecordedGamePlay {

    private static LongOpenHashSet set = new LongOpenHashSet();
    private static BufferedWriter writer;

    public static void main(String[] args) throws IOException {
        writer = Files.newBufferedWriter(Paths.get("boards"));
        set.add(Board.INIT.hash());
        writer.write(FenFormatter.toFen(Board.INIT) + "\n");

        Files.lines(Paths.get("src/test/resources/raw/games.txt"))
//                .limit(10)
                .map(e -> split(e, "\t"))
                .map(e -> new Game(GameResult.valueOf(e[0]), parseInt(e[1]), parseInt(e[2]), split(e[3], " ")))
                .forEach(RecordedGamePlay::play);

        writer.close();
    }

    private static void play(Game game) {
        try {
            Board board = Board.INIT;
            for (String move : game.moves) {
                board = board.move(moveFromSan(board, move)).nextTurn();
                if (!set.contains(board.hash())) {
                    set.add(board.hash());
                    writer.write(FenFormatter.toFen(board) + "\n");
                }
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
