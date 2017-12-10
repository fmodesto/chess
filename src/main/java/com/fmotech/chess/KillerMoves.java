package com.fmotech.chess;

public class KillerMoves {

    private final int[] killers = new int[128];

    public void addKiller(int ply, int move) {
        int index = findIndex(ply);
        if (killers[index] != move) {
            killers[index + 1] = killers[index];
            killers[index] = move;
        }
    }

    public int getPrimaryKiller(int ply) {
        int index = findIndex(ply);
        return killers[index];
    }

    public int getSecundaryKiller(int ply) {
        int index = findIndex(ply);
        return killers[index + 1];
    }

    public int findIndex(int ply) {
        return (ply & 0x3F) << 1;
    }
}
