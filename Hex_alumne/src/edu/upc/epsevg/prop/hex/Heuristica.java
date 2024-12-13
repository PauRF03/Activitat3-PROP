/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package edu.upc.epsevg.prop.hex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

/**
 *
 * @author Pau Ramos
 * @author Jia Le Chen
 */
public class Heuristica {

    private HexGameStatus tauler;

    public Heuristica(HexGameStatus t) {
        this.tauler = t;
    }

    /**
     * Función de evaluación heurística del estado del juego. Actualmente
     * retorna 0, pero debería mejorarse para considerar: - Proximidad a la
     * victoria. - Cadenas conectadas. - Control del tablero.
     *
     * @return Valor heurístico del estado. Mayor es mejor para el jugador
     * maximizador.
     */
    // Funció heurística general
    public double evaluate(int player) {
        int opponent = (player == -1) ? 1 : -1;

        double longestPathPlayer = calculateLongestPath(player);
        double longestPathOpponent = calculateLongestPath(opponent);

        double proximityPlayer = calculateProximity(player);
        double proximityOpponent = calculateProximity(opponent);

        double strategicControl = calculateStrategicControl(player);

        // Ponderacions ajustables
        double w3 = 1.0, w4 = 1.0, w5 = 2.0, w6 = 3.0, w7 = 3.0;

        return w3 * proximityPlayer - w4 * proximityOpponent
                + w5 * strategicControl + w6 * longestPathPlayer - w7 * longestPathOpponent;
    }

// Modificar Dijkstra per calcular el camí més llarg
    private double calculateLongestPath(int player) {
        // Matriu de distàncies inicialitzada a valors mínims
        double[][] distance = new double[tauler.getSize()][tauler.getSize()];
        for (int i = 0; i < tauler.getSize(); i++) {
            Arrays.fill(distance[i], Double.MIN_VALUE);
        }

        // Cola de prioritats per a Dijkstra modificat
        PriorityQueue<Node> pq = new PriorityQueue<>(Comparator.comparingDouble(n -> -n.cost)); // Max-Heap

        // Afegir les peces del costat inicial del jugador
        if (player == 1) {
            for (int row = 0; row < tauler.getSize(); row++) {
                if (tauler.getPos(row, 0) == player || tauler.getPos(row, 0) == 0) { // Inclou caselles buides
                    pq.add(new Node(row, 0, 1));
                    distance[row][0] = 1;
                }
            }
        } else {
            for (int col = 0; col < tauler.getSize(); col++) {
                if (tauler.getPos(0, col) == player || tauler.getPos(0, col) == 0) { // Inclou caselles buides
                    pq.add(new Node(0, col, 1));
                    distance[0][col] = 1;
                }
            }
        }

        double maxPath = 0;

        // Dijkstra modificat per maximitzar el camí més llarg
        while (!pq.isEmpty()) {
            Node current = pq.poll();

            // Si arriba al costat oposat, actualitzar el camí més llarg
            if ((player == 1 && current.col == tauler.getSize() - 1)
                    || (player == 2 && current.row == tauler.getSize() - 1)) {
                maxPath = Math.max(maxPath, current.cost);
            }

            // Explorar veïns
            for (Node neighbor : getNeighbors(current.row, current.col)) {
                int newRow = neighbor.row;
                int newCol = neighbor.col;

                if (tauler.getPos(newRow, newCol) == player || tauler.getPos(newRow, newCol) == 0) { // Caselles pròpies o buides
                    double newCost = current.cost + 1; // Incrementar el cost del camí
                    if (newCost > distance[newRow][newCol]) {
                        distance[newRow][newCol] = newCost;
                        pq.add(new Node(newRow, newCol, newCost));
                    }
                }
            }
        }

        return maxPath;
    }

    // Calcula la proximitat al costat objectiu
    private double calculateProximity(int player) {
        double totalProximity = 0;
        int count = 0;

        for (int row = 0; row < tauler.getSize(); row++) {
            for (int col = 0; col < tauler.getSize(); col++) {
                if (tauler.getPos(row, col) == player) {
                    if (player == 1) {
                        totalProximity += (tauler.getSize() - 1 - col); // Distància al costat dret
                    } else {
                        totalProximity += (tauler.getSize() - 1 - row); // Distància al costat inferior
                    }
                    count++;
                }
            }
        }

        return count > 0 ? totalProximity / count : Double.MAX_VALUE;
    }

    // Calcula el control estratègic (centralitat)
    private double calculateStrategicControl(int player) {
        double score = 0;
        int center = tauler.getSize() / 2;

        for (int row = 0; row < tauler.getSize(); row++) {
            for (int col = 0; col < tauler.getSize(); col++) {
                if (tauler.getPos(row, col) == player) {
                    score += 1.0 / (Math.abs(row - center) + Math.abs(col - center) + 1);
                }
            }
        }

        return score;
    }

    // Retorna els veïns d'una cel·la
    private List<Node> getNeighbors(int row, int col) {
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}, {-1, 1}, {1, -1}};
        List<Node> neighbors = new ArrayList<>();

        for (int[] dir : directions) {
            int newRow = row + dir[0];
            int newCol = col + dir[1];
            if (newRow >= 0 && newRow < tauler.getSize() && newCol >= 0 && newCol < tauler.getSize()) {
                neighbors.add(new Node(newRow, newCol, 0));
            }
        }

        return neighbors;
    }

    // Classe auxiliar per Dijkstra
    static class Node {

        int row, col;
        double cost;

        Node(int row, int col, double cost) {
            this.row = row;
            this.col = col;
            this.cost = cost;
        }
    }
}
