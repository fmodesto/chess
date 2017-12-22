package com.fmotech.chess.ai.mediocre;

/**
 * Definitions
 *
 * Contains constants used by multiple classes (like values of pieces)
 * 
 * First Created: 2006-12-14
 *
 * @author: Jonatan Pettersson (mediocrechess@gmail.com)
 */
public interface Definitions {

	// Side to move
	public static final int WHITE_TO_MOVE = 1;
	public static final int BLACK_TO_MOVE = -1;
	// END side to move
	
	// Side
	public static final int WHITE = 1;
	public static final int BLACK = -1;
	// END side

	// Constants for squares as they are represented on the 0x88 board
	public static final int A1 = 0;   public static final int A2 = 16;
	public static final int B1 = 1;   public static final int B2 = 17;
	public static final int C1 = 2;   public static final int C2 = 18;
	public static final int D1 = 3;   public static final int D2 = 19;
	public static final int E1 = 4;   public static final int E2 = 20;
	public static final int F1 = 5;   public static final int F2 = 21;
	public static final int G1 = 6;   public static final int G2 = 22;
	public static final int H1 = 7;   public static final int H2 = 23;
	
	public static final int A3 = 32;  public static final int A4 = 48;
	public static final int B3 = 33;  public static final int B4 = 49;
	public static final int C3 = 34;  public static final int C4 = 50;
	public static final int D3 = 35;  public static final int D4 = 51;
	public static final int E3 = 36;  public static final int E4 = 52;
	public static final int F3 = 37;  public static final int F4 = 53;
	public static final int G3 = 38;  public static final int G4 = 54;
	public static final int H3 = 39;  public static final int H4 = 55;
	
	public static final int A5 = 64;  public static final int A6 = 80;
	public static final int B5 = 65;  public static final int B6 = 81;
	public static final int C5 = 66;  public static final int C6 = 82;
	public static final int D5 = 67;  public static final int D6 = 83;
	public static final int E5 = 68;  public static final int E6 = 84;
	public static final int F5 = 69;  public static final int F6 = 85;
	public static final int G5 = 70;  public static final int G6 = 86;
	public static final int H5 = 71;  public static final int H6 = 87;
		
	public static final int A7 = 96;  public static final int A8 = 112;
	public static final int B7 = 97;  public static final int B8 = 113;
	public static final int C7 = 98;  public static final int C8 = 114;
	public static final int D7 = 99;  public static final int D8 = 115;
	public static final int E7 = 100; public static final int E8 = 116;
	public static final int F7 = 101; public static final int F8 = 117;
	public static final int G7 = 102; public static final int G8 = 118;
	public static final int H7 = 103; public static final int H8 = 119;
	// END squares
	

	// Constants for pieces
	public static final int W_KING = 1;
	public static final int W_QUEEN = 2;
	public static final int W_ROOK = 3;
	public static final int W_BISHOP = 4;
	public static final int W_KNIGHT = 5;
	public static final int W_PAWN = 6;

	public static final int B_KING = -1;
	public static final int B_QUEEN = -2;
	public static final int B_ROOK = -3;
	public static final int B_BISHOP = -4;
	public static final int B_KNIGHT = -5;
	public static final int B_PAWN = -6;

	public static final int EMPTY_SQUARE = 0;
	// END piece constants

	// Evaluation constants
	public static final int INFINITY = 32000;
	public static final int EVALNOTFOUND = 32001;
	// END evaluation constants
	
	// Game phase constans
	public static final int PHASE_OPENING = 0;
	public static final int PHASE_MIDDLE = 43;
	public static final int PHASE_ENDING = 171;
	public static final int PHASE_PAWN_ENDING = 256; // No null-moves in this phase

	// Constants for castling availability
	public static final int CASTLE_NONE = 0;
	public static final int CASTLE_SHORT = 1;
	public static final int CASTLE_LONG = 2;
	public static final int CASTLE_BOTH = 3;
	// END Castling availability constans

	// Piece deltas
	public static int[] bishop_delta = {-15, -17, 15, 17, 0, 0, 0, 0};
	public static int[] rook_delta = {-1, -16, 1, 16, 0, 0, 0, 0};
	public static int[] queen_delta = {-15, -17, 15, 17, -1, -16, 1, 16};
	public static int[] king_delta = {-15, -17, 15, 17, -1, -16, 1, 16};
	public static int[] knight_delta = {18, 33, 31, 14, -31, -33, -18, -14};
	public static int[] pawn_delta = {16, 32, 17, 15, 0, 0, 0, 0};
	// END piece deltas

	int[] BB_88 = { 7, 6, 5, 4, 3, 2, 1, 0, 23, 22, 21, 20, 19, 18, 17, 16, 39, 38, 37, 36, 35, 34, 33, 32, 55, 54, 53, 52, 51, 50, 49, 48, 71, 70, 69, 68, 67, 66, 65, 64, 87, 86, 85, 84, 83, 82, 81, 80, 103, 102, 101, 100, 99, 98, 97, 96, 119, 118, 117, 116, 115, 114, 113, 112 };
}
