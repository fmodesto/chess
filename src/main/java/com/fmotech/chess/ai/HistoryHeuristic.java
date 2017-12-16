package com.fmotech.chess.ai;

import com.fmotech.chess.Move;

import java.util.Arrays;

public class HistoryHeuristic {

    private int[] table = new int[2 * 6 * 64];

    public void addMove(int ply, int depth, int move) {
        table[index(ply, move)] += depth;
    }

    public int scoreMove(int ply, int move) {
        return table[index(ply, move)];
    }

    private int index(int ply, int move) {
        int index = ((Move.srcType(move) - 1) << 7);
        index |= (ply & 1) << 6;
        index |= Move.tgtPos(move);
        return index;
    }

    public void clear() {
        Arrays.fill(table, 0);
    }
}
