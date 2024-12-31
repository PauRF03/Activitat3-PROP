package edu.upc.epsevg.prop.hex.players;

import edu.upc.epsevg.prop.hex.*;
import edu.upc.epsevg.prop.hex.utilitats.*;

/**
 * Implementació de HEXercici per al joc de Hex. Aquest jugador utilitza
 * un algorisme MiniMax amb o sense IDS.
 */
public class HEXercici implements IPlayer, IAuto {

    private final boolean iteratiu;
    MinimaxBase MiniMax;

    /**
     * Constructor de la classe HEXercici.
     *
     * @param maxDepth La profunditat màxima de cerca.
     * @param iteratiu Indica si s'utilitza IDS.
     */
    public HEXercici(int maxDepth, boolean iteratiu) {
        this.iteratiu = iteratiu;
        MiniMax = iteratiu ? new MinimaxIteratiu(maxDepth) : new Minimax(maxDepth);
    }

    /**
     * Genera el següent moviment del jugador utilitzant l'algoritme MiniMax.
     *
     * @param hexGameStatus L'estat actual del joc de Hex.
     * @return Un objecte {@link PlayerMove} que conté el moviment calculat , els nodes explorats i la profunditat.
     */
    @Override
    public PlayerMove move(HexGameStatus hexGameStatus) {
        return new PlayerMove(MiniMax.millorMoviment(hexGameStatus), MiniMax.getNodesExplorats(), MiniMax.getProfunditatMaxima(), iteratiu ? SearchType.MINIMAX_IDS : SearchType.MINIMAX);
    }

    /**
     * Atura l'algorisme MiniMax si salta el timeout.
     */
    @Override
    public void timeout() {
        MiniMax.timeout();
    }

    /**
     * Retorna el nom del jugador.
     *
     * @return Nom del jugador.
    */
    @Override
    public String getName() {
        return "HEXercici";
    }

}
