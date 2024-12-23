package edu.upc.epsevg.prop.hex;

import java.awt.Point;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Implementación de un jugador automático que emplea el algoritmo Minimax con poda alfa-beta.
 * La heurística por defecto es trivial y debe mejorarse para un mejor rendimiento.
 */
public class PlayerMinimax implements IPlayer, IAuto {
    private final int maxDepth;
    private final Map<Double, HexGameStatus> jugades; //Obtenir la clau creant un tauler de pesos i utilitzar els pesos de buits per deduir quin tauler és.
    private int exploredNodesCount;
    private boolean timeout;

    /**
     * Constructor del jugador minimax.
     * @param maxDepth Profundidad máxima de búsqueda.
     */
    public PlayerMinimax(int maxDepth) {
        this.maxDepth = maxDepth;
        this.jugades = new HashMap<>();
        this.exploredNodesCount = 0;
        this.timeout = false;
    }

    @Override
    public PlayerMove move(HexGameStatus hgs) {
        Point bestMove = null;
        int bestValue = Integer.MIN_VALUE;
        exploredNodesCount = 0;

        // Obtener todos los movimientos posibles desde el estado actual
        List<MoveNode> possibleMoves = hgs.getMoves();

        // Si no hay movimientos posibles, retornar un movimiento con valor neutro.
        // Esto depende de la lógica del juego: si no hay movimientos, ¿es un estado terminal?
        if (possibleMoves.isEmpty()) {
            return new PlayerMove(null, exploredNodesCount, maxDepth, SearchType.MINIMAX);
        }

        // Para cada movimiento posible, evaluamos el resultado a través de minimax
        for (MoveNode move : possibleMoves) {
            Point currentPoint = move.getPoint();

            // Simular el movimiento
            HexGameStatus newState = new HexGameStatus(hgs);
            newState.placeStone(currentPoint);

            // Llamamos a minimax para evaluar la calidad de este movimiento
            int value = minimax(newState, maxDepth - 1, Integer.MIN_VALUE, Integer.MAX_VALUE, false);

            // Actualizar el mejor movimiento si encontramos uno de mayor valor
            if (value > bestValue) {
                bestValue = value;
                bestMove = currentPoint;
            }

            exploredNodesCount++;
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
     * @param isMaximizingPlayer Indica si estamos en el nivel del jugador maximizador.
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
                if(timeout) break;
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
                if(timeout) break;
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