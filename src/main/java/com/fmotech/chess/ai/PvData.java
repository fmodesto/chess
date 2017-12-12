package com.fmotech.chess.ai;

public class PvData {

    public static final long OPEN = 0x8000_0000_0000_0000L;
    public static final long CLOSE = 0x0000_0000_0000_0000L;

    public static final long EXACT = 0x6000_0000_0000_0000L;
    public static final long ALPHA = 0x4000_0000_0000_0000L;
    public static final long BETA = 0x2000_0000_0000_0000L;
    public static final long UNKNOWN = 0x0000_0000_0000_0000L;

    private static final int DEPTH_MASK = 0xF;
    private static final int PLY_MASK = 0x1FF;
    private static final int SCORE_MASK = 0xFFFF;

    public static long create(long flags, int ply, int depth, int score, int move) {
        return flags | (long) (ply & PLY_MASK) << 52 | (long) (depth & DEPTH_MASK) << 48 | (long) (score & SCORE_MASK) << 32 | move & 0xFFFFFFFFL;
    }

    public static long status(long data) {
        return data & OPEN;
    }

    public static long scoreType(long data) {
        return data & EXACT;
    }

    public static int ply(long data) {
        return (int) (data >>> 52 & PLY_MASK);
    }

    public static int depth(long data) {
        return (int) (data >>> 48 & DEPTH_MASK);
    }

    public static int score(long data) {
        return (short) (data >>> 32 & SCORE_MASK);
    }

    public static int move(long data) {
        return (int) data;
    }
}
