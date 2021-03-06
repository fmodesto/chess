package com.fmotech.chess.epd;

import com.fmotech.chess.Board;
import com.fmotech.chess.MoveGenerator;
import com.fmotech.chess.epd.EpdReader.Epd;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.Long.parseLong;
import static java.time.LocalDateTime.now;
import static java.time.temporal.ChronoUnit.MILLIS;
import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class PerftTests {

    private final Epd epd;

    @Parameters
    public static List<Object[]> data() throws Exception {
        return EpdReader.read(Paths.get("src/test/resources/perftsuite.epd"))
                .map(e -> new Object[] { e })
                .collect(Collectors.toList());

    }

    public PerftTests(Epd epd) {
        this.epd = epd;
    }

    @Test
    public void execute() {
        epd.actions.forEach(e -> execute(parseLong(e.parameter), epd.board, e.action.charAt(1) - '0'));
        System.out.println("Done");
    }

    public void execute(long expected, Board board, int level) {
        System.out.println(board);
        LocalDateTime start = now();
        long count = MoveGenerator.countMoves(level, board);
        System.out.printf("%d: %10d in %6d ms\n", level, count, MILLIS.between(start, now()));
        assertEquals(expected, count);
    }
}
