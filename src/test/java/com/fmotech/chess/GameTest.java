package com.fmotech.chess;

import org.junit.Test;

public class GameTest {

    @Test
    public void perpetualMove() {
        Game game1 = new Game(Board.fen("6k1/6p1/8/6KQ/1r6/q2b4/8/8 w - - 0 1"));
        game1.thinkMove(1000, 10);
        System.out.println(game1.fen());
        Game game2 = Game.load(
                "1. e4 e5 2. Nf3 Nc6 3. Bb5 a6 4. Ba4 Nf6 5. O-O d6 6. Re1 Bg4\n" +
                        "7. c3 Nd7 8. h3 Bxf3 9. Qxf3 Be7 10. d4 O-O 11. Be3 Bg5 12. d5\n" +
                        "Ncb8 13. Nd2 Bxe3 14. Qxe3 Nb6 15. Bc2 N8d7 16. b4 Qh4 17. c4\n" +
                        "Qf4 18. Qc3 Rac8 19. Nb3 f5 20. f3 fxe4 21. Bxe4 Nf6 22. Na5\n" +
                        "Rb8 23. c5 Nxe4 24. fxe4 Nd7 25. Nxb7 Nf6 26. cxd6 Rxb7\n" +
                        "27. dxc7 Rxc7 28. Qxc7 Ng4");
        System.out.println(game2.fen());
        game2.thinkMove(1000, 10);
    }

}