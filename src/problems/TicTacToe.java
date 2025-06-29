package problems;

import java.util.*;

/**
 * Represents a generalized Tic-Tac-Toe game of any board size.
 * <p>
 * Assumptions:
 * 'X' is the MAX player (human)
 * 'O' is the MIN player (AI)
 * Board size is smaller than 100
 */
public class TicTacToe implements Game<Square> {

    // Board size, (e.g., 3 for a 3x3 board)
    private final int BOARD_SIZE;

    // Internal board representation: maps each occupied square to a mark (X or O)
    // Note: The map only contains *marked* squares
    private final Map<Square, Mark> board;

    // Utility value for winning the game from MAX player's perspective
    private final int WIN;

    // Utility value for losing the game from MAX player's perspective
    // (this is equivalent to MIN player winning the game)
    private final int LOSS;

    // Static weights that reward central square control (used in heuristic)
    private final int[][] positionWeight;

    public TicTacToe(int size) {
        this.BOARD_SIZE = size;
        this.board = new HashMap<>();
        WIN = 100 * BOARD_SIZE;
        LOSS = -WIN;
        positionWeight = new int[BOARD_SIZE][BOARD_SIZE];
        // Higher weights for center and near-center positions
        int mid = BOARD_SIZE / 2;
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                int distToCenter = Math.max(Math.abs(i - mid), Math.abs(j - mid));
                positionWeight[i][j] = BOARD_SIZE - distToCenter;
            }
        }
    }

    /**
     * Computes the utility of the current game state.
     *
     * @return WIN if X player wins,
     * LOSE if O player wins,
     * 0 otherwise (draw or unfinished game)
     */
    public int utility() {
        // Check rows
        for (int row = 0; row < BOARD_SIZE; row++) {
            int rowSum = 0;
            for (int col = 0; col < BOARD_SIZE; col++) {
                Square square = new Square(row, col);
                if (board.containsKey(square)) {
                    if (board.get(square) == Mark.X) {
                        rowSum++;
                    } else {
                        rowSum--;
                    }
                }
            }
            if (rowSum == BOARD_SIZE) {
                return WIN;
            } else if (rowSum == -BOARD_SIZE) {
                return LOSS;
            }
        }
        // Check columns
        for (int col = 0; col < BOARD_SIZE; col++) {
            int colSum = 0;
            for (int row = 0; row < BOARD_SIZE; row++) {
                Square square = new Square(row, col);
                if (board.containsKey(square)) {
                    if (board.get(square) == Mark.X) {
                        colSum++;
                    } else {
                        colSum--;
                    }
                }
            }
            if (colSum == BOARD_SIZE) {
                return WIN;
            } else if (colSum == -BOARD_SIZE) {
                return LOSS;
            }
        }
        // Check diagonal (top left to bottom right)
        int diaSum = 0;
        for (int d = 0; d < BOARD_SIZE; d++) {
            Square square = new Square(d, d);
            if (board.containsKey(square)) {
                if (board.get(square) == Mark.X) {
                    diaSum++;
                } else {
                    diaSum--;
                }
            }
        }
        if (diaSum == BOARD_SIZE) {
            return WIN;
        } else if (diaSum == -BOARD_SIZE) {
            return LOSS;
        }
        // Check diagonal (top right to bottom left)
        diaSum = 0;
        for (int d = 0; d < BOARD_SIZE; d++) {
            Square square = new Square(d, BOARD_SIZE - 1 - d);
            if (board.containsKey(square)) {
                if (board.get(square) == Mark.X) {
                    diaSum++;
                } else {
                    diaSum--;
                }
            }
        }
        if (diaSum == BOARD_SIZE) {
            return WIN;
        } else if (diaSum == -BOARD_SIZE) {
            return LOSS;
        }
        // No one has won yet; either a draw or unfinished
        return 0;
    }

    /**
     * Checks if the current state is terminal.
     *
     * @return true if a player has won or the board is full (draw),
     * false otherwise
     */
    public boolean isTerminal() {
        int utility = utility();
        // A player has won the game
        if (utility == WIN || utility == LOSS) {
            return true;
        }
        // Game is either a draw (return true) or unfinished (return false)
        return (board.size() == BOARD_SIZE * BOARD_SIZE);
    }

    /**
     * Applies a move to the board, placing an X or O.
     *
     * @param move  the square to place the mark
     * @param isMax true if it's the MAX player's move (X),
     *              false for MIN (O)
     */
    public void execute(Square move, boolean isMax) {
        if (isMax) {
            board.put(move, Mark.X);
        } else {
            board.put(move, Mark.O);
        }
    }

    /**
     * Undoes a previous move.
     *
     * @param move  the square to unmark
     * @param isMax true if the move was by the MAX player
     *              false if by the MIN player
     */
    public void undo(Square move, boolean isMax) {
        board.remove(move);
    }

    /**
     * Returns all empty (i.e., unmarked) squares on the board.
     *
     * @return a list of all empty squares
     */
    public List<Square> getAllRemainingMoves() {
        List<Square> result = new ArrayList<>();
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                Square square = new Square(row, col);
                if (!board.containsKey(square)) {
                    //this square is not marked
                    result.add(square);
                }
            }
        }
        return result;
    }

    /**
     * Checks whether the specified square is currently marked (i.e., occupied by X or O).
     *
     * @param square the square to check
     * @return true if the square has been marked,
     * false if it's still empty
     */
    public boolean markedSquare(Square square) {
        return board.containsKey(square);
    }

    /**
     * Scores a line (row, column, or diagonal) based on how many Xs or Os it contains.
     * Only unblocked lines are scored. More marks result in higher return values, as
     * follows:
     * 1 mark: 100
     * 2 marks: 200
     * 3 marks: 300, ... etc.
     * Note that any blocked line always returns a zero; X scores are positive,
     * O scores are negative.
     *
     * @param line a list of squares representing a line on the board
     * @return a positive score if the line favors X,
     * negative if it favors O,
     * 0 if blocked
     */
    private int scoreLine(List<Square> line) {
        int xCount = 0;
        int oCount = 0;

        // Count how many X and O marks appear in the line
        for (Square square : line) {
            Mark mark = board.get(square);
            if (mark == Mark.X) {
                xCount++;
            } else if (mark == Mark.O) {
                oCount++;
            }
        }
        // Blocked line: Contains both X and O
        if (xCount > 0 && oCount > 0) {
            return 0;
        }
        int markCount = Math.max(xCount, oCount);
        int score = markCount * 100; // 1 mark = 100, 2 marks = 200, ... etc.

        return xCount > 0 ? score : -score;
    }

    /**
     * Heuristic evaluation of the current board state for non-terminal nodes.
     * <p>
     * Returns a score that reflects which player has the strongest unblocked line.
     * Combines two components:
     * 1. Best unblocked line score for each player (X and O), scaled by number of marks × 100
     * 2. Positional influence based on static center-weighted bonus
     * <p>
     * The heuristic always returns a value less than the win utility,
     * so it cannot outweigh an actual win.
     *
     * @return positive result if X is favored, negative if O is favored
     */
    public int heuristicEvaluation() {
        int bestXScore = 0;
        int bestOScore = 0;
        // Check rows and columns
        for (int i = 0; i < BOARD_SIZE; i++) {
            List<Square> row = new ArrayList<>();
            List<Square> col = new ArrayList<>();
            for (int j = 0; j < BOARD_SIZE; j++) {
                row.add(new Square(i, j));
                col.add(new Square(j, i));
            }
            int rowScore = scoreLine(row);
            int colScore = scoreLine(col);
            if (rowScore > 0) {
                bestXScore = Math.max(bestXScore, rowScore);
            } else if (rowScore < 0) {
                bestOScore = Math.min(bestOScore, rowScore);
            }

            if (colScore > 0) {
                bestXScore = Math.max(bestXScore, colScore);
            } else if (colScore < 0) {
                bestOScore = Math.min(bestOScore, colScore);
            }
        }
        // Check diagonals
        List<Square> diag1 = new ArrayList<>();
        List<Square> diag2 = new ArrayList<>();
        for (int i = 0; i < BOARD_SIZE; i++) {
            diag1.add(new Square(i, i));
            diag2.add(new Square(i, BOARD_SIZE - 1 - i));
        }
        int d1Score = scoreLine(diag1);
        int d2Score = scoreLine(diag2);
        if (d1Score > 0) {
            bestXScore = Math.max(bestXScore, d1Score);
        } else if (d1Score < 0) {
            bestOScore = Math.min(bestOScore, d1Score);
        }
        if (d2Score > 0) {
            bestXScore = Math.max(bestXScore, d2Score);
        } else if (d2Score < 0) {
            bestOScore = Math.min(bestOScore, d2Score);
        }
        // Add a positional bonus for center-weighted square control
        int positionalScore = 0;
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                Square sq = new Square(i, j);
                Mark m = board.get(sq);
                if (m == Mark.X) {
                    positionalScore += positionWeight[i][j];
                } else if (m == Mark.O) {
                    positionalScore -= positionWeight[i][j];
                }
            }
        }
        return (bestXScore + bestOScore) + positionalScore;
    }

    /**
     * Returns an immediate tactical move if available:
     * Winning move for O if it exists
     * Winning move for X if it exists - O player needs to block
     * this move.
     * Otherwise, returns null
     * <p>
     * This avoids a full minimax search when urgent threats exist.
     */
    public Square getTacticalMove() {
        Square oWinMove = findLineWithNMinus1(Mark.O); // O win
        if (oWinMove != null) {
            return oWinMove;
        }
        Square xWinMove = findLineWithNMinus1(Mark.X); // must block X
        if (xWinMove != null) {
            return xWinMove;
        }
        return null; // No immediate tactic
    }

    /**
     * Searches for a line where the specified player (mark) has BOARD_SIZE−1
     * marks and one empty square. If found, returns the square to complete
     * that line. This is Used to find immediate winning or blocking moves.
     */
    private Square findLineWithNMinus1(Mark mark) {
        for (int i = 0; i < BOARD_SIZE; i++) {
            List<Square> row = new ArrayList<>();
            List<Square> col = new ArrayList<>();
            for (int j = 0; j < BOARD_SIZE; j++) {
                row.add(new Square(i, j));
                col.add(new Square(j, i));
            }
            Square rowResult = checkLineForNMinus1(row, mark);
            if (rowResult != null) return rowResult;

            Square colResult = checkLineForNMinus1(col, mark);
            if (colResult != null) return colResult;
        }
        List<Square> diag1 = new ArrayList<>();
        List<Square> diag2 = new ArrayList<>();
        for (int i = 0; i < BOARD_SIZE; i++) {
            diag1.add(new Square(i, i));
            diag2.add(new Square(i, BOARD_SIZE - 1 - i));
        }
        Square d1Result = checkLineForNMinus1(diag1, mark);
        if (d1Result != null) {
            return d1Result;
        }
        return checkLineForNMinus1(diag2, mark);
    }

    /**
     * Returns the empty square in the given line if it contains exactly
     * BOARD_SIZE−1 same marks and one empty; otherwise returns null.
     */
    private Square checkLineForNMinus1(List<Square> line, Mark mark) {
        int count = 0;
        Square empty = null;
        for (Square sq : line) {
            Mark m = board.get(sq);
            if (m == mark) {
                count++;
            } else if (m == null) {
                if (empty == null) {
                    empty = sq;
                } else {
                    return null; // More than one empty
                }
            } else {
                return null; // Contains opponent mark
            }
        }
        return (count == BOARD_SIZE - 1 && empty != null) ? empty : null;
    }

    /**
     * Print out the current game board in a neat format.
     * If the most recent move is by AI (O), highlight it in a
     * different color.
     *
     * @param newMove the most recent AI (O) move
     *                null if the most recent move is by human (X)
     */
    public void printBoard(Square newMove) {
        String RESET = "\u001B[0m";
        String RED = "\u001B[31m";
        String CYAN = "\u001B[36m";
        String YELLOW = "\u001B[33m";  // Highlight color for the last O move
        // Print column headers
        System.out.print("   ");
        for (int col = 0; col < BOARD_SIZE; col++) {
            System.out.print(" " + col + "  ");
        }
        System.out.println();
        for (int i = 0; i < BOARD_SIZE; i++) {
            // Print row number
            System.out.print(" " + i + " ");
            // Print each cell in the row
            for (int j = 0; j < BOARD_SIZE; j++) {
                Square square = new Square(i, j);
                if (board.containsKey(square)) {
                    if (board.get(square) == Mark.X) {
                        System.out.print(" " + CYAN + board.get(square) + RESET + " ");
                    } else {
                        if (square.equals(newMove)) {
                            System.out.print(" " + RED + board.get(square) + RESET + " ");
                        } else {
                            System.out.print(" " + YELLOW + board.get(square) + RESET + " ");
                        }
                    }
                } else {
                    System.out.print(" " + " " + " ");
                }
                if (j < BOARD_SIZE - 1) {
                    System.out.print("|");
                }
            }
            System.out.println();
            // Print separator line between rows
            if (i < BOARD_SIZE - 1) {
                System.out.print("   ");
                for (int j = 0; j < BOARD_SIZE; j++) {
                    System.out.print("---");
                    if (j < BOARD_SIZE - 1) {
                        System.out.print("+");
                    }
                }
                System.out.println();
            }
        }
    }
}
