package com.fmotech.chess.game;

public class Console {

    public static void debug(String arg, Object... params) {
//        System.err.println(String.format(arg, params));
    }

    public static void info(String arg, Object... params) {
        System.out.println(String.format(arg, params));
    }
}
