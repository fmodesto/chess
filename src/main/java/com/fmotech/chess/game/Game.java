package com.fmotech.chess.game;

import com.fmotech.chess.BitOperations;
import com.fmotech.chess.Board;
import com.fmotech.chess.FenFormatter;
import com.fmotech.chess.MoveGenerator;
import com.fmotech.chess.ai.AI;

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

    private AI ai = new AI();
    private Board initBoard;
    private Board board;
    private int[] moves = new int[512];
    private long[] hashes = new long[512];
    private Pattern FEN = Pattern.compile("[a-h][1-8][a-h][1-8]");

    public static void main(String[] args) {
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
                "1. e2e4 e7e5 2. g1f3 b8c6 3. d2d4 e5d4 4. f3d4 d8h4 5. b1c3 f8b4 6. d1d3 b4c3 7. b2c3 g8f6 8. d4f5 h4g4 \n" +
                "9. f2f3 g4g6 10. g2g4 h7h6 11. c1f4 d7d6 12. e1c1 e8g8 13. d3b5 g6h7 14. f1d3 a7a6 15. b5b1 h7h8 16. h1e1 f8e8 \n" +
                "17. f5d4 c6e5 18. d3e2 a8b8 19. b1b2 g7g5 20. f4e3 d6d5 21. e4d5 f6d5 22. e3d2 h8f6 23. c3c4 d5f4 24. e2f1 c8d7 \n" +
                "25. d2c3 f4g6 26. h2h3 b7b6 27. f1e2 g6h4 28. e2f1 f6f4 29. c3d2 f4g3 30. e1e3 c7c5 31. d4e2 g3h2 32. b2c3 f7f6 \n" +
                "33. d2e1 h4g6 34. e1g3 h2h1 35. f3f4 g5f4 36. e2f4 g6f4 37. g3f4 b8d8 38. f1e2 h1g2 39. c3b3 g2b7 40. f4h6 d7c6 \n" +
                "41. d1d8 e8d8 42. h6f4 e5g6 43. f4h2 c6e4 44. b3b2 d8d4 45. e3b3 b7h7 46. h2g1 d4d6 47. b3e3 g6f4 48. b2b3 h7h6 \n" +
                "49. e2f1 f4g2 50. f1g2 e4g2 51. c1b2 f6f5 52. g4f5 h6f6 53. c2c3 f6g5 54. h3h4 g5h4 55. b3a4 b6b5 56. a4a5 g2e4 \n" +
                "57. a5c7 d6d2 58. b2a3 d2g2 59. c7e5 g2g4 60. e5c5 h4d8 61. g1f2 d8d2 62. c5c8 g8f7 63. c8c7 f7f6 64. c7b6 f6f5 \n" +
                "65. b6c5 f5g6 66. c5c8 d2c1 67. a3b4 c1b2 68. b4a5 b2a2 69. a5b6 g6g5 70. c8g8 g5f4 71. g8f7 f4g5 72. e3h3 a2f2 \n" +
                "73. f7f2 e4f5 74. c4b5 a6b5 75. h3g3 f5d7 76. f2d2 g5f6 77. d2d6 f6g5 78. g3f3 g4a4 79. d6d7 a4f4 80. d7g7 g5f5 \n" +
                "81. g7f7 f5g5 82. f7f4 g5h5 83. f3g3 b5b4 84. f4g5 ");
//        System.out.println("\n");
        long initTime = System.currentTimeMillis();
//        game.autoPly(-1, 7);
        game.followPly(2000, 64, copy);
        double time = (System.currentTimeMillis() - initTime) / 1000D;
//        System.out.println(AI.nodesNegamaxTotal + " nps " + (AI.nodesNegamaxTotal / time));
//        System.out.println(AI.nodesQuiescenceTotal + " nps " + (AI.nodesQuiescenceTotal / time));
//        System.out.println((AI.nodesNegamaxTotal + AI.nodesQuiescenceTotal) + " nps " + ((AI.nodesNegamaxTotal + AI.nodesQuiescenceTotal) / time));
        System.out.println(game.pgn());
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
//        if (board.ply() % 2 == 0)
//            System.out.printf("%2d. ", board.fullMove());
//        System.out.print(raw + " ");
//        if ((board.ply() & 0x0F) == 0x0F)
//            System.out.println();
        int move = FEN.matcher(raw).find() ? moveFromFen(board, raw) : moveFromSan(board, raw);
        moves[board.ply()] = move;
        hashes[board.ply()] = board.hash();
        board = board.move(move).nextTurn();
    }

    public Board currentBoard() {
        return board;
    }

    public String thinkMove(int millis, int maxDepth) {
        ai.setPreviousPositions(board.ply(), hashes);
        return moveToFen(board, ai.think(millis, maxDepth, board));
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
                return (board.whiteTurn() ? "0-1 {black" : "1-0 {white") + " mates (claimed by Chessy)}";
            else
                return "1/2-1/2 {stalemate (claimed by Chessy)}";
        } else if (board.fifty() >= 100) {
            return "1/2-1/2 {fifty move rule (claimed by Chessy)}";
        } else if (countRepetitions() >= 3) {
            return "1/2-1/2 {3-fold repetition (claimed by Chessy)}";
        } else if (isDrawMaterial()) {
            return "1/2-1/2 {insufficient material (claimed by Chessy)}";
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
        if (count(board.ownQueens(), board.ownRooks()) >= 1)
            return false;
        if (count(board.enemyQueens(), board.enemyRooks()) >= 1)
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

    public void resetAI() {
        ai.reset();
    }

    public void resetBoard(Board board) {
        this.initBoard = board;
        this.board = board;
    }
}
