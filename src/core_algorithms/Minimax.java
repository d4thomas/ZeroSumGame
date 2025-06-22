package core_algorithms;

import problems.Game;

import java.util.ArrayList;
import java.util.List;

/**
 * Implements the Minimax algorithm with alpha-beta pruning and depth-limited search.
 * <p>
 * Assumption:
 * - Human is the MAX player (typically X)
 * - AI is the MIN player (typically O)
 *
 * @param <A> the type representing a move
 */
public class Minimax<A> {
    private final Game<A> game;
    private final int maxDepth;

    /**
     * Record to store the score of a game state and the path of moves leading to it.
     */
    public record ScoreAndPath<A>(int score, List<A> pathOfMoves) {
    }

    /**
     * Creates a minimax search instance with alpha-beta pruning and a depth limit.
     *
     * @param game     the game being played
     * @param maxDepth the maximum depth to search
     *                 (e.g., 0 is root, 1 is one move ahead)
     */
    public Minimax(Game<A> game, int maxDepth) {
        this.game = game;
        this.maxDepth = maxDepth;
    }

    /**
     * Starts minimax search from the current game state and returns the
     * best move for the MIN player (AI), assuming the AI plays second.
     *
     * @return the best move for the current player to make
     */
    public A minimaxSearch() {
        // Step 1: Check for immediate tactical move
        // This is a fast check to see if there is an obvious win or threat that needs
        // to be blocked. If such a move exists, the AI will make it without running
        // the full minimax search.
        A tacticalMove = game.getTacticalMove();
        if (tacticalMove != null) {
            return tacticalMove;
        }
        // Step 2: If no urgent threat: start recursive minimax search
        ScoreAndPath<A> b = min(Integer.MIN_VALUE, Integer.MAX_VALUE, 0);
        return b.pathOfMoves().getFirst();
    }

    /**
     * MAX node in the minimax tree (trying to maximize the score).
     * <p>
     * Applies alpha-beta pruning to skip branches that cannot affect the outcome
     * (i.e., branches that would not be allowed by the MIN player).
     * <p>
     * If the depth limit is reached, it uses the heuristic evaluation instead of
     * continuing deeper.
     */
    public ScoreAndPath<A> max(int alpha, int beta, int depth) {
        if (game.isTerminal()) {
            // Base Case: If the game is over, return its final utility score
            return new ScoreAndPath<>(game.utility(), new ArrayList<>());
        } else if (depth == maxDepth) {
            // Depth Limit Reached; stop search and use heuristic evaluation
            int heuristicScore = game.heuristicEvaluation();
            return new ScoreAndPath<>(heuristicScore, new ArrayList<>());
        } else {
            List<A> bestPath = new ArrayList<>();
            // Explore all possible moves for MAX
            for (A move : game.getAllRemainingMoves()) {
                game.execute(move, true);
                ScoreAndPath<A> a = min(alpha, beta, depth + 1);
                game.undo(move, true);
                if (a.score() >= beta) {
                    // Prune Branch: Result too good for MAX player, MIN player
                    // will not allow this branch
                    return new ScoreAndPath<>(a.score, null);
                } else if (a.score > alpha) {
                    // Update the best score and move sequence if this move is better
                    // for MAX player
                    alpha = a.score();
                    bestPath = a.pathOfMoves();
                    bestPath.addFirst(move);
                }
            }
            return new ScoreAndPath<>(alpha, bestPath);
        }
    }

    /**
     * MIN node in the minimax tree (trying to minimize the score).
     * <p>
     * Applies alpha-beta pruning to skip branches that cannot affect the outcome
     * (i.e., branches that would not be allowed by the MAX player).
     * <p>
     * If the depth limit is reached, it uses the heuristic evaluation instead of
     * continuing deeper.
     */
    public ScoreAndPath<A> min(int alpha, int beta, int depth) {
        if (game.isTerminal()) {
            // Base Case: If the game is over, return its final utility score
            return new ScoreAndPath<>(game.utility(), new ArrayList<>());
        } else if (depth == maxDepth) {
            // Depth limit reached, use heuristic evaluation
            int heuristicScore = game.heuristicEvaluation();
            return new ScoreAndPath<>(heuristicScore, new ArrayList<>());
        } else {
            List<A> bestPath = new ArrayList<>();
            for (A move : game.getAllRemainingMoves()) {
                // Explore all possible moves for MIN
                game.execute(move, false);
                ScoreAndPath<A> b = max(alpha, beta, depth + 1);
                game.undo(move, false);
                if (b.score <= alpha) {
                    // Prune Branch: Result too good for MIN player,
                    // MAX player will not allow this branch
                    return new ScoreAndPath<>(b.score, null);
                } else if (b.score() < beta) {
                    // Update the best score and move sequence if this move is better for MIN
                    beta = b.score();
                    bestPath = b.pathOfMoves();
                    bestPath.addFirst(move);
                }
            }
            return new ScoreAndPath<>(beta, bestPath);
        }
    }
}
