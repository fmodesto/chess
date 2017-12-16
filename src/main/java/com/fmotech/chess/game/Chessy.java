package com.fmotech.chess.game;

import java.util.Scanner;

public class Chessy {

    public static void main(String[] args) {
        System.out.println("Chessy 0.1");
        System.out.println("by Francisco Modesto");
        System.out.println("enter uci or xboard to start");
        String protocol = new Scanner(System.in).nextLine();
        if ("uci".equals(protocol)) {
            UciProtocol.execute();
        } else if ("xboard".equals(protocol)) {
            XboardProtocol.execute();
        }
    }
}