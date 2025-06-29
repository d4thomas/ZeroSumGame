package solutions;

import core_algorithms.*;
import problems.Mark;
import problems.Square;
import problems.TicTacToe;

import java.util.Scanner;

/**
 * A console-based runner for playing Tic-Tac-Toe.
 * <p>
 * Assumptions:
 * The human player is 'X' (MAX player)
 * The AI is 'O' (MIN player)
 */
public class TicTacToeRunner extends Minimax<Square> {

    // The size of the board (e.g., 3 for 3x3 board)
    private static final int BOARD_SIZE = 3;
    // Depth limit for minimax search:
    // Higher values allow a deeper lookahead but increase computation time
    private static final int MAX_DEPTH = 5;
    // Specifies the player who moves first:
    // Mark.X = human, Mark.O = AI
    private Mark turn = Mark.O;
    // The game instance managing board state and rules
    private final TicTacToe game;

    public TicTacToeRunner(TicTacToe game, int maxDepth) {
        super(game, maxDepth);
        this.game = game;
    }

    /**
     * Main loop to play the game. Alternates between human (X) and AI (O) turns
     * until the game reaches a terminal state.
     * For each turn, the move is executed, and the board is updated and displayed.
     */
    public void play() {
        if (turn == Mark.X) {
            game.printBoard(null);
        }
        while (!game.isTerminal()) {
            Square newMove = null;
            if (turn == Mark.X) {
                game.execute(getUserMove(), true);
                turn = Mark.O;
            } else {
                System.out.println("\nAI's turn: (" + "\u001B[31m" + "O" +
                        "\u001B[0m" + " marks the most recent AI move)");
                newMove = minimaxSearch();
                game.execute(newMove, false);
                turn = Mark.X;
            }
            game.printBoard(newMove);
        }
        announceWinner(game.utility());
    }

    /**
     * Prompts the human player to enter a move via the console.
     * Re-prompts until a valid move is provided.
     * <p>
     * A valid move satisfies the following conditions:
     * The input includes two integers separated by space: a row and a column
     * The row and column are within the board bounds (i.e., 0 to BOARD_SIZE-1)
     * The selected square is currently unmarked
     *
     * @return the valid square chosen by the human player
     */
    private Square getUserMove() {
        int row = -1, col = -1;
        boolean validInput = false;
        Scanner scanner = new Scanner(System.in);
        while (!validInput) {
            System.out.print("\nYour turn: enter row & column separated by space: ");
            if (scanner.hasNextInt()) {
                row = scanner.nextInt();
            } else {
                scanner.next();
                continue;
            }
            if (scanner.hasNextInt()) {
                col = scanner.nextInt();
            } else {
                scanner.next();
                continue;
            }
            if (isValidMove(row, col)) {
                validInput = true;
            } else {
                System.out.println("Invalid position, please try again.");
            }
        }
        // Human player plays as X
        return new Square(row, col);
    }

    /**
     * Checks if a move is valid (within board bounds and not already marked).
     *
     * @param row the row number of the square
     * @param col the column number of the square
     * @return true if the row # and the colum # are within the board bounds and unmarked,
     * false otherwise
     */
    private boolean isValidMove(int row, int col) {
        boolean isWithinBounds = (row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE);
        return (isWithinBounds && !game.markedSquare(new Square(row, col)));
    }

    /**
     * Announces the result of the game based on the utility value.
     *
     * @param utility the final utility value:
     *                Positive = human (X) wins,
     *                Negative = AI (O) wins,
     *                Zero = draw
     */
    private void announceWinner(int utility) {
        if (utility > 0) {
            System.out.println("\nPlayer (X) wins!");
        } else if (utility < 0) {
            System.out.println("\nAI (O) wins!");
        } else {
            System.out.println("\nIt's a draw!");
        }
    }

    public static void main(String[] args) {
        TicTacToeRunner runner = new TicTacToeRunner(new TicTacToe(BOARD_SIZE), MAX_DEPTH);
        runner.play();
    }
}
