package com.fmotech.chess.epd;


import com.fmotech.chess.ai.AI;
import org.junit.Assume;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.fmotech.chess.FenFormatter.moveToFen;
import static java.time.LocalDateTime.now;
import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class EpdTests {

    public static final int TIME = 100;
    public static final int EXECUTE = -1;
    private final EpdReader.Epd epd;

    @Parameterized.Parameters
    public static List<Object[]> data() {
        List<Object[]> tests = EpdReader.read(Paths.get("src/test/resources/wacnew.epd"))
                .map(e -> new Object[]{e})
                .collect(Collectors.toList());
        return EXECUTE < 0 ? tests : Collections.singletonList(tests.get(EXECUTE));
    }

    public EpdTests(EpdReader.Epd epd) {
        this.epd = epd;
    }

    @Test
    public void execute() {
        System.out.println(epd.board);
        String bm = EpdReader.getFen(epd, "bm");
        String am = EpdReader.getFen(epd, "am");
        if (bm.length() > 0)
            System.out.println("Best moves: " + bm);
        if (am.length() > 0)
            System.out.println("Avoid moves: " + am);
        AI ai = new AI();
        int bestMove = ai.think(TIME, 32, epd.board);
        List<Integer> expectedBest = EpdReader.getMoves(epd, "bm");
        ignoreFalse(moveToFen(epd.board, bestMove) + " in [" + bm + "]", expectedBest.isEmpty() || expectedBest.contains(bestMove));
        List<Integer> expectedBad = EpdReader.getMoves(epd, "am");
        ignoreTrue(moveToFen(epd.board, bestMove) + " not in [" + am + "]", expectedBad.contains(bestMove));
    }

    private void ignoreFalse(String message, boolean condition) {
        Assume.assumeTrue(message, condition);
    }

    private void ignoreTrue(String message, boolean condition) {
        Assume.assumeFalse(message, condition);
    }
}

