package com.fmotech.chess.ai;

import com.fmotech.chess.Board;

public interface Evaluation {

    int evaluateBoardPosition(Board board, int alpha, int beta);
}
