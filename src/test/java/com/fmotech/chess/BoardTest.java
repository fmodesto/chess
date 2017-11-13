package com.fmotech.chess;

import org.junit.Test;

import static com.fmotech.chess.DebugUtils.*;
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
        assertBoard(Board.INIT.move(w("e2"), w("e3")),
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
        assertBoard(Board.INIT.nextTurn().move(b("b7"), b("b6")),
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
        assertBoard(Board.INIT.move(w("d2"), w("d4")),
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
        assertBoard(Board.INIT.nextTurn().move(b("h7"), b("h5")),
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
        assertBoard(MIDDLE_GAME_WHITE.move(w("c4"), w("d5")),
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
        assertBoard(MIDDLE_GAME_BLACK.move(b("c5"), b("b4")),
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
        assertBoard(MIDDLE_GAME_WHITE.move(w("b5"), w("c6")),
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
        assertBoard(MIDDLE_GAME_BLACK.move(b("h4"), b("g3")),
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
        assertBoard(MIDDLE_GAME_BLACK.move(b("b2"), b("b1")),
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
        assertBoard(MIDDLE_GAME_BLACK.move(b("b2"), b("a1")),
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
        assertBoard(Board.INIT.move(w("b1"), w("c3")),
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
        assertBoard(Board.INIT.nextTurn().move(b("b8"), b("c6")),
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
                .move(w("a2"), w("a3")).nextTurn()
                .move(b("a7"), b("a6")).nextTurn()
                .move(w("a1"), w("a2"));
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
        assertBoard(CASTLE.move(w("e1"), w("g1")),
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
        assertBoard(CASTLE.move(w("e1"), w("c1")),
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
        assertBoard(CASTLE.nextTurn().move(b("e8"), b("g8")),
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
        assertBoard(CASTLE.nextTurn().move(b("e8"), b("c8")),
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

    @Test
    public void castle() {
        assertTrue(CASTLE.castleLow());
        assertTrue(CASTLE.castleHigh());
        assertTrue(CASTLE.nextTurn().castleLow());
        assertTrue(CASTLE.nextTurn().castleHigh());
    }

    public static void assertBoard(Board actual, Board expected) {
        DebugUtils.debug(CHESS, actual);
        assertEquals(expected, actual);
    }
}