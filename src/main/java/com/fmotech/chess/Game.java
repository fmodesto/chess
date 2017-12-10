package com.fmotech.chess;

import java.util.ArrayList;
import java.util.List;
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
//        game.autoPly(-1, 7);
//        Game copy = Game.load(
//                " 1. e2e4 e7e5  2. b1c3 f8c5  3. g1f3 b8c6  4. f3e5 c6e5  5. d2d4 c5d6  6. d4e5 d6e5  7. c1d2 g8f6  8. f2f4 e5d4 \n" +
//                " 9. e4e5 d4c3 10. d2c3 f6e4 11. d1f3 e4c3 12. f3c3 e8g8 13. e1c1 d8h4 14. g2g3 h4d8 15. f1h3 f8e8 16. c3d4 e8e7 \n" +
//                "17. h3f5 d8f8 18. d1d2 g7g6 19. f5g4 f8e8 20. g4f3 e8f8 21. h1d1 e7e8 22. c2c4 f8b4 23. a2a3 b4e7 24. c4c5 c7c6 \n" +
//                "25. f3g2 e7f8 26. g2f1 e8e7 27. d4c3 f8h6 28. f1d3 g6g5 29. c3d4 g5f4 30. g3f4 g8f8 31. a3a4 h6h4 32. a4a5 e7e6 \n" +
//                "33. d3f5 e6e7 34. d4f2 h4f2 35. d2f2 a8b8 36. f2f3 b7b6 37. a5b6 a7b6 38. f3b3 b6b5 39. b3h3 b8a8 40. h3h7 a8a1 \n" +
//                "41. c1c2 a1d1 42. c2d1 f8g8 43. h7h6 g8g7 44. h6d6 g7f8 45. h2h4 f8e8 46. h4h5 e8f8 47. h5h6 f8g8 48. d6d3 e7e8 \n" +
//                "49. f5d7 c8d7 50. d3d7 b5b4 51. d1c2 e8e6 52. h6h7 g8h7 53. d7f7 h7g8 54. f7b7 e6h6 55. b7b4 h6h2 56. c2b1 h2e2 \n" +
//                "57. b4d4 g8f8 58. d4d6 e2f2 59. d6c6 f2f4 60. c6f6 f4f6 61. e5f6 f8f7 62. c5c6 f7e8 63. f6f7 e8f7 64. c6c7 f7g8 \n" +
//                "65. c7c8q g8h7 66. b2b4 h7g7 67. b4b5 g7h7 68. b5b6 h7g7 69. b6b7 g7f7 70. b7b8q f7g6 71. b8g3 g6f7 72. g3g4 f7e7 \n" +
//                "73. g4e6");
        Game copy = Game.load(
                " 1. e2e4 e7e5  2. b1c3 b8c6  3. f1c4 f8c5  4. d2d3 d7d6  5. d1h5 g7g6  6. h5d1 d8h4  7. g2g3 h4d8  8. c3d5 c8e6 \n" +
                " 9. g1f3 e6d5 10. e4d5 c6a5 11. c1e3 c5e3 12. f2e3 a5c4 13. d3c4 f7f5 14. c2c3 g8f6 15. d1a4 d8d7 16. a4d7 e8d7 \n" +
                "17. a1d1 h8e8 18. f3g5 c7c6 19. d1d3 f6e4 20. g5e4 f5e4 21. d3d1 c6d5 22. c4d5 e8f8 23. e1e2 g6g5 24. g3g4 d7e7 \n" +
                "25. h1f1 f8f1 26. d1f1 a8c8 27. f1f5 h7h6 28. f5f1 c8c5 29. f1d1 c5a5 30. a2a3 a5c5 31. e2f2 e7f8 32. f2g1 f8g8 \n" +
                "33. d1d2 g8h8 34. d2d1 h8h7 35. d1d2 h7g8 36. d2d1 g8h8 37. d1d2 h8h7 38. d2d1 h7g7 39. d1d2 g7f7 40. d2f2 f7e8 \n" +
                "41. f2d2 e8d8 42. d2d1 d8c8 43. d1d2 c8b8 44. d2d1 b8c7 45. g1g2 c7b6 46. a3a4 c5c4 47. a4a5 b6a6 48. d1c1 c4c5 \n" +
                "49. c3c4 a6a5 50. c1a1 a5b4 51. a1a7 b4c4 52. a7b7 c4d5 53. b7f7 d5e6 54. f7h7 c5c2 55. g2g1 c2b2 56. h7h6 e6d5 \n" +
                "57. g1f1 d5c5 58. h6g6 b2h2 59. g6g5 c5c4 60. g5h5 h2c2 61. g4g5 c4d3 62. h5h6 d6d5 63. h6e6 d3e3 64. e6e5 d5d4 \n" +
                "65. g5g6 d4d3 66. g6g7 d3d2 67. e5d5 c2c8 68. g7g8r c8g8 69. d5d7 g8f8 70. f1g1 e3e2 71. d7d4 e4e3 72. d4d7 d2d1q \n" +
                "73. d7d1 e2d1 74. g1h1 e3e2 75. h1g1 e2e1q 76. g1h2 f8g8 77. h2h3 e1g3");
        System.out.println("\n");
        long initTime = System.currentTimeMillis();
//        game.autoPly(-1, 7);
        game.followPly(-1, 7, copy);
        double time = (System.currentTimeMillis() - initTime) / 1000D;
        System.out.println(AI.nodesNegamaxTotal + " nps " + (AI.nodesNegamaxTotal / time));
        System.out.println(AI.nodesQuiescenceTotal + " nps " + (AI.nodesQuiescenceTotal / time));
        System.out.println((AI.nodesNegamaxTotal + AI.nodesQuiescenceTotal) + " nps " + ((AI.nodesNegamaxTotal + AI.nodesQuiescenceTotal) / time));
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

    private void followPly(int millis, int maxDepth, Game copy) {
        long time = System.currentTimeMillis();
        int diffs = 0;
        List<String> changes = new ArrayList<>();
        while (!isGameOver()) {
            String raw = thinkMove(millis, maxDepth);
            int newMove = FenFormatter.moveFromFen(board, raw);
            int originalMove = copy.moves[board.ply()];
            if (newMove != originalMove) {
                diffs++;
                changes.add(board.fullMove() + ". " + FenFormatter.moveToFen(board, originalMove) + " vs " + FenFormatter.moveToFen(board, newMove));
            }
            move(moveToFen(board, originalMove));
        }
        System.out.println(result() + "{Total time: " + (System.currentTimeMillis() - time) + "}");
        System.out.println("Differs: " + diffs + " " + changes);
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
