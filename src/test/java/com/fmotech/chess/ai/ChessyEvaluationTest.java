package com.fmotech.chess.ai;

import com.fmotech.chess.Board;
import com.fmotech.chess.ai.mediocre.OliThinkEvaluation;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Paths;

import static com.fmotech.chess.ai.AI.INFINITE;
import static java.nio.file.StandardOpenOption.READ;
import static org.junit.Assert.assertEquals;

public class ChessyEvaluationTest {

    @Test
    @Ignore
    public void evaluateBoard() throws IOException {
        SeekableByteChannel channel = Files.newByteChannel(Paths.get("board.dat"), READ);
        ByteBuffer buffer = ByteBuffer.allocate(80 * 1024);
        OliThinkEvaluation evaluation = new OliThinkEvaluation();
        Board white = Board.INIT.cloneBoard();
        Board black = Board.INIT.cloneBoard();
        int count = 0;
        while (channel.read(buffer) > 0) {
            buffer.flip();
            while (buffer.remaining() >= 80) {
                white.load(buffer);
                black.load(buffer);
                assertEquals(evaluation.evaluateBoardPosition(white, -INFINITE, INFINITE), -evaluation.evaluateBoardPosition(black, -INFINITE, INFINITE));
                count += 1;
            }
            buffer.compact();
        }
        System.out.println(count);
    }

    @Test
    public void test() {
        ChessyEvaluation whiteEval = new ChessyEvaluation();
        ChessyEvaluation blackEval = new ChessyEvaluation();
        Board wb = Board.fen("r1bqkbnr/pp1ppppp/2n5/2p5/4P3/2N5/PPPP1PPP/R1BQKBNR w KQkq - 3 2");
        Board bb = Board.fen("r1bqkbnr/pp1ppppp/2n5/2p5/4P3/2N5/PPPP1PPP/R1BQKBNR b KQkq - 3 2");
        int w = whiteEval.evaluateBoardPosition(wb, 0, 0);
        int b = blackEval.evaluateBoardPosition(bb, 0, 0);
        System.out.println(w);
        System.out.println(whiteEval.ownEvaluation);
        System.out.println(whiteEval.enemyEvaluation);
        System.out.println(b);
        System.out.println(blackEval.ownEvaluation);
        System.out.println(blackEval.enemyEvaluation);
    }
}