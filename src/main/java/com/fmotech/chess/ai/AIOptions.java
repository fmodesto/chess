package com.fmotech.chess.ai;

public class AIOptions {
    public static boolean allowCheckEvasions = true;
    public static boolean allowNullMove = true;

    public static Evaluation evaluation() {
        return new ChessyEvaluation();
    }
}
