package com.fmotech.chess;

import java.util.regex.Pattern;

import static com.fmotech.chess.FenFormatter.moveFromFen;
import static com.fmotech.chess.FenFormatter.moveToFen;
import static com.fmotech.chess.MoveGenerator.isInCheck;
import static com.fmotech.chess.SanFormatter.moveFromSan;
import static org.apache.commons.lang3.StringUtils.normalizeSpace;

public class Game {

    private final Board initBoard;
    private Board board;
    private int[] moves = new int[512];
    private long[] hashes = new long[512];
    private Pattern FEN = Pattern.compile("[a-h][1-8][a-h][1-8]");

    public static void main(String[] args) {
        Game game = new Game();
        game.autoPly(2000, 64);
        System.out.println(game.pgn());
//        Game game = Game.load("e2e4 e7e5 g1f3 g8f6 f3e5 d7d6 e5f3 f6e4 d2d3 e4f6 c1e3 c8e6 b1c3 d6d5 d3d4 f8b4 f1b5 b8d7 b5d7 e6d7 f3e5 f6e4 e3d2 b4c3 d2c3 a8c8 c3b4 d7e6 c2c4 f7f6 d1a4 c7c6 e5f3 d8b6 c4d5 e6d5 a4a3 f6f5 e1g1 c6c5 d4c5 b6a6 a3a6 b7a6 f1d1 d5e6 f3d4 e8f7 c5c6 c8b8 c6c7 b8c8 f2f3 e4f6 d4e6 f7e6 d1d6 e6f7 a1c1 h8e8 d6a6 f6d5 b4a3 d5f4 g1f1 c8a8 c1c5 f4d3 c5f5 f7g8 g2g3 e8e1 f1g2 e1e2 g2h3 d3f2 h3h4 f2d3 h2h3 d3e1 h4g4 g7g6 f5c5 a8c8 a6a7 h7h5 g4g5 g8g7 c5c3 e2e5 g5f4 e5f5 f4e4 h5h4 g3h4 g7f7 a3d6 e1g2 d6e5 g2h4 c3b3 f7e7 f3f4 e7d7 b3b6 g6g5 b6h6 f5f8 h6h7 d7c6 h7h6 c6d7 h6h7 d7c6");
//        System.out.println(game.thinkMove(-8));
//        h7h6 c6d7
//        System.out.println(game.isGameOver());
    }

    public static Game load(String pgn) {
        String[] moves = normalizeSpace(pgn.replaceAll("[0-9]*\\.", "")).split(" ");
        Game game = new Game();
        for (String move : moves)
            game.move(move);
        return game;
    }

    public Game() {
        this(Board.INIT);
    }

    public Game(Board init) {
        this.initBoard = init;
        this.board = init;
    }

    public void move(String raw) {
        int move = FEN.matcher(raw).find() ? moveFromFen(board, raw) : moveFromSan(board, raw);
        moves[board.ply()] = move;
        hashes[board.ply()] = board.hash();
        board = board.move(move).nextTurn();
    }

    public Board currentBoard() {
        return board;
    }

    public String thinkMove(int millis, int maxDepth) {
        return moveToFen(board, new AI(millis, maxDepth, board, hashes).think());
    }

    public void autoPly(int millis, int maxDepth) {
        long time = System.currentTimeMillis();
        while (!isGameOver()) {
            move(thinkMove(millis, maxDepth));
        }
        System.out.println(result());
        System.out.println("Total time: " + (System.currentTimeMillis() - time));
    }

    public boolean isGameOver() {
        if (MoveGenerator.generateValidMoves(board, board.moves()) == 0) {
            return true;
        } else if (board.fifty() >= 100) {
            return true;
        } else if (countRepetitions() >= 3) {
            return true;
        } else if (isDrawMaterial()) {
            return true;
        }
        return false;
    }

    public String result() {
        if (MoveGenerator.generateValidMoves(board, board.moves()) == 0) {
            if (isInCheck(board))
                return (board.whiteTurn() ? "0-1 {black" : "1-0 {white") + " mates (claimed by Cheesy)}";
            else
                return "1/2-1/2 {stalemate (claimed by Cheesy)}";
        } else if (board.fifty() >= 100) {
            return "1/2-1/2 {fifty move rule (claimed by Cheesy)}";
        } else if (countRepetitions() >= 3) {
            return "1/2-1/2 {3-fold repetition (claimed by Cheesy)}";
        } else if (isDrawMaterial()) {
            return "1/2-1/2 {insufficient material (claimed by Cheesy)}";
        }
        return "* {game in progress}";
    }

    private int countRepetitions() {
        long hash = board.hash();
        int count = 1;
        for (int i = board.ply() - 1; i >= board.ply() - board.fifty(); i--) {
            if (hashes[i] == hash) count++;
        }
        return count;
    }

    private boolean isDrawMaterial() {
        if (count(board.ownPawns(), board.enemyPawns()) >= 1)
            return false;
        if (count(board.ownQueens(), board.ownRocks()) >= 1)
            return false;
        if (count(board.enemyQueens(), board.enemyRocks()) >= 1)
            return false;
        if (count(board.ownKnights(), board.ownBishops()) >= 2)
            return false;
        if (count(board.enemyKnights(), board.enemyBishops()) >= 2)
            return false;
        return true;
    }

    private int count(long... pieces) {
        int total = 0;
        for (long piece : pieces) {
            total += BitOperations.bitCount(piece);
        }
        return total;
    }

    public String fen() {
        return FenFormatter.toFen(board);
    }

    public String pgn() {
        StringBuilder sb = new StringBuilder();
        int max = board.ply();
        Board board = initBoard.cloneBoard();
        int index = 0;
        while (index < max) {
            if (index % 2 == 0)
                sb.append(board.fullMove()).append(". ");
            sb.append(moveToFen(board, moves[index])).append(" ");

            board = board.move(moves[index]).nextTurn();

            if ((index & 15) == 15)
                sb.append("\n");

            index += 1;
        }
        return sb.toString();
    }

    public boolean whiteTurn() {
        return board.whiteTurn();
    }

    public String uci() {
        StringBuilder sb = new StringBuilder();
        int max = board.ply();
        Board board = this.initBoard.cloneBoard();
        if (Board.INIT.equals(initBoard)) {
            sb.append("position startpos");
        } else {
            sb.append("position fen ").append(FenFormatter.toFen(initBoard));
        }
        for (int i = 0; i < max; i++) {
            if (i == 0)
                sb.append(" moves");
            sb.append(" ").append(moveToFen(board, moves[i]));
            board = board.move(moves[i]).nextTurn();
        }
        return sb.toString();
    }
}
