package com.fmotech.chess;

import java.util.regex.Pattern;

import static com.fmotech.chess.FenFormatter.moveFromFen;
import static com.fmotech.chess.FenFormatter.moveToFen;
import static com.fmotech.chess.MoveGenerator.isInCheck;
import static com.fmotech.chess.SanFormatter.moveFromSan;
import static org.apache.commons.lang3.StringUtils.normalizeSpace;
import static org.apache.commons.lang3.StringUtils.trim;

public class Game {

    private final Board initBoard;
    private Board board;
    private int[] moves = new int[512];
    private long[] hashes = new long[512];
    private Pattern FEN = Pattern.compile("[a-h][1-8][a-h][1-8]");

    public static void main(String[] args) {
        AI.SILENT = true;
        Game game = new Game();
        game.autoPly(1000, 64);
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
        if (board.ply() % 2 == 0)
            System.out.printf("%2d. ", board.fullMove());
        System.out.print(raw + " ");
        if ((board.ply() & 0x0F) == 0x0F)
            System.out.println();
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
        System.out.println(result() + "{Total time: " + (System.currentTimeMillis() - time) + "}");
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

    public String moves() {
        StringBuilder sb = new StringBuilder();
        int max = board.ply();
        Board board = initBoard.cloneBoard();
        int index = 0;
        while (index < max) {
            sb.append(moveToFen(board, moves[index])).append(" ");
            board = board.move(moves[index]).nextTurn();
            index += 1;
        }
        return trim(sb.toString());
    }
}
