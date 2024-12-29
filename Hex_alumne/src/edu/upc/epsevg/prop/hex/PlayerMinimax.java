package edu.upc.epsevg.prop.hex;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Implementación de un jugador automático que emplea el algoritmo Minimax con
 * poda alfa-beta. La heurística por defecto es trivial y debe mejorarse para un
 * mejor rendimiento.
 */
public class PlayerMinimax implements IPlayer, IAuto {

    private final int maxDepth;
    private int exploredNodesCount;
    private boolean timeout;
    private boolean iterativeDeepening;

    /**
     * Constructor del jugador minimax.
     *
     * @param maxDepth Profundidad máxima de búsqueda.
     */
    public PlayerMinimax(int maxDepth, boolean iterativeDeepening) {
        this.maxDepth = maxDepth;
        this.exploredNodesCount = 0;
        this.timeout = false;
        this.iterativeDeepening = iterativeDeepening;
    }

    @Override
    public PlayerMove move(HexGameStatus hgs) {
        Point bestMove = null;
        int bestValue = Integer.MIN_VALUE;
        exploredNodesCount = 0;

        // Obtenir tots els moviments possibles des de l'estat actual
        List<MoveNode> possibleMoves = hgs.getMoves();

        List<MoveNode> selectedMoves = new ArrayList<>();
        // Si no hi ha moviments possibles, retorna un moviment neutre.
        if (possibleMoves.isEmpty()) {
            return new PlayerMove(null, exploredNodesCount, maxDepth, SearchType.MINIMAX);
        }

        // Si iterativeDeepening està habilitat
        if (iterativeDeepening) {
            for (int depth = 1; depth <= maxDepth; depth++) {
                int currentBestValue = Integer.MIN_VALUE;
                Point currentBestMove = null;
                possibleMoves.addAll(0, selectedMoves);
                // Explora tots els moviments possibles a aquesta profunditat
                for (MoveNode move : possibleMoves) {
                    Point currentPoint = move.getPoint();

                    // Simular el moviment
                    HexGameStatus newState = new HexGameStatus(hgs);
                    newState.placeStone(currentPoint);

                    // Crida a Minimax per a aquesta profunditat
                    int value = minimax(newState, depth - 1, Integer.MIN_VALUE, Integer.MAX_VALUE, false);

                    // Actualitzar el millor moviment si trobem un de millor
                    if (value > currentBestValue) {
                        currentBestValue = value;
                        currentBestMove = currentPoint;
                    }
                }
                // Actualitza el millor moviment trobat fins ara
                if (currentBestValue > bestValue) {
                    bestValue = currentBestValue;
                    bestMove = currentBestMove;
                }
                // Comprova si hem superat el temps límit
                if (timeout) {
                    break;
                }
                if (bestMove != null && !selectedMoves.contains(new MoveNode(bestMove))) {
                    selectedMoves.add(new MoveNode(bestMove));
                }
            }
        } else {
            // Minimax normal sense iterative deepening
            for (MoveNode move : possibleMoves) {
                Point currentPoint = move.getPoint();

                // Simular el moviment
                HexGameStatus newState = new HexGameStatus(hgs);
                newState.placeStone(currentPoint);

                // Crida a Minimax
                int value = minimax(newState, maxDepth - 1, Integer.MIN_VALUE, Integer.MAX_VALUE, false);

                // Actualitzar el millor moviment si trobem un de millor
                if (value > bestValue) {
                    bestValue = value;
                    bestMove = currentPoint;
                }

                exploredNodesCount++;
            }
        }
        return new PlayerMove(bestMove, exploredNodesCount, maxDepth, SearchType.MINIMAX);
    }

    /**
     * Implementación del algoritmo Minimax con poda alfa-beta.
     *
     * @param hgs Estado del juego actual.
     * @param depth Profundidad restante de la búsqueda.
     * @param alpha Límite inferior (poda alfa).
     * @param beta Límite superior (poda beta).
     * @param isMaximizingPlayer Indica si estamos en el nivel del jugador
     * maximizador.
     * @return El valor heurístico del estado.
     */
    private int minimax(HexGameStatus hgs, int depth, int alpha, int beta, boolean isMaximizingPlayer) {
        // Caso base: juego terminado o profundidad alcanzada
        if (hgs.isGameOver() || depth == 0) {
            Heuristica h = new Heuristica(hgs);
            return h.evaluate(hgs.getCurrentPlayerColor());
        }
        timeout = false;
        List<MoveNode> possibleMoves = hgs.getMoves();
        exploredNodesCount++;
        // Si no hay movimientos, evaluamos el estado actual (posible estado terminal)
        if (possibleMoves.isEmpty()) {
            Heuristica h = new Heuristica(hgs);
            return h.evaluate(hgs.getCurrentPlayerColor());
        }

        if (isMaximizingPlayer) {
            int maxEval = Integer.MIN_VALUE;
            for (MoveNode move : possibleMoves) {
                HexGameStatus newState = new HexGameStatus(hgs);
                newState.placeStone(move.getPoint());
                int eval = minimax(newState, depth - 1, alpha, beta, false);
                maxEval = Math.max(maxEval, eval);
                alpha = Math.max(alpha, eval);
                if (beta <= alpha) {
                    break; // Poda beta
                }
                if (timeout) {
                    break;
                }
            }
            return maxEval;
        } else {
            int minEval = Integer.MAX_VALUE;
            for (MoveNode move : possibleMoves) {
                HexGameStatus newState = new HexGameStatus(hgs);
                newState.placeStone(move.getPoint());
                int eval = minimax(newState, depth - 1, alpha, beta, true);
                minEval = Math.min(minEval, eval);
                beta = Math.min(beta, eval);
                if (beta <= alpha) {
                    break; // Poda alfa
                }
                if (timeout) {
                    break;
                }
            }
            return minEval;
        }
    }

    @Override
    public void timeout() {
        timeout = true;
    }

    @Override
    public String getName() {
        return "Minimax Auto, Depth: " + maxDepth;
    }
}
