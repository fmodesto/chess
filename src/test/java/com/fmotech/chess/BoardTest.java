package com.fmotech.chess;

import org.junit.Test;

import static com.fmotech.chess.DebugUtils.CHESS;
import static com.fmotech.chess.DebugUtils.createBlackBoard;
import static com.fmotech.chess.DebugUtils.createWhiteBoard;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BoardTest {

    private static final Board MIDDLE_GAME_WHITE = FenFormatter.fromFen("r3k2r/Pp1p1pp1/1b3nbN/1Ppn4/BBP1P2p/q4N2/Pp1P2PP/R2Q1RK1 w kq c6 0 1");
    private static final Board MIDDLE_GAME_BLACK = FenFormatter.fromFen("r3k2r/Pp1p1pp1/1b3nbN/1Ppn4/BBP1P1Pp/q4N2/Pp1P3P/R2Q1RK1 b kq g3 0 1");
    private static final Board CASTLE = FenFormatter.fromFen("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1");

    @Test
    public void initalPositionWhite() {
        assertBoard(Board.INIT,
                createWhiteBoard(
                        "r n b q·k·b n r",
                        "p p p p p p p p",
                        "               ",
                        "               ",
                        "               ",
                        "               ",
                        "P P P P P P P P",
                        "R N B Q·K·B N R"));
    }

    @Test
    public void initalPositionBlack() {
        assertBoard(Board.INIT.nextTurn(),
                createBlackBoard(
                        "r n b q·k·b n r",
                        "p p p p p p p p",
                        "               ",
                        "               ",
                        "               ",
                        "               ",
                        "P P P P P P P P",
                        "R N B Q·K·B N R"));
    }

    @Test
    public void pawnMoveWhite() {
        assertBoard(Board.INIT.move(w("e2e3")),
                createWhiteBoard(
                        "r n b q·k·b n r",
                        "p p p p p p p p",
                        "               ",
                        "               ",
                        "               ",
                        "        P      ",
                        "P P P P   P P P",
                        "R N B Q·K·B N R"));
    }

    @Test
    public void pawnMoveBlack() {
        assertBoard(Board.INIT.nextTurn().move(b("b7b6")),
                createBlackBoard(
                        "r n b q·k·b n r",
                        "p   p p p p p p",
                        "  p            ",
                        "               ",
                        "               ",
                        "               ",
                        "P P P P P P P P",
                        "R N B Q·K·B N R"));
    }

    @Test
    public void pawnJumpWhite() {
        assertBoard(Board.INIT.move(w("d2d4")),
                createWhiteBoard(
                        "r n b q·k·b n r",
                        "p p p p p p p p",
                        "               ",
                        "               ",
                        "      P        ",
                        "      ·        ",
                        "P P P   P P P P",
                        "R N B Q·K·B N R"));
    }

    @Test
    public void pawnJumpBlack() {
        assertBoard(Board.INIT.nextTurn().move(b("h7h5")),
                createBlackBoard(
                        "r n b q·k·b n r",
                        "p p p p p p p  ",
                        "              ·",
                        "              p",
                        "               ",
                        "               ",
                        "P P P P P P P P",
                        "R N B Q·K·B N R"));
    }

    @Test
    public void pawnKillsNormalWhite() {
        assertBoard(MIDDLE_GAME_WHITE.move(w("c4d5")),
                createWhiteBoard(
                        "r      ·k·    r",
                        "P p   p   p p  ",
                        "  b       n b N",
                        "  P p P        ",
                        "B B     P     p",
                        "q         N    ",
                        "P p   P     P P",
                        "R     Q   R K  "));
    }

    @Test
    public void pawnKillsNormalBlack() {
        assertBoard(MIDDLE_GAME_BLACK.move(b("c5b4")),
                createBlackBoard(
                        "r      ·k·    r",
                        "P p   p   p p  ",
                        "  b       n b N",
                        "  P   n        ",
                        "B p P   P   P p",
                        "q         N    ",
                        "P p   P       P",
                        "R     Q   R K  "));
    }

    @Test
    public void pawnKillsEnPassantWhite() {
        assertBoard(MIDDLE_GAME_WHITE.move(w("b5c6")),
                createWhiteBoard(
                        "r      ·k·    r",
                        "P p   p   p p  ",
                        "  b P     n b N",
                        "      n        ",
                        "B B P   P     p",
                        "q         N    ",
                        "P p   P     P P",
                        "R     Q   R K  "));
    }

    @Test
    public void pawnKillsEnPassantBlack() {
        assertBoard(MIDDLE_GAME_BLACK.move(b("h4g3")),
                createBlackBoard(
                        "r      ·k·    r",
                        "P p   p   p p  ",
                        "  b       n b N",
                        "  P p n        ",
                        "B B P   P      ",
                        "q         N p  ",
                        "P p   P       P",
                        "R     Q   R K  "));
    }

    @Test
    public void pawnPromotesBlack() {
        assertBoard(MIDDLE_GAME_BLACK.move(b("b2b1")),
                createBlackBoard(
                        "r      ·k·    r",
                        "P p   p   p p  ",
                        "  b       n b N",
                        "  P p n        ",
                        "B B P   P   P p",
                        "q         N    ",
                        "P     P       P",
                        "R q   Q   R K  "));
    }

    @Test
    public void pawnKillsAndPromotesBlack() {
        assertBoard(MIDDLE_GAME_BLACK.move(b("b2a1")),
                createBlackBoard(
                        "r      ·k·    r",
                        "P p   p   p p  ",
                        "  b       n b N",
                        "  P p n        ",
                        "B B P   P   P p",
                        "q         N    ",
                        "P     P       P",
                        "q     Q   R K  "));
    }

    @Test
    public void knightMoveWhite() {
        assertBoard(Board.INIT.move(w("b1c3")),
                createWhiteBoard(
                        "r n b q·k·b n r",
                        "p p p p p p p p",
                        "               ",
                        "               ",
                        "               ",
                        "    N          ",
                        "P P P P P P P P",
                        "R   B Q·K·B N R"));
    }

    @Test
    public void knightMoveBlack() {
        assertBoard(Board.INIT.nextTurn().move(b("b8c6")),
                createBlackBoard(
                        "r   b q·k·b n r",
                        "p p p p p p p p",
                        "    n          ",
                        "               ",
                        "               ",
                        "               ",
                        "P P P P P P P P",
                        "R N B Q·K·B N R"));
    }

    @Test
    public void rockMovesWhite() {
        Board board = Board.INIT
                .move(w("a2a3")).nextTurn()
                .move(b("a7a6")).nextTurn()
                .move(w("a1a2"));
        assertBoard(board,
                createWhiteBoard(
                        "r n b q·k·b n r",
                        "  p p p p p p p",
                        "p              ",
                        "               ",
                        "               ",
                        "P              ",
                        "R P P P P P P P",
                        "  N B Q K·B N R"));
    }

    @Test
    public void castleKingWhite() {
        assertBoard(CASTLE.move(w("e1g1")),
                createWhiteBoard(
                        "r      ·k·    r",
                        "p   p p q p b  ",
                        "b n     p n p  ",
                        "      P N      ",
                        "  p     P      ",
                        "    N     Q   p",
                        "P P P B B P P P",
                        "R         R K  "));
    }

    @Test
    public void castleQueenWhite() {
        assertBoard(CASTLE.move(w("e1c1")),
                createWhiteBoard(
                        "r      ·k·    r",
                        "p   p p q p b  ",
                        "b n     p n p  ",
                        "      P N      ",
                        "  p     P      ",
                        "    N     Q   p",
                        "P P P B B P P P",
                        "    K R       R"));
    }

    @Test
    public void castleKingBlack() {
        assertBoard(CASTLE.nextTurn().move(b("e8g8")),
                createBlackBoard(
                        "r         r k  ",
                        "p   p p q p b  ",
                        "b n     p n p  ",
                        "      P N      ",
                        "  p     P      ",
                        "    N     Q   p",
                        "P P P B B P P P",
                        "R      ·K·    R"));
    }

    @Test
    public void castleQueenBlack() {
        assertBoard(CASTLE.nextTurn().move(b("e8c8")),
                createBlackBoard(
                        "    k r       r",
                        "p   p p q p b  ",
                        "b n     p n p  ",
                        "      P N      ",
                        "  p     P      ",
                        "    N     Q   p",
                        "P P P B B P P P",
                        "R      ·K·    R"));
    }

    private int w(String move) {
        return FenFormatter.moveFromFen(Board.INIT, move);
    }

    private int b(String move) {
        return FenFormatter.moveFromFen(Board.INIT.nextTurn(), move);
    }

    @Test
    public void castle() {
        assertTrue(CASTLE.castleLow());
        assertTrue(CASTLE.castleHigh());
        assertTrue(CASTLE.nextTurn().castleLow());
        assertTrue(CASTLE.nextTurn().castleHigh());
    }

    public static void assertBoard(Board actual, Board expected) {
        assertEquals(expected, actual);
    }
}