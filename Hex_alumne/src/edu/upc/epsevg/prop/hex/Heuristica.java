/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package edu.upc.epsevg.prop.hex;

/**
 *
 * @author Pau Ramos
 * @author Jia Le Chen
 */
public class Heuristica {
    private HexGameStatus tauler;
    
    Heuristica(HexGameStatus t){
        this.tauler = t;
    }
    
    /**
     * Función de evaluación heurística del estado del juego.
     * Actualmente retorna 0, pero debería mejorarse para considerar:
     * - Proximidad a la victoria.
     * - Cadenas conectadas.
     * - Control del tablero.
     *
     * @param hgs Estado del juego a evaluar.
     * @return Valor heurístico del estado. Mayor es mejor para el jugador maximizador.
     */
    int evaluate(){
        return 0;
    }
}
