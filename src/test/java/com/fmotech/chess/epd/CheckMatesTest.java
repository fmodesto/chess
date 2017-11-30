package com.fmotech.chess.epd;


import com.fmotech.chess.AI;
import com.fmotech.chess.Board;
import com.fmotech.chess.MoveGenerator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static com.fmotech.chess.FenFormatter.moveToFen;
import static com.fmotech.chess.MoveGenerator.countMoves;
import static java.lang.Long.parseLong;
import static java.time.LocalDateTime.now;
import static java.time.temporal.ChronoUnit.MILLIS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
public class CheckMatesTest {

    private final EpdReader.Epd epd;

    @Parameterized.Parameters
    public static List<Object[]> data() throws Exception {
        return EpdReader.read(Paths.get("src/test/resources/larsen.epd"))
                .map(e -> new Object[] { e })
                .collect(Collectors.toList());

    }

    public CheckMatesTest(EpdReader.Epd epd) {
        this.epd = epd;
    }

    @Test
    public void execute() {
        System.out.println(epd.board);
        AI ai = new AI(5000);
        int bestMove = ai.think(epd.board);
        List<Integer> expectedBest = EpdReader.getMoves(epd, "bm");
        assertTrue(moveToFen(epd.board, bestMove) + " in [" + EpdReader.getSan(epd, "bm") + "]", expectedBest.contains(bestMove));
        List<Integer> expectedBad = EpdReader.getMoves(epd, "am");
        assertFalse(moveToFen(epd.board, bestMove) + " not in [" + EpdReader.getSan(epd, "am") + "]", expectedBad.contains(bestMove));
    }

    public void execute(long expected, Board board, int level) {
        System.out.println(board);
        LocalDateTime start = now();
        long count = MoveGenerator.countMoves(level, board);
        System.out.printf("%d: %10d in %6d ms\n", level, count, MILLIS.between(start, now()));
        assertEquals(expected, countMoves(level, board));
    }
}

