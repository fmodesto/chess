package com.fmotech.chess.utils;

import com.fmotech.chess.Board;
import com.fmotech.chess.ai.mediocre.MediocreBoard;
import com.fmotech.chess.ai.mediocre.MediocreBoardEvaluation;
import com.fmotech.chess.ai.mediocre.MediocreBoardEvaluation.EvalDetail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static com.fmotech.chess.ai.mediocre.MediocreBoardEvaluation.drawProbabilityEnding;
import static com.fmotech.chess.ai.mediocre.MediocreBoardEvaluation.drawProbabilityMiddle;
import static com.fmotech.chess.ai.mediocre.MediocreBoardEvaluation.evalDetail;
import static com.fmotech.chess.ai.mediocre.MediocreBoardEvaluation.gamePhase;
import static com.fmotech.chess.ai.mediocre.MediocreBoardEvaluation.gamePhaseScale;

public class ScoreStats {

    public static void main(String[] args) throws IOException {
        Files.lines(Paths.get("boards"))
                .map(Board::fen)
                .forEach(ScoreStats::evaluate);
    }

    private static void evaluate(Board board) {
        MediocreBoard mediocre = new MediocreBoard();
        mediocre.initBoard(board);
        printEval(mediocre);
//        MediocreBoardEvaluation.evaluate(mediocre);
//        EvalDetail eval = evalDetail;
//        System.out.println(eval.whiteMiddleStats() + ","
//                + eval.blackMiddleStats() + ","
//                + eval.whiteEndStats() + ","
//                + eval.blackEndStats());
    }



    /**
     * Almost the same as evaluate() but has a few traces and doesn't touch the hash tables
     * @param board
     * @return
     */
    public static final int printEval(MediocreBoard board) {
        MediocreBoardEvaluation.evaluate(board);

        int middleEval = drawProbabilityMiddle(board, evalDetail.totalWhiteMiddleEval() - evalDetail.totalBlackMiddleEval());
        int endingEval = drawProbabilityEnding(board, evalDetail.totalWhiteEndingEval() - evalDetail.totalBlackEndingEval());

        // Adjust the score for likelyhood of a draw
        int finalEval = gamePhaseScale(middleEval, endingEval);

        System.out.println("Game phase: " + gamePhase);
        System.out.println("Middle part: " + 100*(256 - gamePhase)/256 + "% Ending part: " + 100*(gamePhase)/256 + "%");
        System.out.println(board.getFen());

        System.out.println("                   WhiteM WhiteE BlackM BlackE TotalM TotalE  Total");
        System.out.format("Material.......... %6d %6d %6d %6d %6d %6d %6d\n",
                evalDetail.material.wm,
                evalDetail.material.we,
                evalDetail.material.bm,
                evalDetail.material.be,
                (evalDetail.material.wm-evalDetail.material.bm),
                (evalDetail.material.we-evalDetail.material.be),
                gamePhaseScale(evalDetail.material.wm-evalDetail.material.bm, evalDetail.material.we-evalDetail.material.be));
        System.out.format("Positioning....... %6d %6d %6d %6d %6d %6d %6d\n",
                evalDetail.piecePos.wm,
                evalDetail.piecePos.we,
                evalDetail.piecePos.bm,
                evalDetail.piecePos.be,
                (evalDetail.piecePos.wm-evalDetail.piecePos.bm),
                (evalDetail.piecePos.we-evalDetail.piecePos.be),
                gamePhaseScale(evalDetail.piecePos.wm-evalDetail.piecePos.bm, evalDetail.piecePos.we-evalDetail.piecePos.be));
        System.out.format("Trapped........... %6d %6d %6d %6d %6d %6d %6d\n",
                evalDetail.trappedEval.wm,
                evalDetail.trappedEval.we,
                evalDetail.trappedEval.bm,
                evalDetail.trappedEval.be,
                (evalDetail.trappedEval.wm-evalDetail.trappedEval.bm),
                (evalDetail.trappedEval.we-evalDetail.trappedEval.be),
                gamePhaseScale(evalDetail.trappedEval.wm-evalDetail.trappedEval.bm, evalDetail.trappedEval.we-evalDetail.trappedEval.be));
        System.out.format("Mobility.......... %6d %6d %6d %6d %6d %6d %6d\n",
                evalDetail.mobility.wm,
                evalDetail.mobility.we,
                evalDetail.mobility.bm,
                evalDetail.mobility.be,
                (evalDetail.mobility.wm-evalDetail.mobility.bm),
                (evalDetail.mobility.we-evalDetail.mobility.be),
                gamePhaseScale(evalDetail.mobility.wm-evalDetail.mobility.bm, evalDetail.mobility.we-evalDetail.mobility.be));
        System.out.format("Pawn structure.... %6d %6d %6d %6d %6d %6d %6d\n",
                evalDetail.pawnStructure.wm,
                evalDetail.pawnStructure.we,
                evalDetail.pawnStructure.bm,
                evalDetail.pawnStructure.be,
                (evalDetail.pawnStructure.wm-evalDetail.pawnStructure.bm),
                (evalDetail.pawnStructure.we-evalDetail.pawnStructure.be),
                gamePhaseScale(evalDetail.pawnStructure.wm-evalDetail.pawnStructure.bm, evalDetail.pawnStructure.we-evalDetail.pawnStructure.be));
        System.out.format("Passed pawns...... %6d %6d %6d %6d %6d %6d %6d\n",
                evalDetail.passerEval.wm,
                evalDetail.passerEval.we,
                evalDetail.passerEval.bm,
                evalDetail.passerEval.be,
                (evalDetail.passerEval.wm-evalDetail.passerEval.bm),
                (evalDetail.passerEval.we-evalDetail.passerEval.be),
                gamePhaseScale(evalDetail.passerEval.wm-evalDetail.passerEval.bm, evalDetail.passerEval.we-evalDetail.passerEval.be));
        System.out.format("King attacked..... %6d %6d %6d %6d %6d %6d %6d\n",
                evalDetail.kingAttacked.wm,
                evalDetail.kingAttacked.we,
                evalDetail.kingAttacked.bm,
                evalDetail.kingAttacked.be,
                (evalDetail.kingAttacked.wm-evalDetail.kingAttacked.bm),
                (evalDetail.kingAttacked.we-evalDetail.kingAttacked.be),
                gamePhaseScale(evalDetail.kingAttacked.wm-evalDetail.kingAttacked.bm, evalDetail.kingAttacked.we-evalDetail.kingAttacked.be));
        System.out.format("King defense...... %6d %6d %6d %6d %6d %6d %6d\n",
                evalDetail.kingDefense.wm,
                evalDetail.kingDefense.we,
                evalDetail.kingDefense.bm,
                evalDetail.kingDefense.be,
                (evalDetail.kingDefense.wm-evalDetail.kingDefense.bm),
                (evalDetail.kingDefense.we-evalDetail.kingDefense.be),
                gamePhaseScale(evalDetail.kingDefense.wm-evalDetail.kingDefense.bm, evalDetail.kingDefense.we-evalDetail.kingDefense.be));
        System.out.format("Tropism........... %6d %6d %6d %6d %6d %6d %6d\n",
                evalDetail.tropism.wm,
                evalDetail.tropism.we,
                evalDetail.tropism.bm,
                evalDetail.tropism.be,
                (evalDetail.tropism.wm-evalDetail.tropism.bm),
                (evalDetail.tropism.we-evalDetail.tropism.be),
                gamePhaseScale(evalDetail.tropism.wm-evalDetail.tropism.bm, evalDetail.tropism.we-evalDetail.tropism.be));
        System.out.format("Tempo............. %6d %6d %6d %6d %6d %6d %6d\n",
                evalDetail.tempoEval.wm,
                evalDetail.tempoEval.we,
                evalDetail.tempoEval.bm,
                evalDetail.tempoEval.be,
                (evalDetail.tempoEval.wm-evalDetail.tempoEval.bm),
                (evalDetail.tempoEval.we-evalDetail.tempoEval.be),
                gamePhaseScale(evalDetail.tempoEval.wm-evalDetail.tempoEval.bm, evalDetail.tempoEval.we-evalDetail.tempoEval.be));
        System.out.println("");
        System.out.format("Total eval........ %6d %6d %6d %6d %6d %6d %6d\n",evalDetail.totalWhiteMiddleEval(),evalDetail.totalWhiteEndingEval(),evalDetail.totalBlackMiddleEval(),evalDetail.totalBlackEndingEval(),evalDetail.totalWhiteMiddleEval() - evalDetail.totalBlackMiddleEval(),evalDetail.totalWhiteEndingEval() - evalDetail.totalBlackEndingEval(),gamePhaseScale(evalDetail.totalWhiteMiddleEval() - evalDetail.totalBlackMiddleEval(),evalDetail.totalWhiteEndingEval() - evalDetail.totalBlackEndingEval()));
        System.out.format("Adjusted to draw.. %6d %6d %6d %6d %6d %6d %6d\n",
                evalDetail.totalWhiteMiddleEval()-drawProbabilityMiddle(board,evalDetail.totalWhiteMiddleEval()),
                evalDetail.totalWhiteEndingEval()-drawProbabilityEnding(board,evalDetail.totalWhiteEndingEval()),
                evalDetail.totalBlackMiddleEval()-drawProbabilityMiddle(board,evalDetail.totalBlackMiddleEval()),
                evalDetail.totalBlackEndingEval()-drawProbabilityEnding(board,evalDetail.totalBlackEndingEval()),
                (evalDetail.totalWhiteMiddleEval() - evalDetail.totalBlackMiddleEval())-middleEval,
                (evalDetail.totalWhiteEndingEval() - evalDetail.totalBlackEndingEval())-endingEval,
                gamePhaseScale((evalDetail.totalWhiteMiddleEval() - evalDetail.totalBlackMiddleEval())-middleEval,(evalDetail.totalWhiteEndingEval() - evalDetail.totalBlackEndingEval())-endingEval));
        System.out.format("Final eval........ %6d %6d %6d %6d %6d %6d %6d\n",
                drawProbabilityMiddle(board,evalDetail.totalWhiteMiddleEval()),
                drawProbabilityEnding(board,evalDetail.totalWhiteEndingEval()),
                drawProbabilityMiddle(board,evalDetail.totalBlackMiddleEval()),
                drawProbabilityEnding(board,evalDetail.totalBlackEndingEval()),
                drawProbabilityMiddle(board,evalDetail.totalWhiteMiddleEval()-evalDetail.totalBlackMiddleEval()),
                drawProbabilityEnding(board,evalDetail.totalWhiteEndingEval()-evalDetail.totalBlackEndingEval()),
                finalEval);
        System.out.println();
        System.out.println("-------------------------------------------------------------------");
        System.out.println();

        return finalEval*board.toMove;
    }
}
