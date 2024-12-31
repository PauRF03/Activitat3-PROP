package edu.upc.epsevg.prop.hex.utilitats;

import edu.upc.epsevg.prop.hex.HexGameStatus;
import edu.upc.epsevg.prop.hex.MoveNode;
import edu.upc.epsevg.prop.hex.PlayerType;

import java.awt.*;
import java.util.List;

/**
 * Implementació de l'algorisme Minimax amb cerca iterativa per trobar la
 * millor jugada.
 */
public class MinimaxIteratiu extends MinimaxBase {

    private int profActual;

    /**
     * Constructor de la classe Iterative.
     *
     * @param maxDepth La profunditat màxima de cerca per l'algoritme.
     */
    public MinimaxIteratiu(int maxDepth) {
        super(maxDepth);
    }

    /**
     * Troba la millor jugada utilitzant una aproximació iterativa del Minimax
     * amb poda alpha-beta.
     *
     * @param status L'estat actual del joc de Hex.
     * @return El punt del tauler corresponent a la millor jugada calculada.
     */
    @Override
    public Point millorMoviment(HexGameStatus status) {
        exploredNodes = 0;
        PlayerType player = status.getCurrentPlayer();
        List<MoveNode> moveList = status.getMoves();

        moveList.sort((a, b) -> {
            HexGameStatus tauler1 = new HexGameStatus(status);
            tauler1.placeStone(a.getPoint());
            HexGameStatus tauler2 = new HexGameStatus(status);
            tauler2.placeStone(b.getPoint());
            PlayerType currentplayer = status.getCurrentPlayer();
            int valor1 = heuristica.eval(tauler1, currentplayer);
            int valor2 = heuristica.eval(tauler2, currentplayer);
            return Integer.compare(valor1, valor2);
        });
        
        Point moviment = moveList.get(0).getPoint();
        Point res = moviment;
        for (int profunditat = 1; profunditat <= maxDepth && !timeout; profunditat++) {
            int bestScore = Integer.MIN_VALUE;
            for (int i = 0; i < 30 && i < moveList.size(); ++i) {
                if (timeout) break;
                MoveNode mn = moveList.get(i);
                HexGameStatus newStatus = new HexGameStatus(status);
                newStatus.placeStone(mn.getPoint());

                if (newStatus.isGameOver()) return mn.getPoint();

                int score = getMillorResultat(newStatus, profunditat - 1, bestScore, Integer.MAX_VALUE, false, player);
                if (score > bestScore) {
                    bestScore = score;
                    moviment = mn.getPoint();
                }
            }

            if (!timeout) {
                profActual = profunditat;
                res = moviment;
            }
        }

        timeout = false;
        return res;
    }

    /**
     * Atura l'execució de l'algorisme.
     */
    @Override
    public void timeout() {
        this.timeout = true;
    }

    /**
     * Obté el nombre total de nodes explorats fins al moment.
     *
     * @return El nombre de nodes explorats.
     */
    @Override
    public long getNodesExplorats() {
        return exploredNodes;
    }

    /**
     * Obté la profunditat màxima de cerca assolida durant la cerca iterativa.
     *
     * @return La profunditat màxima explorada.
     */
    @Override
    public int getProfunditatMaxima() {
        return profActual;
    }
}
