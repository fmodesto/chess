package com.fmotech.chess.ai;

import com.fmotech.chess.Board;
import com.fmotech.chess.Moves;

import static com.fmotech.chess.BitOperations.bitCount;
import static com.fmotech.chess.BitOperations.fileFill;
import static com.fmotech.chess.BitOperations.lowestBit;
import static com.fmotech.chess.BitOperations.lowestBitPosition;
import static com.fmotech.chess.BitOperations.nextLowestBit;
import static com.fmotech.chess.BitOperations.northFill;
import static com.fmotech.chess.BitOperations.southFill;
import static com.fmotech.chess.MoveGenerator.generateKnightMask;
import static com.fmotech.chess.ai.EvaluationUtils.A1;
import static com.fmotech.chess.ai.EvaluationUtils.A2;
import static com.fmotech.chess.ai.EvaluationUtils.A3;
import static com.fmotech.chess.ai.EvaluationUtils.A6;
import static com.fmotech.chess.ai.EvaluationUtils.A7;
import static com.fmotech.chess.ai.EvaluationUtils.A8;
import static com.fmotech.chess.ai.EvaluationUtils.B1;
import static com.fmotech.chess.ai.EvaluationUtils.B2;
import static com.fmotech.chess.ai.EvaluationUtils.B3;
import static com.fmotech.chess.ai.EvaluationUtils.B4;
import static com.fmotech.chess.ai.EvaluationUtils.B5;
import static com.fmotech.chess.ai.EvaluationUtils.B6;
import static com.fmotech.chess.ai.EvaluationUtils.B7;
import static com.fmotech.chess.ai.EvaluationUtils.B8;
import static com.fmotech.chess.ai.EvaluationUtils.C1;
import static com.fmotech.chess.ai.EvaluationUtils.C2;
import static com.fmotech.chess.ai.EvaluationUtils.C3;
import static com.fmotech.chess.ai.EvaluationUtils.C6;
import static com.fmotech.chess.ai.EvaluationUtils.C7;
import static com.fmotech.chess.ai.EvaluationUtils.C8;
import static com.fmotech.chess.ai.EvaluationUtils.D2;
import static com.fmotech.chess.ai.EvaluationUtils.D3;
import static com.fmotech.chess.ai.EvaluationUtils.D6;
import static com.fmotech.chess.ai.EvaluationUtils.D7;
import static com.fmotech.chess.ai.EvaluationUtils.E2;
import static com.fmotech.chess.ai.EvaluationUtils.E3;
import static com.fmotech.chess.ai.EvaluationUtils.E6;
import static com.fmotech.chess.ai.EvaluationUtils.E7;
import static com.fmotech.chess.ai.EvaluationUtils.ENEMY_SIDE;
import static com.fmotech.chess.ai.EvaluationUtils.F1;
import static com.fmotech.chess.ai.EvaluationUtils.F2;
import static com.fmotech.chess.ai.EvaluationUtils.F3;
import static com.fmotech.chess.ai.EvaluationUtils.F6;
import static com.fmotech.chess.ai.EvaluationUtils.F7;
import static com.fmotech.chess.ai.EvaluationUtils.F8;
import static com.fmotech.chess.ai.EvaluationUtils.G1;
import static com.fmotech.chess.ai.EvaluationUtils.G2;
import static com.fmotech.chess.ai.EvaluationUtils.G3;
import static com.fmotech.chess.ai.EvaluationUtils.G4;
import static com.fmotech.chess.ai.EvaluationUtils.G5;
import static com.fmotech.chess.ai.EvaluationUtils.G6;
import static com.fmotech.chess.ai.EvaluationUtils.G7;
import static com.fmotech.chess.ai.EvaluationUtils.G8;
import static com.fmotech.chess.ai.EvaluationUtils.H1;
import static com.fmotech.chess.ai.EvaluationUtils.H2;
import static com.fmotech.chess.ai.EvaluationUtils.H3;
import static com.fmotech.chess.ai.EvaluationUtils.H6;
import static com.fmotech.chess.ai.EvaluationUtils.H7;
import static com.fmotech.chess.ai.EvaluationUtils.H8;
import static com.fmotech.chess.ai.EvaluationUtils.OWN_SIDE;
import static com.fmotech.chess.ai.EvaluationUtils.PAWN_ISOLATED_TABLE;
import static com.fmotech.chess.ai.EvaluationUtils.PAWN_PASSED_TABLE;
import static com.fmotech.chess.ai.EvaluationUtils.file;
import static com.fmotech.chess.ai.EvaluationUtils.rank;
import static com.fmotech.chess.ai.EvaluationUtils.toBoardPosition;
import static java.lang.Math.abs;

public class ChessyEvaluation implements Evaluation {

    private static final byte[][] PAWN_TABLE = toBoardPosition(
            0,   0,   0,   0,   0,   0,   0,   0,
           20,  20,  20,  30,  30,  20,  20,  20,
           10,  10,  10,  20,  20,  10,  10,  10,
            5,   5,   5,  10,  10,   5,   5,   5,
            0,   0,  10,  20,  20,  10,   0,   0,
            5,   0,   0,   5,   5,   0,   0,   5,
           10,  10,   0, -10, -10,   0,  10,  10,
            0,   0,   0,   0,   0,   0,   0,   0
    );

    private static final byte[][] KNIGHT_TABLE = toBoardPosition(
            0,   0,   0,   0,   0,   0,   0,   0,
            0,   0,   5,  10,  10,   5,   0,   0,
            5,  10,  10,  20,  20,  10,  10,   5,
            5,  10,  15,  20,  20,  15,  10,   5,
            0,   0,  10,  20,  20,  10,   5,   0,
            0,   0,  10,  10,  10,  10,   0,   0,
            0,   0,   0,   5,   5,   0,   0,   0,
            0, -10,   0,   0,   0,   0, -10,   0
    );

    private static final byte[][] BISHOP_TABLE = toBoardPosition(
            0,   0,   0,   0,   0,   0,   0,   0,
            0,   0,   0,  10,  10,   0,   0,   0,
            0,   0,  10,  15,  15,  10,   0,   0,
            0,  10,  15,  20,  20,  15,  10,   0,
            0,  10,  15,  20,  20,  15,  10,   0,
            0,   0,  10,  15,  15,  10,   0,   0,
            0,   0,   0,  10,  10,   0,   0,   0,
            0,   0, -10,   0,   0, -10,   0,   0
    );

    private static final byte[][] ROOK_TABLE = toBoardPosition(
            0,   0,   5,  10,  10,   5,   0,   0,
           25,  25,  25,  25,  25,  25,  25,  25,
            0,   0,   5,  10,  10,   5,   0,   0,
            0,   0,   5,  10,  10,   5,   0,   0,
            0,   0,   5,  10,  10,   5,   0,   0,
            0,   0,   5,  10,  10,   5,   0,   0,
            0,   0,   5,  10,  10,   5,   0,   0,
            0,   0,   5,  10,  10,   5,   0,   0
    );

    private static final byte[][] KING_OPENING = toBoardPosition(
          -70, -70, -70, -70, -70, -70, -70, -70,
          -70, -70, -70, -70, -70, -70, -70, -70,
          -70, -70, -70, -70, -70, -70, -70, -70,
          -70, -70, -70, -70, -70, -70, -70, -70,
          -70, -70, -70, -70, -70, -70, -70, -70,
          -50, -50, -50, -50, -50, -50, -50, -50,
          -30, -30, -30, -30, -30, -30, -30, -30,
            0,   5,   5, -10, -10,   0,  10,   5
    );

    private static final byte[][] KING_ENDING = toBoardPosition(
          -50, -10,   0,   0,   0,   0, -10, -50,
          -10,   0,  10,  10,  10,  10,   0, -10,
            0,  10,  20,  20,  20,  20,  10,   0,
            0,  10,  20,  40,  40,  20,  10,   0,
            0,  10,  20,  40,  40,  20,  10,   0,
            0,  10,  20,  20,  20,  20,  10,   0,
          -10,   0,  10,  10,  10,  10,   0, -10,
          -50, -10,   0,   0,   0,   0, -10, -50
    );

    private static final int[][] PAWN_PASSED = new int[][] { { 0, 5, 10, 20, 35, 60, 100, 200 }, { 200, 100, 60, 35, 20, 10, 5, 0 } };
    private static final int PAWN_DOUBLED = -10;
    private static final int PAWN_ISOLATED = -10;
    private static final int QUEEN_OPEN = 5;
    private static final int QUEEN_SEMI = 3;
    private static final int ROOK_OPEN = 10;
    private static final int ROOK_SEMI = 5;
    private static final int BISHOP_PAIR = 30;

    private static final int QUEEN = 1000;
    private static final int ROOK = 550;
    private static final int BISHOP = 325;
    private static final int KNIGHT = 325;
    private static final int PAWN = 100;

    private static final int QUEEN_EG = 1000;
    private static final int ROOK_EG = 550;
    private static final int BISHOP_EG = 325;
    private static final int KNIGHT_EG = 325;
    private static final int PAWN_EG = 120;

    static class Evaluation {
        public int mgMaterial;
        public int egMaterial;

        public int mgPosition;
        public int egPosition;

        public int mgDeffense;
        public int egDeffense;

        public int trapped;
        public int mobility;

        public void reset() {
            mgMaterial = 0;
            egMaterial = 0;

            mgPosition = 0;
            egPosition = 0;

            mgDeffense = 0;
            egDeffense = 0;

            trapped = 0;
            mobility = 0;
        }

        public int mgTotal() {
            return mgMaterial + mgPosition + mgDeffense + trapped + mobility;
        }

        public int egTotal() {
            return egMaterial + egPosition + egDeffense + trapped + mobility;
        }

        @Override
        public String toString() {
            return "Evaluation{" +
                    "mgMaterial=" + mgMaterial +
                    ", egMaterial=" + egMaterial +
                    ", mgPosition=" + mgPosition +
                    ", egPosition=" + egPosition +
                    ", mgDeffense=" + mgDeffense +
                    ", egDeffense=" + egDeffense +
                    ", trapped=" + trapped +
                    ", mobility=" + mobility +
                    '}';
        }
    }

    Evaluation ownEvaluation = new Evaluation();
    Evaluation enemyEvaluation = new Evaluation();

    @Override
    public int evaluateBoardPosition(Board board, int alpha, int beta) {
        long ownOpen = ~fileFill(board.ownPawns());
        long enemyOpen = ~fileFill(board.enemyPawns());
        long open = (ownOpen & enemyOpen);

        ownEvaluation.reset();
        enemyEvaluation.reset();

        long ownMask = 0;
        long enemyMask = 0;
        long tmpMask = 0;

        ownMask |= evalPawns(OWN_SIDE, ownEvaluation, board.ownPawns(), board.enemyPawns());
        enemyMask |= evalPawns(ENEMY_SIDE, enemyEvaluation, board.enemyPawns(), board.ownPawns());

        tmpMask = evalKnight(OWN_SIDE, ownEvaluation, board.ownKnights(), board.pieces(), board.ownPieces(), enemyMask);
        enemyMask |= evalKnight(ENEMY_SIDE, enemyEvaluation, board.enemyKnights(), board.pieces(), board.enemyPieces(), ownMask);
        ownMask |= tmpMask;

        tmpMask = evalBishop(OWN_SIDE, ownEvaluation, board.ownBishops(), board.pieces(), board.ownPieces(), enemyMask);
        enemyMask |= evalBishop(ENEMY_SIDE, enemyEvaluation, board.enemyBishops(), board.pieces(), board.enemyPieces(), ownMask);
        ownMask |= tmpMask;

        tmpMask = evalRook(OWN_SIDE, ownEvaluation, board.ownRooks(), open, ownOpen, board.pieces(), board.ownPieces(), enemyMask);
        enemyMask |= evalRook(ENEMY_SIDE, enemyEvaluation, board.enemyRooks(), open, enemyOpen, board.pieces(), board.enemyPieces(), ownMask);
        ownMask |= tmpMask;

        tmpMask = evalQueen(OWN_SIDE, ownEvaluation, board.ownQueens(), open, ownOpen, board.pieces(), board.ownPieces(), enemyMask);
        enemyMask |= evalQueen(ENEMY_SIDE, enemyEvaluation, board.enemyQueens(), open, enemyOpen, board.pieces(), board.enemyPieces(), ownMask);
        ownMask |= tmpMask;

        evalKing(OWN_SIDE, ownEvaluation, board.ownKing(), board.ownCastle(), board.ownPawns());
        evalKing(ENEMY_SIDE, enemyEvaluation, board.enemyKing(), board.enemyCastle(), board.enemyPawns());

        evalOwnTrappedPieces(ownEvaluation, board);
        evalEnemyTrappedPieces(enemyEvaluation, board);

        return adjustScore(board, mgScore(ownEvaluation, enemyEvaluation), egScore(ownEvaluation, enemyEvaluation));
    }

    private int mgScore(Evaluation own, Evaluation enemy) {
        return own.mgTotal() - enemy.mgTotal();
    }

    private int egScore(Evaluation own, Evaluation enemy) {
        return own.egTotal() - enemy.egTotal();
    }

    private int adjustScore(Board board, int opening, int endgame) {
        int phase = gamePhase(board);
        int adjustedScore = ((opening * (256 - phase)) + (endgame * phase)) / 256;
        int fifty = board.fifty();

        if (adjustedScore > 0 && ownDrawByMaterial(board)) return 0;
        if (adjustedScore < 0 && enemyDrawByMaterial(board)) return 0;

        return fifty > 20 ? (120 - fifty) * adjustedScore / 100 : adjustedScore;
    }

    private boolean ownDrawByMaterial(Board board) {
        return (board.ownPawns() | board.ownQueens() | board.ownRooks()) == 0
                && nextLowestBit(board.ownBishops()) == 0
                && nextLowestBit(nextLowestBit(board.ownKnights())) == 0;
    }

    private boolean enemyDrawByMaterial(Board board) {
        return (board.enemyPawns() | board.enemyQueens() | board.enemyRooks()) == 0
                && nextLowestBit(board.enemyBishops()) == 0
                && nextLowestBit(nextLowestBit(board.enemyKnights())) == 0;
    }

    private int gamePhase(Board board) {
        int KNIGHT_BISHOP_PHASE = 1;
        int ROOK_PHASE = 2;
        int QUEEN_PHASE = 4;
        int TOTAL_PHASE = KNIGHT_BISHOP_PHASE * 4 + KNIGHT_BISHOP_PHASE * 4 + ROOK_PHASE * 4 + QUEEN_PHASE * 2;

        int phase = TOTAL_PHASE;
        phase -= KNIGHT_BISHOP_PHASE * bitCount(board.knights() | board.bishops());
        phase -= ROOK_PHASE * bitCount(board.rooks());
        phase -= QUEEN_PHASE * bitCount(board.queens());

        return (phase * 256 + (TOTAL_PHASE / 2)) / TOTAL_PHASE;
    }

    void evalOwnTrappedPieces(Evaluation evaluation, Board board) {
        int score = 0;
        score += trapped(board.ownKnights(), board.enemyPawns(), A7, B7 | C6, -100);
        score += trapped(board.ownKnights(), board.enemyPawns(), H7, G7 | F6, -100);
        score += trappedAny(board.ownKnights(), board.enemyPawns(), A8, A7 | C7, -50);
        score += trappedAny(board.ownKnights(), board.enemyPawns(), H8, H7 | F7, -50);
        score += trapped(board.ownBishops(), board.enemyPawns(), A7, B6, -100);
        score += trapped(board.ownBishops(), board.enemyPawns(), A7, B6 | C7, -50);
        score += trapped(board.ownBishops(), board.enemyPawns(), H7, G6, -100);
        score += trapped(board.ownBishops(), board.enemyPawns(), H7, G6 | F7, -50);
        score += trapped(board.ownBishops(), board.enemyPawns(), B8, C7, -100);
        score += trapped(board.ownBishops(), board.enemyPawns(), B8, C7 | B6, -50);
        score += trapped(board.ownBishops(), board.enemyPawns(), G8, F7, -100);
        score += trapped(board.ownBishops(), board.enemyPawns(), G8, F7 | G6, -50);
        score += trapped(board.ownBishops(), board.enemyPawns(), A6, B5, -100);
        score += trapped(board.ownBishops(), board.enemyPawns(), H6, G5, -100);
        score += trappedAny(board.ownRooks(), board.ownKing(), H1 | H2 | G1 | G2, F1 | G1, -50);
        score += trappedAny(board.ownRooks(), board.ownKing(), A1 | A2 | B1 | B2, B1 | C1, -50);
        score += trapped(board.ownPawns(), board.pieces(), D2, D3, -20);
        score += trapped(board.ownPawns(), board.pieces(), board.ownBishops(), D2, D3, C1, -30);
        score += trapped(board.ownPawns(), board.pieces(), E2, E3, -20);
        score += trapped(board.ownPawns(), board.pieces(), board.ownBishops(), E2, E3, F1, -30);
        evaluation.trapped = score;
    }

    void evalEnemyTrappedPieces(Evaluation evaluation, Board board) {
        int score = 0;
        score += trapped(board.enemyKnights(), board.ownPawns(), A2, B2 | C3, -100);
        score += trapped(board.enemyKnights(), board.ownPawns(), H2, G2 | F3, -100);
        score += trappedAny(board.enemyKnights(), board.ownPawns(), A1, A2 | C2, -50);
        score += trappedAny(board.enemyKnights(), board.ownPawns(), H1, H2 | F2, -50);
        score += trapped(board.enemyBishops(), board.ownPawns(), A2, B3, -100);
        score += trapped(board.enemyBishops(), board.ownPawns(), A2, B3 | C2, -50);
        score += trapped(board.enemyBishops(), board.ownPawns(), H2, G3, -100);
        score += trapped(board.enemyBishops(), board.ownPawns(), H2, G3 | F2, -50);
        score += trapped(board.enemyBishops(), board.ownPawns(), B1, C2, -100);
        score += trapped(board.enemyBishops(), board.ownPawns(), B1, C2 | B3, -50);
        score += trapped(board.enemyBishops(), board.ownPawns(), G1, F2, -100);
        score += trapped(board.enemyBishops(), board.ownPawns(), G1, F2 | G3, -50);
        score += trapped(board.enemyBishops(), board.ownPawns(), A3, B4, -100);
        score += trapped(board.enemyBishops(), board.ownPawns(), H3, G4, -100);
        score += trappedAny(board.enemyRooks(), board.enemyKing(), H8 | H7 | G8 | G7, F8 | G8, -50);
        score += trappedAny(board.enemyRooks(), board.enemyKing(), A8 | A7 | B8 | B7, B8 | C8, -50);
        score += trapped(board.enemyPawns(), board.pieces(), D7, D6, -20);
        score += trapped(board.enemyPawns(), board.pieces(), board.enemyBishops(), D7, D6, C8, -30);
        score += trapped(board.enemyPawns(), board.pieces(), E7, E6, -20);
        score += trapped(board.enemyPawns(), board.pieces(), board.enemyBishops(), E7, E6, F8, -30);
        evaluation.trapped = score;
    }

    private int trapped(long mask1, long mask2, long set1, long set2, int score) {
        return (mask1 & set1) == set1 && (mask2 & set2) == set2 ? score : 0;
    }

    private int trappedAny(long mask1, long mask2, long set1, long set2, int score) {
        return (mask1 & set1) != 0 && (mask2 & set2) != 0 ? score : 0;
    }

    private int trapped(long mask1, long mask2, long mask3, long set1, long set2, long set3, int score) {
        return (mask1 & set1) == set1 && (mask2 & set2) == set2 && (mask3 & set3) == set3 ? score : 0;
    }

    private long evalPawns(int side, Evaluation evaluation, long own, long enemy) {
        long next = own;
        int doubled = 0;
        while (next != 0) {
            int pos = lowestBitPosition(next);
            evaluation.mgMaterial += PAWN;
            evaluation.mgPosition += PAWN_TABLE[side][pos];
            evaluation.egMaterial += PAWN_EG;
            evaluation.egPosition += PAWN_TABLE[side][pos];
            if ((doubled & fileMask(pos)) != 0) {
                evaluation.mgPosition += PAWN_DOUBLED;
                evaluation.egPosition += PAWN_DOUBLED;
            }
            if ((PAWN_ISOLATED_TABLE[pos] & own) == 0) {
                evaluation.mgPosition += PAWN_ISOLATED;
                evaluation.egPosition += PAWN_ISOLATED;
            }
            if ((PAWN_PASSED_TABLE[side][pos] & enemy) == 0) {
                evaluation.mgPosition += PAWN_PASSED[side][rank(pos)];
                evaluation.egPosition += PAWN_PASSED[side][rank(pos)];
            }
            doubled |= fileMask(pos);
            next = nextLowestBit(next);
        }
        return side == OWN_SIDE ? nw(own) | ne(own) : sw(own) | se(own);
    }

    private long evalKnight(int side, Evaluation evaluation, long knights, long pieces, long own, long attackMask) {
        long mobilityMask = 0;
        while (knights != 0) {
            int pos = lowestBitPosition(knights);
            evaluation.mgMaterial += KNIGHT;
            evaluation.mgPosition += KNIGHT_TABLE[side][pos];
            evaluation.egMaterial += KNIGHT_EG;
            evaluation.egPosition += KNIGHT_TABLE[side][pos];
            long mask = generateKnightMask(pos, pieces, own);
            evaluation.mobility += bitCount(mask);
            evaluation.mobility += 2 * bitCount(mask & ~attackMask);
            mobilityMask |= mask;
            knights = nextLowestBit(knights);
        }
        return 0;
    }

    private long evalBishop(int side, Evaluation evaluation, long bishops, long pieces, long own, long attackMask) {
        long mobilityMask = 0;
        int count = 0;
        while (bishops != 0) {
            int pos = lowestBitPosition(bishops);
            evaluation.mgMaterial += BISHOP;
            evaluation.mgPosition += BISHOP_TABLE[side][pos];
            evaluation.egMaterial += BISHOP_EG;
            evaluation.egPosition += BISHOP_TABLE[side][pos];
            count += 1;
            long mask = Moves.bishopMove(pos, pieces, own);
            evaluation.mobility += bitCount(mask);
            evaluation.mobility += 2 * bitCount(mask & ~attackMask);
            mobilityMask |= mask;
            bishops = nextLowestBit(bishops);
        }
        if (count > 1) {
            evaluation.mgPosition += BISHOP_PAIR;
            evaluation.egPosition += BISHOP_PAIR;
        }
        return mobilityMask;
    }

    private long evalRook(int side, Evaluation evaluation, long rooks, long open, long semi, long pieces, long own, long attackMask) {
        long mobilityMask = 0;
        long next = rooks;
        while (next != 0) {
            long piece = lowestBit(next);
            int pos = lowestBitPosition(next);
            evaluation.mgMaterial += ROOK;
            evaluation.mgPosition += ROOK_TABLE[side][pos];
            evaluation.egMaterial += ROOK_EG;
            evaluation.egPosition += ROOK_TABLE[side][pos];
            if ((piece & open) != 0) {
                evaluation.mgPosition += ROOK_OPEN;
                evaluation.egPosition += ROOK_OPEN;
            } else if ((piece & semi) != 0) {
                evaluation.mgPosition += ROOK_SEMI;
                evaluation.egPosition += ROOK_SEMI;
            }
            long mask = Moves.rookMove(pos, pieces, own);
            evaluation.mobility += bitCount(mask);
            evaluation.mobility += 2 * bitCount(mask & ~attackMask);
            mobilityMask |= mask;
            next = nextLowestBit(next);
        }
        return mobilityMask;
    }

    private long evalQueen(int side, Evaluation evaluation, long queens, long open, long semi, long pieces, long own, long attackMask) {
        long mobilityMask = 0;
        long next = queens;
        while (next != 0) {
            long piece = lowestBit(next);
            int pos = lowestBitPosition(next);
            evaluation.mgMaterial += QUEEN;
            evaluation.egMaterial += QUEEN_EG;
            if ((piece & open) != 0) {
                evaluation.mgPosition += QUEEN_OPEN;
                evaluation.egPosition += QUEEN_OPEN;
            } else if ((piece & semi) != 0) {
                evaluation.mgPosition += QUEEN_SEMI;
                evaluation.egPosition += QUEEN_SEMI;
            }
            long mask = Moves.queenMove(pos, pieces, own);
            evaluation.mobility += bitCount(mask);
            evaluation.mobility += 2 * bitCount(mask & ~attackMask);
            mobilityMask |= mask;
            next = nextLowestBit(next);
        }
        return mobilityMask;
    }

    final void evalKing(int side, Evaluation evaluation, long king, long castle, long pawns) {
        int pos = lowestBitPosition(king);
        evaluation.mgPosition += KING_OPENING[side][pos];
        evaluation.egPosition += KING_ENDING[side][pos];
        if (castle == 0) {
            evaluation.mgDeffense -= bitCount(pawnShield(side, pos, pawns));
        }
    }

    private long pawnShield(int side, int pos, long pawns) {
        long shield = PAWN_PASSED_TABLE[side][pos];
        long mask = side == OWN_SIDE ? northFill(shield & pawns) : southFill(shield & pawns);
        return shield & ~mask;
    }

    private long se(long own) {
        return (own & 0xfefefefefefefefeL) >>> 9;
    }

    private long sw(long own) {
        return (own & 0x7f7f7f7f7f7f7f7fL) >>> 7;
    }

    private long ne(long own) {
        return (own & 0xfefefefefefefefeL) << 7;
    }

    private long nw(long own) {
        return (own & 0x7f7f7f7f7f7f7f7fL) << 9;
    }

    private int fileMask(int pos) {
        return 1 << file(pos);
    }
}
