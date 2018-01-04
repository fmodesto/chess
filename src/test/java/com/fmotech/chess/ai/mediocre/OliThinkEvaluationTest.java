package com.fmotech.chess.ai.mediocre;

import com.fmotech.chess.Board;
import com.fmotech.chess.ai.OliThinkEvaluation;
import org.junit.Test;

public class OliThinkEvaluationTest {

    @Test
    public void testError() {
        Board board;
        board = Board.fen("rnb1kbnr/pppp1p1p/5qp1/4p2Q/3PP3/8/PPP1BPPP/RNB1K1NR b KQkq - 1 4");
        System.out.println(new OliThinkEvaluation().evaluateBoardPosition(board, 0, 0));
        board = Board.fen("rnb1kbnr/pppp1p1p/5qp1/4p3/3PP3/8/PPP1BPPP/RNB1K1NR b KQkq - 1 4");
        System.out.println(new OliThinkEvaluation().evaluateBoardPosition(board, 0, 0));
    }

}