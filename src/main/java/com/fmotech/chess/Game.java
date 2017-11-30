package com.fmotech.chess;

import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;

public class Game {

    private final Board initBoard;
    private Board board;
    private int[] moves = new int[512];
    private Long2LongMap table = new Long2LongOpenHashMap();

    public Game() {
        this(Board.INIT);
    }

    public Game(Board init) {
        this.initBoard = init;
        this.board = init;
    }

    public void move(String sanMove) {
        int move = SanFormatter.moveFromSan(board, sanMove);
        moves[board.ply()] = move;
        board = board.move(move).nextTurn();
    }

    public Board currentBoard() {
        return board;
    }

    public void printPgn() {
        int max = board.ply();
        Board board = initBoard.cloneBoard();
        int index = 0;
        while (index < max) {
            if (index % 2 == 0)
                System.out.print(board.fullMove() + ". ");
            System.out.print(FenFormatter.moveToFen(board, moves[index]) + " ");

            board = board.move(moves[index]).nextTurn();

            if ((index & 15) == 15)
                System.out.println();

            index += 1;
        }
    }
}
