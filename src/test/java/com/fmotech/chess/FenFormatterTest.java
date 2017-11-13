package com.fmotech.chess;

import org.junit.Test;

import static com.fmotech.chess.BoardTest.assertBoard;
import static com.fmotech.chess.DebugUtils.createBlackBoard;
import static com.fmotech.chess.DebugUtils.createWhiteBoard;
import static com.fmotech.chess.FenFormatter.fromFen;
import static com.fmotech.chess.FenFormatter.toFen;
import static org.junit.Assert.assertEquals;

public class FenFormatterTest {

    @Test
    public void testFromFen() {
        assertBoard(fromFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"),
                createWhiteBoard(
                        "r n b q·k·b n r",
                        "p p p p p p p p",
                        "               ",
                        "               ",
                        "               ",
                        "               ",
                        "P P P P P P P P",
                        "R N B Q·K·B N R"));

        assertBoard(fromFen("rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1"),
                createBlackBoard(
                        "r n b q·k·b n r",
                        "p p p p p p p p",
                        "               ",
                        "               ",
                        "        P      ",
                        "        ·      ",
                        "P P P P   P P P",
                        "R N B Q·K·B N R"));

        assertBoard(fromFen("rnbqkbnr/pp1ppppp/8/2p5/4P3/8/PPPP1PPP/RNBQKBNR w Qkq c6 0 2"),
                createWhiteBoard(
                        "r n b q·k·b n r",
                        "p p   p p p p p",
                        "    ·          ",
                        "    p          ",
                        "        P      ",
                        "               ",
                        "P P P P   P P P",
                        "R N B Q·K B N R"));

        assertBoard(fromFen("rnbqkbnr/pp1ppppp/8/2p5/4P3/5N2/PPPP1PPP/RNBQKB1R b - - 1 2"),
                createBlackBoard(
                        "r n b q k b n r",
                        "p p   p p p p p",
                        "               ",
                        "    p          ",
                        "        P      ",
                        "          N    ",
                        "P P P P   P P P",
                        "R N B Q K B   R"));
    }

    @Test
    public void testToFen() {
        Board board = fromFen("rnbqkbnr/pp1ppppp/8/2p5/4P3/5N2/PPPP1PPP/RNBQKB1R b - - 1 2");
        assertEquals("rnbqkbnr/pp1ppppp/8/2p5/4P3/5N2/PPPP1PPP/RNBQKB1R b - - 0 1", toFen(board));
    }
}