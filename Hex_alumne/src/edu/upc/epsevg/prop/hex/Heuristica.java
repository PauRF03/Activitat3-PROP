package edu.upc.epsevg.prop.hex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.awt.Point;
import java.util.PriorityQueue;

/**
 *
 * @author Pau Ramos
 * @author Jia Le Chen
 */

public class Heuristica {
    
    private final HexGameStatus board;
    private final int[] dx = {-1, -1, 0, 0, 1, 1};
    private final int[] dy = {0, 1, -1, 1, -1, 0};

    public Heuristica(HexGameStatus board) {
        this.board = board;
    }

    public int evaluate(int player) {
        int opponent = -1;
        if(player == -1) opponent = 1;
        int playerDistance = shortestPath(player);
        int opponentDistance = shortestPath(opponent);
        
        // Retorna una heurística basada en la diferència de distàncies.
        return opponentDistance - playerDistance;
    }

    private int shortestPath(int player) {
        PriorityQueue<Node> pq = new PriorityQueue<>(Comparator.comparingInt(node -> node.distance));
        boolean[][] visited = new boolean[board.getSize()][board.getSize()];
        
        // Afegim els nodes inicials a la cua de prioritats.
        if (player == 1) { // Jugador 1 connecta esquerra-dreta.
            for (int i = 0; i < board.getSize(); i++) {
                if (board.getPos(i, 0) == player || board.getPos(i, 0) == 0) {
                    pq.add(new Node(new Point(i, 0), 0));
                }
            }
        } else { // Jugador 2 connecta dalt-baix.
            for (int j = 0; j < board.getSize(); j++) {
                if (board.getPos(0, j) == player || board.getPos(0, j) == 0) {
                    pq.add(new Node(new Point(0, j), 0));
                }
            }
        }

        // Executem l'algorisme de Dijkstra.
        while (!pq.isEmpty()) {
            Node current = pq.poll();

            if (visited[current.p.x][current.p.y] || board.getPos(current.p) != player) continue;
            visited[current.p.x][current.p.y] = true;

            // Comprova si hem arribat al costat oposat.
            if ((player == 1 && current.p.y == board.getSize() - 1) || (player == 2 && current.p.x == board.getSize() - 1)) {
                return current.distance;
            }

            // Recorre els veïns.
            for (int i = 0; i < 6; i++) {
                int nx = current.p.x + dx[i];
                int ny = current.p.y + dy[i];

                if (isValid(nx, ny) && !visited[nx][ny] && (board.getPos(nx, ny) == player || board.getPos(nx, ny) == 0)) {
                    pq.add(new Node(new Point(nx, ny), current.distance + 1));
                }
            }
        }

        // Si no es pot arribar al costat oposat, retorna un valor alt (per representar infinit).
        return Integer.MAX_VALUE;
    }

    private boolean isValid(int x, int y) {
        return x >= 0 && x < board.getSize() && y >= 0 && y < board.getSize();
    }

    private class Node {
        int distance;
        Point p;
        
        public Node(Point p, int distance) {
            this.p = p;
            this.distance = distance;
        }
    }
}
