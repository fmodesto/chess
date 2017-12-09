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

    public static long rotateLeft(long i, int distance) {
        return Long.rotateLeft(i, distance);
    }

    public static long rotateRight(long i, int distance) {
        return Long.rotateRight(i, distance);
    }

    public static long joinInts(int high, int low) {
        return ((long) high << 32) | (low & 0xffffffffL);
    }

    public static int highInt(long n) {
        return  (int) (n >> 32);
    }

    public static int lowInt(long n) {
        return  (int) n;
    }

    public static long southFill(long n) {
        n |= (n >>  8);
        n |= (n >> 16);
        n |= (n >> 32);
        return n & 0xFFL;
    }

    public static long fileFill(long n) {
        return 0x0101010101010101L * southFill(n);
    }
}
