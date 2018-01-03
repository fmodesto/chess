package com.fmotech.chess.fast;

public class Utils {
    public static final String SYMBOLS = "··PNBRQK";

    public static long BIT(int i) { return 1L << i; }
    public static boolean TEST(int position, long bitBoard) { return (BIT(position) & bitBoard) != 0; }
    public static int RANK(int position) { return position >>> 3; }
    public static int FILE(int position) { return position & 0x07; }
}
