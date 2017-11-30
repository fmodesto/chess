package com.fmotech.chess;

import org.junit.Test;

import static com.fmotech.chess.FenFormatter.fromFen;
import static org.junit.Assert.*;

public class AITest {

    @Test
    public void testHorizonEffect() {
        Board board = fromFen("5r1k/4Qpq1/4p3/1p1p2P1/2p2P2/1p2P3/3P4/BK6 b - - 0 1");
        new AI(Integer.MAX_VALUE).think(board);
    }
}
/*
Scid  : position fen 5r1k/4Qpq1/4p3/1p1p2P1/2p2P2/1p2P3/3P4/BK6 b - - 0 1
Scid  : go infinite
Engine: info depth 1
Engine: info multipv 1 depth 1 seldepth 7 score cp 244 time 2 nodes 35 pv b3b2 a1b2
Engine: info multipv 1 depth 1 seldepth 14 score cp 263 time 3 nodes 348 pv d5d4 a1d4
Engine: info multipv 1 depth 1 seldepth 14 score cp 276 time 3 nodes 489 pv f7f6 a1f6
Engine: info depth 1 seldepth 14 time 3 nodes 1013 nps 0
Engine: info depth 2
Engine: info multipv 1 depth 2 seldepth 14 score cp 276 time 4 nodes 1085 pv f7f6 a1f6
Engine: info depth 2 seldepth 14 time 4 nodes 1218 nps 0
Engine: info depth 3
Engine: info multipv 1 depth 3 seldepth 14 score cp 74 upperbound time 5 nodes 1791 pv f7f6 a1f6 f8f6 g5f6 g7g6 b1a1
Engine: info depth 3 seldepth 14 time 5 nodes 2247 nps 0
Engine: info depth 4
Engine: info multipv 1 depth 4 seldepth 14 score cp 66 time 7 nodes 3664 pv f7f6 a1f6 f8f6 g5f6 g7g6 b1a1 g6g2
Engine: info multipv 1 depth 4 seldepth 14 score cp 139 lowerbound time 7 nodes 3957 pv d5d4 e7f7
Engine: info depth 4 seldepth 14 time 7 nodes 3957 nps 0
Engine: info depth 5
Engine: info multipv 1 depth 5 seldepth 14 score cp 79 upperbound time 8 nodes 4742 pv d5d4 a1d4 e6e5 d4e5 f7f6 e5f6
Engine: info depth 5 seldepth 14 time 10 nodes 6904 nps 0
Engine: info depth 6
Engine: info multipv 1 depth 6 seldepth 14 score cp 79 time 11 nodes 7667 pv d5d4 a1d4 e6e5 d4e5 f7f6 e5f6
Engine: info depth 6 seldepth 14 time 13 nodes 9319 nps 0
Engine: info depth 7
Engine: info multipv 1 depth 7 seldepth 28 score cp -37 upperbound time 45 nodes 36931 pv d5d4 a1d4 f7f6 d4f6 f8f6 g5f6 g7g6 b1a1 g6g2 e7e8 h8h7 f6f7 b3b2 a1b2 g2d2 b2b1
Engine: info depth 7 seldepth 28 time 81 nodes 70096 nps 0
Engine: info depth 8
Engine: info multipv 1 depth 8 seldepth 31 score cp -55 time 128 nodes 109348 pv d5d4 a1d4 f7f6 d4f6 f8f6 g5f6 g7g6 b1b2 g6g2 b2a3 g2a8 a3b4 a8g2 b4b5 g2d2 b5c4 d2e3
Engine: info multipv 1 depth 8 seldepth 32 score cp 0 time 236 nodes 200521 pv f7f6 a1f6 f8f6 g5f6 g7g6 b1a1 g6g2 f6f7 g2g7 a1b1 g7g6 b1a1 g6g7
Engine: info depth 8 seldepth 32 time 246 nodes 208239 nps 0
Engine: info depth 9
Engine: info multipv 1 depth 9 seldepth 38 score cp 0 time 627 nodes 526382 pv f7f6 a1f6 f8f6 g5f6 g7g6 b1a1 b5b4 e7e6 g6g8 e6h3 g8h7 f6f7 h7h3 f7f8q h8h7 f8f7 h7h6 f7f6 h6h7 f6f7
 */
/*
[Event "?"]
[Site "?"]
[Date "????.??.??"]
[Round "?"]
[White "?"]
[Black "?"]
[Result "*"]
[FEN "5r1k/4Qpq1/4p3/1p1p2P1/2p2P2/1p2P3/3P4/BK6 b - - 0 1"]

1...f7f6 {-91} a1f6 {-68} f8f6 {-129} g5f6 {-26} g7g8 {-24} b1b2 {-26} b5b4 {-27} e7b4 {-6} g8a8 {-4} b4a3 {-6} a8a3 {-186} b2a3 {-14} h8g8 {-16} a3b2 {-10}

 */