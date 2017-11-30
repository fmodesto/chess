package com.fmotech.chess;

import org.junit.Test;

import static org.junit.Assert.*;

public class SimpleEvaluationTest {

    @Test
    public void testMaximumPuntuation() {
        Board board = FenFormatter.fromFen("R6R/3Q4/1Q4Q1/4Q3/2Q4Q/Q4Q2/pp1Q4/kBNN1KB1 w - - 0 1");
        System.out.println(SimpleEvaluation.evaluateBoardPosition(board));
    }
}