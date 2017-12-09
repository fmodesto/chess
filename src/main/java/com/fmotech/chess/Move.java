package com.fmotech.chess;

import static com.fmotech.chess.BitOperations.highInt;
import static com.fmotech.chess.BitOperations.joinInts;

public class Move {

    public static final int MOVE_CAST_H = 0x80;
    public static final int MOVE_CAST_L = 0x40;
    public static final int MOVE_EP_CAP = 0x20;
    public static final int MOVE_EP = 0x10;
    public static final int MOVE_PROMO = 0x08;

    public static int create(int srcPos, int tgtPos, int srcType, int tgtType, int flags) {
        return flags << 24 | tgtType << 20 | (~srcType & 0xF) << 16 | tgtPos << 8 | srcPos;
    }

    public static int srcPos(long move) {
        return (int) (move & 0xFF);
    }

    public static int tgtPos(long move) {
        return (int) (move >>> 8 & 0xFF);
    }

    public static int srcType(long move) {
        return (int) (~(move >>> 16) & 0x07);
    }

    public static int tgtType(long move) {
        return (int) ((move >>> 20) & 0x07);
    }

    public static int promotion(long move) {
        return (int) (move >>> 24 & 0x07);
    }

    public static int scoreMvvLva(long move) {
        return (int) ((move >>> 16) & 0xFF);
    }

    public static int flags(long move) {
        return (int) (move >>> 24 & 0xF8);
    }

    public static boolean hasFlag(long move, int flag) {
        return (flags(move) & flag) != 0;
    }

    public static long score(long move, int score) {
        return joinInts(score, (int) move);
    }

    public static int score(long move) {
        return highInt(move);
    }
}
