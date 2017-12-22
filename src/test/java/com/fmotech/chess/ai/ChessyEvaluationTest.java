package com.fmotech.chess.ai;

import com.fmotech.chess.Board;
import com.fmotech.chess.FenFormatter;
import org.apache.commons.lang3.StringUtils;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static com.fmotech.chess.FenFormatter.moveFromFen;
import static com.fmotech.chess.ai.AI.INFINITE;
import static com.fmotech.chess.ai.EvaluationUtils.ENEMY_SIDE;
import static com.fmotech.chess.ai.EvaluationUtils.OWN_SIDE;
import static org.apache.commons.lang3.StringUtils.substringAfter;
import static org.junit.Assert.assertEquals;

public class ChessyEvaluationTest {

    @Test
    @Ignore
    public void evaluateBoard() throws IOException {
        Files.lines(Paths.get("boards"))
                .forEach(this::eval);
        System.out.println(Files.lines(Paths.get("boards")).count());
    }

    ChessyEvaluation evaluation = new ChessyEvaluation();

    private void eval(String fen) {
        Board boardWhite = Board.fen(fen.replace(" b ", " w "));
        int w = evaluateBoardPosition(boardWhite);
        Board boardBlack = Board.fen(fen.replace(" w ", " b "));
        int b = evaluateBoardPosition(boardBlack);
//        if (w != -b) {
//            System.out.println(fen);
//            System.out.println(evaluateBoardPosition(boardWhite));
            System.out.println(evaluation.ownEvaluation);
            System.out.println(evaluation.enemyEvaluation);
//            System.out.println(evaluateBoardPosition(boardBlack));
            System.out.println(evaluation.ownEvaluation);
            System.out.println(evaluation.enemyEvaluation);
//        }
        assertEquals(w, -b);
    }

    private int evaluateBoardPosition(Board board) {
        return evaluation.evaluateBoardPosition(board, -INFINITE, +INFINITE);
    }

    @Test
    public void test() {
        Board board = Board.fen("8/2k2p2/R5p1/7p/4K2P/6P1/pr6/8 w - - 20 58");
        ChessyEvaluation evaluation = new ChessyEvaluation();
        evaluation.evaluateBoardPosition(board, 0, 0);
        System.out.println();
    }
}