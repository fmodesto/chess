package com.fmotech.chess.ai;

public class HistoryHeuristic {

    private int[] table = new int[2 * 64 * 64];

    public void addMove(int ply, int depth, int move) {
        int d = depth - ply;
        table[index(ply, move)] += d * d;
    }

    public int scoreMove(int ply, int move) {
        return table[index(ply, move)];
    }

    private int index(int ply, int move) {
        return (((move >>> 2) & 0x3F00) | (move & 0x3F)) << (ply & 1);
    }
}
