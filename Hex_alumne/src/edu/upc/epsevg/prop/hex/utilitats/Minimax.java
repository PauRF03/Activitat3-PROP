package edu.upc.epsevg.prop.hex.utilitats;

import edu.upc.epsevg.prop.hex.HexGameStatus;
import edu.upc.epsevg.prop.hex.MoveNode;
import edu.upc.epsevg.prop.hex.PlayerType;

import java.awt.*;
import java.util.List;

/**
 * Implementació bàsica de l'algorisme Minimax per trobar la millor jugada.
 */
public class Minimax extends MinimaxBase {

    /**
     * Constructor de la classe Minimax.
     *
     * @param maxDepth La profunditat màxima per a l'algorisme MiniM+max.
     */
    public Minimax(int maxDepth) {
        super(maxDepth);
    }

    /**
     * Troba la millor jugada utilitzant l'algorisme Minimax.
     *
     * @param tauler L'estat actual de la partida.
     * @return El punt del tauler corresponent a la millor jugada calculada.
     */
    @Override
    public Point millorMoviment(HexGameStatus tauler) {
        exploredNodes = 0;
        PlayerType player = tauler.getCurrentPlayer();
        List<MoveNode> moveList = tauler.getMoves();
        int bestScore = Integer.MIN_VALUE;

        moveList.sort((a, b) -> {
            HexGameStatus tauler1 = new HexGameStatus(tauler);
            tauler1.placeStone(a.getPoint());
            HexGameStatus tauler2 = new HexGameStatus(tauler);
            tauler2.placeStone(b.getPoint());
            PlayerType currentplayer = tauler.getCurrentPlayer();
            int valor1 = heuristica.eval(tauler1, currentplayer);
            int valor2 = heuristica.eval(tauler2, currentplayer);
            return Integer.compare(valor1, valor2);
        });
        Point moviment = moveList.get(0).getPoint();

        for (int i = 0; i < moveList.size() && i < 30; ++i) {
            MoveNode mn = moveList.get(i);
            HexGameStatus nouTauler = new HexGameStatus(tauler);
            nouTauler.placeStone(mn.getPoint());

            if (nouTauler.isGameOver()) return mn.getPoint();

            int score = getMillorResultat(nouTauler, maxDepth - 1, bestScore, Integer.MAX_VALUE, false, player);

            if (score > bestScore) {
                bestScore = score;
                moviment = mn.getPoint();
            }
        }
        return moviment;
    }

    @Override
    public void timeout() {
        // Si no és iteratiu, no s'ha de fer cap cosa quan salta el timeout 
    }

    /**
     * Obté el nombre total de nodes explorats durant la cerca.
     *
     * @return El nombre de nodes explorats.
     */
    @Override
    public long getNodesExplorats() {
        return exploredNodes;
    }

    /**
     * Obté la profunditat màxima configurada per a l'algorisme.
     *
     * @return La profunditat màxima de cerca.
     */
    @Override
    public int getProfunditatMaxima() {
        return maxDepth;
    }
}
