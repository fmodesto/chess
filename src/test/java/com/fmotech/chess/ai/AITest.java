package com.fmotech.chess.ai;

import com.fmotech.chess.Board;
import com.fmotech.chess.game.Game;
import org.junit.Ignore;
import org.junit.Test;

public class AITest {

    @Test
    @Ignore
    public void testHorizonEffect() {
        Game game = new Game(Board.fen("5r1k/4Qpq1/4p3/1p1p2P1/2p2P2/1p2P3/3P4/BK6 b - - 0 1"));
        System.out.println(game.thinkMove(30000, 64));
    }

    @Test
    public void testEndgame() {
        Game game = new Game(Board.fen("8/8/7k/8/5KR1/8/8/8 b - - 40 99"));
        System.out.println(game.thinkMove(-1, 20));
    }
}
