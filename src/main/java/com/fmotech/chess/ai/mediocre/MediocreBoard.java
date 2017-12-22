package com.fmotech.chess.ai.mediocre;

import com.fmotech.chess.BitOperations;
import com.fmotech.chess.Board;

import java.util.Arrays;

/**
 * class Board
 * 
 * Represents a position on the board and also contains methods for making and
 * unmaking a move, as well as generating all possible (legal) moves on the
 * board.
 * 
 * First created: 2006-12-14
 * 
 * @author Jonatan Pettersson (mediocrechess@gmail.com)
 */

public class MediocreBoard implements Definitions {
	public int[] boardArray; // Represents the 0x88 board
	public int[] boardArrayUnique; // Keeps track of what index a piece on a

	public int toMove; // Side to move on the board
	public int movesFifty; // Keeps track of half-moves since last capture

	public PieceList w_pawns;
	public PieceList b_pawns;
	public PieceList w_knights;
	public PieceList b_knights;
	public PieceList w_bishops;
	public PieceList b_bishops;
	public PieceList w_rooks;
	public PieceList b_rooks;
	public PieceList w_queens;
	public PieceList b_queens;
	public PieceList w_king;
	public PieceList b_king;

	// END Variables

	/**
	 * Creates an empty board
	 * 
	 * @return An empty Board
	 */
	public MediocreBoard() {
		this.boardArray = new int[128];
		this.boardArrayUnique = new int[128];
		this.toMove = 1; // White to move
		this.w_pawns = new PieceList();
		this.b_pawns = new PieceList();
		this.w_knights = new PieceList();
		this.b_knights = new PieceList();
		this.w_bishops = new PieceList();
		this.b_bishops = new PieceList();
		this.w_rooks = new PieceList();
		this.b_rooks = new PieceList();
		this.w_queens = new PieceList();
		this.b_queens = new PieceList();
		this.w_king = new PieceList();
		this.b_king = new PieceList();
	} // END Board()

	public void clear() {
		Arrays.fill(boardArray, 0); // Empties the board from any pieces
		Arrays.fill(boardArrayUnique, -1); // Empties the board from any pieces

		// Reset the piece lists
		this.w_pawns.clear();
		this.b_pawns.clear();
		this.w_knights.clear();
		this.b_knights.clear();
		this.w_bishops.clear();
		this.b_bishops.clear();
		this.w_rooks.clear();
		this.b_rooks.clear();
		this.w_queens.clear();
		this.b_queens.clear();
		this.w_king.clear();
		this.b_king.clear();
	}

	public void initBoard(Board board) {
		clear();
		toMove = board.whiteTurn() ? WHITE_TO_MOVE : BLACK_TO_MOVE;
		movesFifty = board.fifty();
		if (!board.whiteTurn()) board = board.nextTurn();
		addPieces(board.ownPawns(), w_pawns, W_PAWN);
		addPieces(board.ownKnights(), w_knights, W_KNIGHT);
		addPieces(board.ownBishops(), w_bishops, W_BISHOP);
		addPieces(board.ownRooks(), w_rooks, W_ROOK);
		addPieces(board.ownQueens(), w_queens, W_QUEEN);
		addPieces(board.ownKing(), w_king, W_KING);

		addPieces(board.enemyPawns(), b_pawns, B_PAWN);
		addPieces(board.enemyKnights(), b_knights, B_KNIGHT);
		addPieces(board.enemyBishops(), b_bishops, B_BISHOP);
		addPieces(board.enemyRooks(), b_rooks, B_ROOK);
		addPieces(board.enemyQueens(), b_queens, B_QUEEN);
		addPieces(board.enemyKing(), b_king, B_KING);
	}

	private void addPieces(long pieces, PieceList list, int type) {
		long next = pieces;
		while (next != 0) {
			int pos = BitOperations.lowestBitPosition(next);
			int boardIndex = BB_88[pos];
			boardArray[boardIndex] = type;
			list.addPiece(boardIndex);
			next = BitOperations.nextLowestBit(next);
		}
	}

	/**
	 * The general class for the piece lists
	 * 
	 * This class is quite fragile so it has to be used right, for example
	 * editing the pieces array in any way but using the internal methods is
	 * dangerous (we have to remember updating the boardArraUnique etc)
	 * 
	 * Also trying to remove a piece if count==0 will not be pleasant, we need
	 * to be careful to never do things like this
	 * 
	 * It is possible to write this class in a safer way, but it costs a little
	 * time and should not be necessary
	 * 
	 * We only work with boardArrayUnique here and never touch the boardArray,
	 * that is done elsewhere (in makeMove and unmakeMove)
	 * 
	 * For promotions you need to remove the pawn/promoted piece (make/unmake)
	 * from the corresponding list and add it to the other. Make sure you always
	 * add AFTER removing the first piece since boardArrayUnique is reset after
	 * removing a piece (making it impossible to find the added piece if it is
	 * not added after the remove)
	 */
	public class PieceList {
		public int[] pieces; // The indexes the white of the certain type is
								// on
		public int count; // The number of pieces (how many slots in the array
							// are filled with indexes)

		public PieceList() {
			this.pieces = new int[10];
			this.count = 0;
		}

		/**
		 * Adds a piece to the list
		 * 
		 * @param boardIndex
		 *            Index where the new piece should be
		 */
		public void addPiece(int boardIndex) {
			boardArrayUnique[boardIndex] = count; // Record the list index for
													// the piece ('count' works
													// here as last filled index
													// +1)
			pieces[count] = boardIndex; // Record the board index in the list
			count++; // Now we can increment the number of pieces
		} // END addPiece()

		public void clear() {
			this.count = 0;
		}
	} // END PieceList


	/**
	 * Returns the current position in FEN-notation
	 * 
	 * @return A string with FEN-notation
	 */
	public final String getFen() {
		String fen_string = ""; // This holds the FEN-string

		// ***
		// The following lines adds the pieces and empty squares to the FEN
		// ***

		int index = 112; // Keeps track of the index on the board
		int empties = 0; // Number of empty squares in a row

		while (index >= 0) // Run until end of the real board
		{
			if ((index & 0x88) != 0) // Reached the end of a rank
			{
				if (empties != 0) {
					fen_string += empties; // Add the empties number if it's
											// not 0
					empties = 0;
				}
				index -= 24; // Jump to the next rank
				if (index >= 0)
					fen_string += "/"; // Add to mark a new rank, if we're not
										// at the end
			} else // The index is on the real board
			{
				if (boardArray[index] != EMPTY_SQUARE) // If a piece is on the
														// square
				// i.e. the square it not empty
				{
					if (empties != 0)
						fen_string += empties; // Add the empty square number
					// if it's not 0
					empties = 0; // Reset empties (since we now have a piece
									// coming)
				}

				switch (boardArray[index]) {
				// Add the piece on the square
				case W_KING:
					fen_string += "K";
					break;
				case W_QUEEN:
					fen_string += "Q";
					break;
				case W_ROOK:
					fen_string += "R";
					break;
				case W_BISHOP:
					fen_string += "B";
					break;
				case W_KNIGHT:
					fen_string += "N";
					break;
				case W_PAWN:
					fen_string += "P";
					break;
				case B_KING:
					fen_string += "k";
					break;
				case B_QUEEN:
					fen_string += "q";
					break;
				case B_ROOK:
					fen_string += "r";
					break;
				case B_BISHOP:
					fen_string += "b";
					break;
				case B_KNIGHT:
					fen_string += "n";
					break;
				case B_PAWN:
					fen_string += "p";
					break;
				default:
					empties++; // If no piece, increment the empty square count
				}
				index++; // Go to the next square
			}

		}

		// END Adding pieces

		fen_string += " "; // Add space for next part

		// Adds side to move (important space before the letter here)
		if (toMove == WHITE_TO_MOVE)
			fen_string += "w"; // White's move
		else
			fen_string += "b"; // Black's move

		fen_string += " "; // Add space for next part

		// Castling rights
		fen_string += "-"; // Neither

		fen_string += " "; // Add space for next part

		// En passant square

		fen_string += "-"; // If no en passant is available

		fen_string += " "; // Add space for next part
		fen_string += movesFifty; // Add half-moves since last capture/pawn
									// move
		fen_string += " ";
		fen_string += 1; // Add number of full moves in the game so
									// far

		return fen_string; // Returns the finished FEN-string
	} // END getFEN()

	public final void inputFen(String fen) {
		String trimmedFen = fen.trim(); // Removes any white spaces in front or
										// behind the string
		Arrays.fill(boardArray, 0); // Empties the board from any pieces
		Arrays.fill(boardArrayUnique, -1); // Empties the board from any pieces

		// Reset the piece lists
		this.w_pawns.clear();
		this.b_pawns.clear();
		this.w_knights.clear();
		this.b_knights.clear();
		this.w_bishops.clear();
		this.b_bishops.clear();
		this.w_rooks.clear();
		this.b_rooks.clear();
		this.w_queens.clear();
		this.b_queens.clear();
		this.w_king.clear();
		this.b_king.clear();

		String currentChar; // Holds the current character in the fen

		int i = 0; // Used to go through the fen-string character by character

		int boardIndex = 112; // Keeps track of current index on the board
								// (while adding pieces)
		// Starts at "a8" (index 112) since the fen string starts on this square

		int currentStep = 0; // This will be incremented when a space is
								// detected in the string
		// 0 - Pieces
		// 1 - Side to move
		// 2 - Castling rights
		// 3 - En passant square
		// 4 - Half-moves (for 50 move rule) and full moves

		boolean fenFinished = false; // Set to true when we're at the end of
										// the fen-string
		while (!fenFinished && i < trimmedFen.length()) {
			currentChar = trimmedFen.substring(i, i + 1); // Gets the current
															// character from
															// the fen-string

			// If a space is detected, get the next character, and move to next
			// step
			if (" ".equals(currentChar)) {
				i++;
				currentChar = trimmedFen.substring(i, i + 1);
				currentStep++;
			}

			switch (currentStep) // Determine what step we're on
			{
			case 0: // Pieces
			{
				switch (currentChar.charAt(0)) // See what piece is on the
												// square
				{
				// If character is a '/' move to first file on next rank
				case '/':
					boardIndex -= 24;
					break;

				// If the character is a piece, add it and move to next square
				case 'K':
					boardArray[boardIndex] = W_KING;
					w_king.addPiece(boardIndex);
					boardIndex++;
					break;
				case 'Q':
					boardArray[boardIndex] = W_QUEEN;
					w_queens.addPiece(boardIndex);
					boardIndex++;
					break;
				case 'R':
					boardArray[boardIndex] = W_ROOK;
					w_rooks.addPiece(boardIndex);
					boardIndex++;
					break;
				case 'B':
					boardArray[boardIndex] = W_BISHOP;
					w_bishops.addPiece(boardIndex);
					boardIndex++;
					break;
				case 'N':
					boardArray[boardIndex] = W_KNIGHT;
					w_knights.addPiece(boardIndex);
					boardIndex++;
					break;
				case 'P':
					boardArray[boardIndex] = W_PAWN;
					w_pawns.addPiece(boardIndex);
					boardIndex++;
					break;
				case 'k':
					boardArray[boardIndex] = B_KING;
					b_king.addPiece(boardIndex);
					boardIndex++;
					break;
				case 'q':
					boardArray[boardIndex] = B_QUEEN;
					b_queens.addPiece(boardIndex);
					boardIndex++;
					break;
				case 'r':
					boardArray[boardIndex] = B_ROOK;
					b_rooks.addPiece(boardIndex);
					boardIndex++;
					break;
				case 'b':
					boardArray[boardIndex] = B_BISHOP;
					b_bishops.addPiece(boardIndex);
					boardIndex++;
					break;
				case 'n':
					boardArray[boardIndex] = B_KNIGHT;
					b_knights.addPiece(boardIndex);
					boardIndex++;
					break;
				case 'p':
					boardArray[boardIndex] = B_PAWN;
					b_pawns.addPiece(boardIndex);
					boardIndex++;
					break;

				// If no piece was found, it has to be a number of empty squares
				// so move to that board index
				default:
					boardIndex += Integer.parseInt(currentChar);
				}
				break;
			}
			case 1: // Side to move
			{
				if ("w".equals(currentChar))
					toMove = WHITE_TO_MOVE;
				else
					toMove = BLACK_TO_MOVE;
				break;
			}
			case 4: // Half-moves (50 move rule) and full moves
			{
				// If the next character is a space, we're done with half-moves
				// and
				// can insert them
				if (" ".equals(trimmedFen.substring(i + 1, i + 2))) {
					movesFifty = Integer.parseInt(currentChar);
				}
				// If the next character is not a space, we know it's a number
				// and since half-moves can't be higher than 50 (or it can, but
				// the game
				// is drawn so there's not much point to it), we can assume
				// there are two numbers and then we're done with half-moves.
				else {
					movesFifty = Integer.parseInt(trimmedFen
							.substring(i, i + 2));
					i++;
				}
				i += 2;
				fenFinished = true; // We're done with the fen-string and can
									// exit the loop
				break;
			}
			}
			i++; // Move to the next character in the fen-string
		}
	} // END inputFEN()
	
	/**
	 * @param index
	 *            The index to check
	 * @return The rank the index is located on (index 18 gives (18-(18%16))/16 =
	 *         rank 1)
	 */
	public static final int rank(int index) { return (index - (index % 16)) / 16; }

	/**
	 * @param index
	 *            The index to check
	 * @return The row (file) the index is located on (index 18 gives 18%16 =
	 *         row 2)
	 */
	public static final int file(int index) { return index % 16; }

	/**
	 * Returns the shortest distance between two squares
	 * 
	 * @param squareA
	 * @param squareB
	 * @return distance The distance between the squares
	 */
	public static final int distance(int squareA, int squareB) {
		return Math.max(Math.abs(file(squareA) - file(squareB)), Math.abs(rank(squareA) - rank(squareB)));
	} // END distance()
}
