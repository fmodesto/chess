package com.fmotech.chess.ai;

import com.fmotech.chess.DebugUtils;
import org.junit.Test;

import static com.fmotech.chess.ai.PvData.ALPHA;
import static com.fmotech.chess.ai.PvData.BETA;
import static com.fmotech.chess.ai.PvData.EXACT;
import static org.junit.Assert.assertEquals;

public class PvDataTest {

    @Test
    public void testCreateExact() {
        long data = PvData.create(EXACT, 510, 14, 32000, 0xFFFFFFF8);
        System.out.println(DebugUtils.toHexString(data));
        assertEquals(EXACT, PvData.scoreType(data));
        assertEquals(510, PvData.ply(data));
        assertEquals(14, PvData.depth(data));
        assertEquals(32000, PvData.score(data));
        assertEquals(0xFFFFFFF8, PvData.move(data));
    }

    @Test
    public void testCreateAlpha() {
        long data = PvData.create(ALPHA, 510, 14, -32000, 0xFFFFFFF8);
        System.out.println(DebugUtils.toHexString(data));
        assertEquals(ALPHA, PvData.scoreType(data));
        assertEquals(510, PvData.ply(data));
        assertEquals(14, PvData.depth(data));
        assertEquals(-32000, PvData.score(data));
        assertEquals(0xFFFFFFF8, PvData.move(data));
    }

    @Test
    public void testCreateBeta() {
        long data = PvData.create(BETA, 510, 14, 0, 0xFFFFFFF8);
        System.out.println(DebugUtils.toHexString(data));
        assertEquals(BETA, PvData.scoreType(data));
        assertEquals(510, PvData.ply(data));
        assertEquals(14, PvData.depth(data));
        assertEquals(0, PvData.score(data));
        assertEquals(0xFFFFFFF8, PvData.move(data));
    }
}