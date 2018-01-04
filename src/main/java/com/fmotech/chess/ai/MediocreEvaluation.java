package com.fmotech.chess.ai;

import com.fmotech.chess.Board;
import com.fmotech.chess.ai.mediocre.MediocreBoard;

public class MediocreEvaluation implements Evaluation {

    private MediocreBoard mediocre = new MediocreBoard();

    @Override
    public int evaluateBoardPosition(Board board, int alpha, int beta) {
        mediocre.initBoard(board);
        return MediocreBoardEvaluation.evaluate(mediocre);
    }
}
