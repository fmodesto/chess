package com.fmotech.chess;

import static com.fmotech.chess.BitOperations.highInt;
import static com.fmotech.chess.BitOperations.joinInts;
import static com.fmotech.chess.Board.KING;

public class Move {

    public static final int MOVE_CAST_H = 0x80;
    public static final int MOVE_CAST_L = 0x40;
    public static final int MOVE_EP_CAP = 0x20;
    public static final int MOVE_EP = 0x10;
    public static final int MOVE_PROMO = 0x08;

    public static int create(int srcPos, int tgtPos, int srcType, int tgtType, int flags) {
        return flags << 24 | tgtType << 20 | (~srcType & 0xF) << 16 | tgtPos << 8 | srcPos;
    }

    public static int srcPos(int move) {
        return move & 0xFF;
    }

    public static int tgtPos(int move) {
        return move >>> 8 & 0xFF;
    }

    public static int srcType(int move) {
        return ~(move >>> 16) & 0x07;
    }

    public static int tgtType(int move) {
        return (move >>> 20) & 0x07;
    }

    public static int promotion(int move) {
        return move >>> 24 & 0x07;
    }

    public static int scoreMvvLva(int move) {
        return (move >>> 16) & 0xFF;
    }

    public static int flags(int move) {
        return move >>> 24 & 0xF8;
    }

    public static boolean hasFlag(int move, int flag) {
        return (flags(move) & flag) != 0;
    }

    public static boolean isCapture(int move) {
        return tgtType(move) != 0;
    }

    public static int evalCapture(int move) {
        int tgt = tgtType(move);
        int src = srcType(move);
        return tgt == 0 || src == KING ? 0 : tgt - src;
    }
}
