package problems;

import java.util.List;

/**
 * A generic interface for turn-based games that can be used with minimax search.
 *
 * @param <A> the type representing a move or action in the game
 */
public interface Game<A> {
    /**
     * Returns a list of all legal moves that can be made from the current state.
     *
     * @return a list of remaining valid moves
     */
    List<A> getAllRemainingMoves();

    /**
     * Computes the utility of the current game state.
     * This value is used by minimax to evaluate terminal states.
     *
     * @return an integer utility value: higher means better for the MAX player
     */
    int utility();

    /**
     * Checks whether the game is over.
     *
     * @return true if the current state is terminal; false otherwise
     */
    boolean isTerminal();

    /**
     * Applies the given move to the current game state.
     *
     * @param move   the move to apply
     * @param isMax  true if the move is by the MAX player,
     *               false if by the MIN player
     */
    void execute(A move, boolean isMax);

    /**
     * Undoes a previously applied move, restoring the previous game state.
     *
     * @param move   the move to undo
     * @param isMax  true if the move was by the MAX player
     *               false if by the MIN player
     */
    void undo(A move, boolean isMax);

    /**
     * Provides a heuristic estimate of the current game state's favorability
     * when the game is not in a terminal state and the search is cut off
     * due to a depth limit.
     *
     * Positive values should indicate that the state favors the MAX player (X),
     * and negative values should indicate that the state favors the MIN player (O).
     *
     * This method is called when minimax search reaches the maximum allowed depth
     * and must estimate the utility of a non-terminal game state.
     *
     * @return a heuristic evaluation score of the current state
     */
    int heuristicEvaluation();

    /**
     * Provides a quick, tactical move based on simple heuristics,
     * without performing a full minimax search.
     *
     * This method is typically used when depth-limited search is not applied,
     * or as a fallback for faster decision-making in time-constrained situations.
     *
     * It returns a move that is likely to improve the player's position,
     * such as completing a winning line or blocking an opponent's winning move.
     *
     * @return a tactically chosen move based on the current game state
     */
    A getTacticalMove();

}
