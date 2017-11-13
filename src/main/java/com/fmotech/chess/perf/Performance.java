package com.fmotech.chess.perf;

import com.fmotech.chess.Board;
import com.fmotech.chess.MoveGenerator;

interface Test {
    long get(long i);
}

class NoOp implements Test {
    @Override
    public long get(long i) {
        return 0;
    }
}

class MovesOp implements Test {

    @Override
    public long get(long i) {
        return MoveGenerator.generateDirtyMoves(Board.INIT, Performance.MOVES);
    }
}

public class Performance {

    public static final long[] MOVES = new long[32];

    public static void main(String[] args) {
        Test[] a = new Test[] {
                new NoOp(),
                new MovesOp(),
        };

        long seed = 0;
        for (int i = 0; i < 100; i++) {
            long c = 0;
            for ( int j = 0; j < a.length; j++ ) {
                c += testGet(a[j], 10_000_000);
            }
            seed += c;
        }
        System.out.println(seed);
    }

    private static long testGet(Test a, int iterations) {
        long nanos = System.nanoTime();
        long c = 0;
        for ( int i = 0; i < iterations; i++ ) {
            c += a.get(i);
        }
        long stop = System.nanoTime();
        System.out.printf("%-20stook %fms%n", a.getClass().getSimpleName(),
                (stop - nanos) / 1000000.0);
        return c;
    }
}
