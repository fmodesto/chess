package com.fmotech.chess.ai;

import java.util.Arrays;

public class FixSizeTable {

    private final long[] table;
    private final int mask;

    public FixSizeTable(int sizeInMb) {
        int size = Integer.highestOneBit(sizeInMb * 1024 * 1024 / 16);
        this.table = new long[2 * size];
        this.mask = size - 1;
    }

    public long get(long key) {
        int hash = hash(key);
        return table[hash] == key ? table[hash + 1] : 0;
    }

    public void put(long key, long value) {
        int hash = hash(key);
        table[hash] = key;
        table[hash + 1] = value;
    }

    private int hash(long key) {
        return (int) (key & mask) << 1;
    }

    public void clear() {
        Arrays.fill(table, 0);
    }
}
