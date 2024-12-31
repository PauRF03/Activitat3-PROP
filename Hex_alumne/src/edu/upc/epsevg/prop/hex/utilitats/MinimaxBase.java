package edu.upc.epsevg.prop.hex.utilitats;

import edu.upc.epsevg.prop.hex.HexGameStatus;
import edu.upc.epsevg.prop.hex.MoveNode;
import edu.upc.epsevg.prop.hex.PlayerType;

import java.awt.*;
import java.util.List;

/**
 * Classe que implementa l'algorisme Minimax.
 * Proporciona funcionalitats bàsiques de MiniMax amb poda alfa-beta i ordenació
 * de moviments basada en la heurística dissenyada.
 */
public abstract class MinimaxBase {

    protected boolean timeout;
    protected int maxDepth;
    protected long exploredNodes;
    protected final Heuristica heuristica = new Heuristica();

    /**
     * Constructor de la classe MinimaxBase.
     *
     * @param maxDepth La profunditat màxima de cerca.
     */
    public MinimaxBase(int maxDepth) {
        timeout = false;
        this.maxDepth = maxDepth;
    }

    public abstract Point millorMoviment(HexGameStatus status);

    public abstract void timeout();

    public abstract long getNodesExplorats();

    public abstract int getProfunditatMaxima();

    /**
     * Calcula la millor puntuació per a un estat donat del joc utilitzant
     * l'algorisme Minimax amb poda alfa-beta.
     *
     * @param tauler L'estat actual del joc.
     * @param profunditat La profunditat restant de la cerca.
     * @param alfa El valor alfa per a la poda alfa-beta.
     * @param beta El valor beta per a la poda alfa-beta.
     * @param maximitzant Indica si és el torn del jugador maximitzador.
     * @param player El jugador.
     *
     * @return La millor puntuació calculada per a l'estat donat.
     */
    protected int getMillorResultat(HexGameStatus tauler, int profunditat, int alfa, int beta, boolean maximitzant, PlayerType player) {
        if (timeout) return 0;

        if (profunditat == 0) {
            exploredNodes++;
            return heuristica.eval(tauler, player);
        }

        int millorRes = maximitzant ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        
        List<MoveNode> moveList = tauler.getMoves();
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

        for (int i = 0; i < 30 && i < moveList.size(); ++i) {
            if (timeout) return 0;
            MoveNode mn = moveList.get(i);
            HexGameStatus nouTauler = new HexGameStatus(tauler);
            nouTauler.placeStone(mn.getPoint());
            if (nouTauler.isGameOver()) return maximitzant ? Integer.MAX_VALUE : Integer.MIN_VALUE;

            int res = getMillorResultat(nouTauler, profunditat - 1, alfa, beta, !maximitzant, player);

            if (timeout) return 0;

            millorRes = maximitzant ? Math.max(millorRes, res) : Math.min(millorRes, res);
            if (maximitzant) alfa = Math.max(alfa, millorRes);
            else beta = Math.min(beta, millorRes);

            if (beta <= alfa) break;
        }
        return millorRes;
    }
}
