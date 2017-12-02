package com.fmotech.chess;

public class BitOperations {

    public static int lowestBitPosition(long n) {
        return Long.numberOfTrailingZeros(n);
    }

    public static long lowestBit(final long n) {
        return Long.lowestOneBit(n);
    }

    public static long nextLowestBit(long n) {
        return n ^ Long.lowestOneBit(n);
    }

    public static int highestBitPosition(long n) {
        return 63 - Long.numberOfLeadingZeros(n);
    }

    public static long highestBit(long n) {
        return Long.highestOneBit(n);
    }

    public static long nextHighestBit(long n) {
        return n ^ Long.highestOneBit(n);
    }

    public static int bitCount(long n) {
        return Long.bitCount(n);
    }

    public static long reverse(long n) {
        return Long.reverseBytes(n);
    }

    public static long joinInts(int high, int low) {
        return ((long) high << 32) | (low & 0xffffffffL);
    }

    public static int highInt(long l) {
        return  (int) (l >> 32);
    }

    public static int lowInt(long l) {
        return  (int) l;
    }
}
