package edu.upc.epsevg.prop.hex;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.HashSet;
import java.util.Set;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Heuristica {

// Pesos iniciales
    private int weightBridge = 0;
    private int weightBlockOpponent = 0;
    private int weightEdgeTemplate = 0;
    private int weightInteriorTemplate = 0;
    private int weightFreeSpaces = 0;
    private int weightDoubleThreat = 0;
    private int weightLadder = 0;
    private int weightCentralArea = 0;
    private int weightCornerArea = 0;
    private int weightDiagonalThreat = 0;
    private double alignmentMultiplier = 0; // Aumenta el peso de movimientos alineados
    private int misalignmentPenalty = -0; // Penaliza movimientos no alineados

    private double progress; // Progreso del juego
    
    private final HexGameStatus board;

    public Heuristica(HexGameStatus board) {
        this.board = board;
    }

 public int evaluate(int player) {
        // Actualizar los pesos según el progreso del juego
        updateWeights();

        int opponent = (player == 1) ? 2 : 1;
        int score = 0;

        // Evaluar conexiones propias
        score += evaluateConnections(player);

        // Penalizar movimientos no alineados
        score += evaluateMisalignedMoves(player);

        // Evaluar amenazas del oponente
        score += evaluateThreats(player);
        score += evaluateDiagonalThreats(opponent);

        // Penalizar espacios libres del oponente
        score += calculateSpacesToConnect(player);

        // Evaluar influencia en áreas estratégicas
        score += evaluateInfluence(player);

        return score;
    }
 
    /**
    * Actualiza los pesos de la heurística según el progreso del juego.
    */
    private void updateWeights() {
        int totalSpaces = board.getSize() * board.getSize();
        int occupiedSpaces = countOccupiedSpaces();
        progress = (double) occupiedSpaces / totalSpaces; // Almacenar el progreso
        if (progress < 0.33) { // Etapa inicial
            weightBridge = 100;
            weightBlockOpponent = 0;
            weightEdgeTemplate = 0;
            weightInteriorTemplate = 0;
            weightFreeSpaces = 0;
            weightDoubleThreat = 0;
            weightLadder = 0;
            weightCentralArea = 0;
            weightCornerArea = 0;
            weightDiagonalThreat = 0;
            alignmentMultiplier = 1.5;
            misalignmentPenalty = -20;
        } else if (progress < 0.66) { // Etapa intermedia
            weightBridge = 60;
            weightBlockOpponent = 30;
            weightEdgeTemplate = 10;
            weightInteriorTemplate = 20;
            weightFreeSpaces = 20;
            weightDoubleThreat = 30;
            weightLadder = 20;
            weightCentralArea = 80;
            weightCornerArea = 10;
            weightDiagonalThreat = 50;
            alignmentMultiplier = 2.5;
            misalignmentPenalty = -30;
        } else { // Etapa final
            weightBridge = 50;
            weightBlockOpponent = 20;
            weightEdgeTemplate = 5;
            weightInteriorTemplate = 10;
            weightFreeSpaces = 10;
            weightDoubleThreat = 40;
            weightLadder = 15;
            weightCentralArea = 20;
            weightCornerArea = 5;
            weightDiagonalThreat = 60;
            alignmentMultiplier = 3.5;
            misalignmentPenalty = -20;
        }
    }

    /**
     * Cuenta el número de casillas ocupadas en el tablero.
     */
    private int countOccupiedSpaces() {
        int occupied = 0;
        for (int x = 0; x < board.getSize(); x++) {
            for (int y = 0; y < board.getSize(); y++) {
                if (board.getPos(x, y) != 0) {
                    occupied++;
                }
            }
        }
        return occupied;
    }

/**
 * Evalúa las conexiones efectivas del jugador (plantillas, puentes, etc.).
 */
private int evaluateConnections(int player) {
    int connectionScore = 0;

    for (int x = 0; x < board.getSize(); x++) {
        for (int y = 0; y < board.getSize(); y++) {
            if (board.getPos(x, y) == player) {
                // Detectar puentes siempre
                for (Point neighbor : board.getNeigh(new Point(x, y))) {
                    if (isBridge(player, x, y)) {
                        int bridgeScore = weightBridge;

                        // Si el puente está alineado, multiplica el peso
                        if (isAlignedWithGoal(player, x, y, neighbor.x, neighbor.y)) {
                            bridgeScore *= alignmentMultiplier;
                        }

                        connectionScore += bridgeScore;
                    }
                }

                // Evaluar plantillas según la etapa del juego
                if (progress >= 0.33) { // Solo en etapa intermedia o final
                    // Detectar plantillas de borde
                    if (isEdgeTemplate(player, x, y)) {
                        int edgeScore = weightEdgeTemplate;

                        // Si la plantilla está alineada, multiplica el peso
                        if (isAlignedWithGoal(player, x, y, x, y)) {
                            edgeScore *= alignmentMultiplier;
                        }

                        connectionScore += edgeScore;
                    }

                    // Detectar plantillas de interior
                    if (isInteriorTemplate(player, x, y)) {
                        int interiorScore = weightInteriorTemplate;

                        // Si la plantilla está alineada, multiplica el peso
                        if (isAlignedWithGoal(player, x, y, x, y)) {
                            interiorScore *= alignmentMultiplier;
                        }

                        connectionScore += interiorScore;
                    }
                }
            }
        }
    }

    return connectionScore;
}

boolean isBridge(int player, int x, int y) {
    // Configuraciones de puentes (piedras opuestas y espacios candidatos)
    int[][] bridgeConfigs = {
        {1, -2, 1, -1, 0, -1}, // Configuración 1
        {-1, -1, 0, -1, -1, 0}, // Configuración 2
        {2, -1, 1, -1, 1, 0}, // Configuración 3
        {-2, 1, -1, 0, -1, 1}, // Configuración 4
        {1, 1, 1, 0, 0, 1}, // Configuración 5
        {-1, 2, -1, 1, 0, 1} // Configuración 6
    };

    for (int[] config : bridgeConfigs) {
        // Piedra opuesta
        int oppX = x + config[0];
        int oppY = y + config[1];

        // Espacios candidatos
        int c1X = x + config[2];
        int c1Y = y + config[3];
        int c2X = x + config[4];
        int c2Y = y + config[5];

        // Verificar condiciones
        if (isValid(c1X, c1Y) && isValid(c2X, c2Y) && isValid(oppX, oppY)) {
            if (board.getPos(c1X, c1Y) == 0 && board.getPos(c2X, c2Y) == 0 &&
                board.getPos(oppX, oppY) == player) {
                return true; // Se detecta un puente
            }
        }
    }

    return false; // No se detectaron puentes
}


/*
Falta por revisar
*/
private boolean isEdgeTemplate(int player, int x, int y) {
    // Definir patrones de plantillas de borde específicas (e.g., A-2, A-3)
    Point[][] edgeTemplates = {
        {new Point(x - 1, y), new Point(x - 1, y + 1)}, // A-2
        {new Point(x - 2, y + 1), new Point(x - 1, y + 2)} // A-3
    };

    // Verificar cada plantilla
    for (Point[] template : edgeTemplates) {
        boolean matches = true;
        for (Point p : template) {
            if (!isValid(p.x, p.y) || board.getPos(p.x, p.y) != player) {
                matches = false;
                break;
            }
        }
        if (matches) return true;
    }

    return false;
}

/*
Falta por revisar
*/
private boolean isInteriorTemplate(int player, int x, int y) {
    // Patrones interiores como Crescent, Trapezoid
    Point[][] interiorTemplates = {
        {new Point(x - 1, y), new Point(x + 1, y), new Point(x, y - 1)}, // Trapezoid
        {new Point(x - 1, y + 1), new Point(x + 1, y - 1), new Point(x, y - 2)} // Crescent
    };

    // Verificar cada plantilla
    for (Point[] template : interiorTemplates) {
        boolean matches = true;
        for (Point p : template) {
            if (!isValid(p.x, p.y) || board.getPos(p.x, p.y) != player) {
                matches = false;
                break;
            }
        }
        if (matches) return true;
    }

    return false;
}

    /**
     * Verifica si una posición (x, y) está dentro de los límites del tablero.
     * 
     * @param x Coordenada X de la posición.
     * @param y Coordenada Y de la posición.
     * @return true si la posición está dentro de los límites del tablero, false en caso contrario.
     */
    private boolean isValid(int x, int y) {
        return x >= 0 && x < board.getSize() && y >= 0 && y < board.getSize();
    }

 private int bfsCalculateSpaces(Point start, int player, boolean[][] visited, List<Point> pathToWest, List<Point> pathToEast) {
    int size = board.getSize();
    Queue<List<Point>> queue = new LinkedList<>();
    boolean[] connectedBorders = new boolean[2]; // Para rastrear los extremos conectados (oeste, este)
    int spaces = 0;

    // Inicializar cola con el primer nodo
    List<Point> initialPath = new ArrayList<>();
    initialPath.add(start);
    queue.add(initialPath);

    while (!queue.isEmpty()) {
        List<Point> currentPath = queue.poll();
        Point current = currentPath.get(currentPath.size() - 1);

        for (Point neighbor : board.getNeigh(current)) {
            if (!isValid(neighbor.x, neighbor.y) || visited[neighbor.x][neighbor.y]) {
                continue;
            }

            int cellValue = board.getPos(neighbor.x, neighbor.y);
            List<Point> newPath = new ArrayList<>(currentPath);

            if (cellValue == 0) { // Casilla libre
                spaces++;
                visited[neighbor.x][neighbor.y] = true;
                newPath.add(neighbor);
                queue.add(newPath);
            } else if (cellValue == player) { // Ficha del jugador
                visited[neighbor.x][neighbor.y] = true;
                newPath.add(neighbor);
                queue.add(newPath);
            }

            // Verificar si alcanzamos un extremo
            int border = getBorder(neighbor, player);
            if (border != -1 && !connectedBorders[border]) {
                connectedBorders[border] = true;
                newPath.add(neighbor);

                // Guardar el camino correspondiente
                if (border == 0) { // Borde oeste
                    pathToWest.clear();
                    pathToWest.addAll(newPath);
                } else if (border == 1) { // Borde este
                    pathToEast.clear();
                    pathToEast.addAll(newPath);
                }
            }

            // Si ambos extremos están conectados, salimos del bucle
            if (connectedBorders[0] && connectedBorders[1]) {
                return spaces - 1; // No contar casilla inicial
            }
        }
    }

    return Integer.MAX_VALUE; // Si no se puede conectar
}

public int calculateSpacesToConnect(int player) {
    int minSpaces = Integer.MAX_VALUE;
    List<Point> shortestPathToWest = new ArrayList<>();
    List<Point> shortestPathToEast = new ArrayList<>();
    boolean[][] visited = new boolean[board.getSize()][board.getSize()];

    for (int x = 0; x < board.getSize(); x++) {
        for (int y = 0; y < board.getSize(); y++) {
            if (board.getPos(x, y) == player && !visited[x][y]) {
                // Realizar BFS desde la ficha del jugador
                List<Point> pathToWest = new ArrayList<>();
                List<Point> pathToEast = new ArrayList<>();
                int spaces = bfsCalculateSpaces(new Point(x, y), player, visited, pathToWest, pathToEast);

                if (spaces < minSpaces) {
                    minSpaces = spaces;
                    shortestPathToWest = new ArrayList<>(pathToWest);
                    shortestPathToEast = new ArrayList<>(pathToEast);
                }
            }
        }
    }

    // Imprimir los caminos más cortos hacia ambos extremos
    //System.out.println("Camino más corto al oeste: " + shortestPathToWest);
    //System.out.println("Camino más corto al este: " + shortestPathToEast);

    return minSpaces == Integer.MAX_VALUE ? 0 : minSpaces;
}


private int getBorder(Point p, int player) {
    int size = board.getSize();
    if (player == 1) { // Este-Oeste
        if (p.x == 0) return 0; // Borde oeste
        if (p.x == size - 1) return 1; // Borde este
    } else { // Norte-Sur
        if (p.y == 0) return 0; // Borde norte
        if (p.y == size - 1) return 1; // Borde sur
    }
    return -1; // No es un borde relevante
}

/**
 * Evalúa las amenazas dobles y control de escaleras.
 * Falta por revisar
 */
private int evaluateThreats(int player) {
    int threatScore = 0;

    for (int x = 0; x < board.getSize(); x++) {
        for (int y = 0; y < board.getSize(); y++) {
            if (board.getPos(x, y) == player) {
                // Amenazas dobles
                if (isDoubleThreat(player, x, y)) {
                    int threatValue = weightDoubleThreat;

                    // Multiplica el peso si la amenaza está alineada
                    if (isAlignedWithGoal(player, x, y, x, y)) {
                        threatValue *= alignmentMultiplier;
                    }
                    threatScore += threatValue;
                }

                // Escaleras
                if (isLadder(player, x, y)) {
                    int ladderScore = weightLadder;

                    // Multiplica el peso si la escalera está alineada
                    if (isAlignedWithGoal(player, x, y, x, y)) {
                        ladderScore *= alignmentMultiplier;
                    }
                    threatScore += ladderScore;
                }
            }

            // Evaluar amenazas del oponente
            if (board.getPos(x, y) == (player == 1 ? 2 : 1)) {
                if (isBridge(player == 1 ? 2 : 1, x, y)) {
                    threatScore -= weightBlockOpponent; // Penaliza amenazas del oponente
                }
            }
        }
    }

    return threatScore;
}

/*
Falta por revisar
*/
private boolean isDoubleThreat(int player, int x, int y) {
    int threats = 0;

    for (Point neighbor : board.getNeigh(new Point(x, y))) {
        if (board.getPos(neighbor.x, neighbor.y) == 0) { // Espacio libre
            // Verificar si el espacio está alineado con el objetivo
            if (isAlignedWithGoal(player, x, y, neighbor.x, neighbor.y)) {
                threats++;
            }
        }

        // Si hay al menos dos amenazas, es una amenaza doble
        if (threats >= 2) return true;
    }

    return false;
}

/*
Falta por revisar
*/
private int evaluateDiagonalThreats(int opponent) {
    int threatScore = 0;

    if (isDiagonalThreat(opponent)) {
        threatScore -= weightDiagonalThreat; // Penalización significativa por amenaza diagonal
    }

    return threatScore;
}

/*
Falta por revisar
*/
private boolean isDiagonalThreat(int opponent) {
    int size = board.getSize();

    // Verificar diagonal principal (superior izquierda a inferior derecha)
    for (int i = 0; i < size; i++) {
        if (board.getPos(i, i) != opponent && board.getPos(i, i) != 0) {
            break;
        }
        if (i == size - 1) return true;
    }

    // Verificar diagonal secundaria (superior derecha a inferior izquierda)
    for (int i = 0; i < size; i++) {
        if (board.getPos(i, size - 1 - i) != opponent && board.getPos(i, size - 1 - i) != 0) {
            break;
        }
        if (i == size - 1) return true;
    }

    return false;
}

/*
Falta por revisar
*/
private boolean isLadder(int player, int x, int y) {
    for (Point neighbor : board.getNeigh(new Point(x, y))) {
        if (board.getPos(neighbor.x, neighbor.y) == player) {
            // Verificar patrón en zigzag
            for (Point secondNeighbor : board.getNeigh(neighbor)) {
                if (board.getPos(secondNeighbor.x, secondNeighbor.y) == 0) {
                    return true;
                }
            }
        }
    }

    return false;
}

/**
 * Evalúa la influencia global del jugador.
 * Falta por revisar
 */
private int evaluateInfluence(int player) {
    int influenceScore = 0;

    for (int x = 0; x < board.getSize(); x++) {
        for (int y = 0; y < board.getSize(); y++) {
            if (board.getPos(x, y) == player) {
                // Ponderar si la posición está en el área central
                if (isCentralArea(x, y)) influenceScore += weightCentralArea;

                // Ponderar si la posición está en el área de esquina
                if (isCornerArea(x, y)) influenceScore += weightCornerArea;
            }
        }
    }

    return influenceScore;
}

/*
Falta por revisar
*/
private boolean isCentralArea(int x, int y) {
    int size = board.getSize();
    int centerMin = size / 3;
    int centerMax = 2 * size / 3;

    return (x >= centerMin && x <= centerMax) && (y >= centerMin && y <= centerMax);
}

/*
Falta por revisar
*/
private boolean isCornerArea(int x, int y) {
    int size = board.getSize();
    int cornerRange = size / 5; // Distancia desde las esquinas

    // Verificar esquinas
    return (x <= cornerRange && y <= cornerRange) || // Esquina superior izquierda
           (x >= size - cornerRange && y <= cornerRange) || // Esquina superior derecha
           (x <= cornerRange && y >= size - cornerRange) || // Esquina inferior izquierda
           (x >= size - cornerRange && y >= size - cornerRange); // Esquina inferior derecha
}

/**
 * Verifica si una jugada está alineada con la dirección objetivo del jugador.
 * Falta por revisar
 * 
 * @param player El jugador (1 o 2).
 * @param startX Coordenada X inicial.
 * @param startY Coordenada Y inicial.
 * @param endX Coordenada X final.
 * @param endY Coordenada Y final.
 * @return true si la jugada avanza hacia los extremos objetivo del jugador.
 */
private boolean isAlignedWithGoal(int player, int startX, int startY, int endX, int endY) {
    int size = board.getSize();
    if (player == 1) {
        // Jugador 1: Prioriza movimientos en X (este-oeste), penaliza movimientos hacia los bordes
        return Math.abs(endX - startX) > Math.abs(endY - startY) && endX > 0 && endX < size - 1;
    } else {
        // Jugador 2: Prioriza movimientos en Y (norte-sur), penaliza movimientos hacia los bordes
        return Math.abs(endY - startY) > Math.abs(endX - startX) && endY > 0 && endY < size - 1;
    }
}

/*
Falta por revisar
*/
private int evaluateMisalignedMoves(int player) {
    int misalignmentPenalty_l = 0;

    for (int x = 0; x < board.getSize(); x++) {
        for (int y = 0; y < board.getSize(); y++) {
            if (board.getPos(x, y) == player) {
                for (Point neighbor : board.getNeigh(new Point(x, y))) {
                    if (!isAlignedWithGoal(player, x, y, neighbor.x, neighbor.y)) {
                        misalignmentPenalty_l -= misalignmentPenalty; // Penalización para movimientos no alineados
                    }
                }
            }
        }
    }

    return misalignmentPenalty_l;
}

}
