package edu.upc.epsevg.prop.hex.utilitats;

import edu.upc.epsevg.prop.hex.HexGameStatus;
import edu.upc.epsevg.prop.hex.PlayerType;
import java.awt.Point;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;

/**
 * Implementació de l'heurística pel joc Hex
 */
public class Heuristica {

// Pesos inicials
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
    private double alignmentMultiplier = 0; // Aumenta el pes de movimientos alineados
    private int misalignmentPenalty = 0; // Penalitza moviments no alineats

    private double progres; // Progrés del joc

    /**
     * Constructora
     */
    public Heuristica() {

    }

    /**
     * Funció que avalua un tauler
     * @param board tauler a avaluar
     * @param player jugador que toca que posi fitxa
     * @return valor pel tauler avaluat
     */
    public int eval(HexGameStatus board, PlayerType player) {
        // Actualitzar els pesos segons el progrés de la partida
        updateWeights(board);

        int opponent = (PlayerType.getColor(player) == 1) ? 2 : 1;
        int puntuacio = 0;

        // Avaluar connexions pròpies
        puntuacio += evaluateConnections(board, PlayerType.getColor(player));

        // Penalitzar moviments no alineats
        puntuacio += evaluateMisalignedMoves(board, PlayerType.getColor(player));

        // Avaluar amenaces de l'oponent
        puntuacio += evaluateThreats(board, PlayerType.getColor(player));
        puntuacio += evaluateDiagonalThreats(board, opponent);

        // Penalitzar espais lliures de l'oponent
        puntuacio -= calculateDistanceToVictory(board, PlayerType.getColor(player)) * weightFreeSpaces;

        // Avaluar influència en àrees estratègiques
        puntuacio += evaluateInfluence(board, PlayerType.getColor(player));

        return puntuacio;
    }

    /**
     * Actualitza els pesos de l'heurística segons el progrés del joc.
     * @param board tauler actual
     */
    private void updateWeights(HexGameStatus board) {
        int totalSpaces = board.getSize() * board.getSize();
        int occupiedSpaces = countOccupiedSpaces(board);
        progres = (double) occupiedSpaces / totalSpaces;

        if (progres < 0.33) { // Etapa inicial
            weightBridge = 50;
            weightBlockOpponent = 20;
            weightEdgeTemplate = 10;
            weightInteriorTemplate = 20;
            weightFreeSpaces = 40;
            weightDoubleThreat = 10;
            weightLadder = 15;
            weightCentralArea = 60;
            weightCornerArea = 10;
            weightDiagonalThreat = 20;
            alignmentMultiplier = 1.5;
            misalignmentPenalty = -10;
        } else if (progres < 0.66) { // Etapa intermèdia
            weightBridge = 60;
            weightBlockOpponent = 50;
            weightEdgeTemplate = 30;
            weightInteriorTemplate = 50;
            weightFreeSpaces = 30;
            weightDoubleThreat = 40;
            weightLadder = 25;
            weightCentralArea = 40;
            weightCornerArea = 15;
            weightDiagonalThreat = 40;
            alignmentMultiplier = 2.0;
            misalignmentPenalty = -20;
        } else { // Etapa final
            weightBridge = 70;
            weightBlockOpponent = 80;
            weightEdgeTemplate = 50;
            weightInteriorTemplate = 30;
            weightFreeSpaces = 20;
            weightDoubleThreat = 60;
            weightLadder = 30;
            weightCentralArea = 20;
            weightCornerArea = 20;
            weightDiagonalThreat = 50;
            alignmentMultiplier = 2.5;
            misalignmentPenalty = -30;
        }
    }

    /**
     * Compta el nombre de caselles ocupades.
     *
     * @param board tauler actual
     * @return nombre de caselles ocupades
     */
    private int countOccupiedSpaces(HexGameStatus board) {
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
     * Avalua les connexions efectives del jugador (plantilles, ponts, etc.).
     * @param board tauler del torn
     * @param player color del jugador que ha de col·locar fitxa
     * @return valor segons les plantilles que es troben al tauler
     */
    private int evaluateConnections(HexGameStatus board, int player) {
        int connectionScore = 0;

        for (int x = 0; x < board.getSize(); x++) {
            for (int y = 0; y < board.getSize(); y++) {
                if (board.getPos(x, y) == player) {
                    // Detectar ponts sempre
                    for (Point neighbor : board.getNeigh(new Point(x, y))) {
                        if (isBridge(board, player, x, y)) {
                            int bridgeScore = weightBridge;

                            // Si el pont està alineat, multiplica el pes
                            if (isAlignedWithGoal(player, x, y, neighbor.x, neighbor.y)) {
                                connectionScore += weightBridge * alignmentMultiplier;
                            }

                            connectionScore += bridgeScore;
                        }
                    }

                    // Avaluar plantilles segons l'etapa del joc
                    if (progres >= 0.33) { // Només en etapa intermèdia o final
                        // Detectar plantilles de la vora
                        if (isEdgeTemplate(board, player, x, y)) {
                            int edgeScore = weightEdgeTemplate;

                            // Si la plantilla està alineada, multiplica el pes
                            if (isAlignedWithGoal(player, x, y, x, y)) {
                                edgeScore *= alignmentMultiplier;
                            }

                            connectionScore += edgeScore;
                        }

                        // Detectar plantilles d'interior
                        if (isInteriorTemplate(board, player, x, y)) {
                            int interiorScore = weightInteriorTemplate;

                            // Si la plantilla està alineada, multiplica el pes
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

    /**
     * Comprova si segueix algun patró de pont
     * @param board tauler actual
     * @param player color del jugador que ha de col·locar fitxa
     * @param x coordenada x de la casella
     * @param y coordenada y de la casella
     * @return si segueix la configuració pont
     */
    private boolean isBridge(HexGameStatus board, int player, int x, int y) {
        // Configuracions de ponts (pedres oposades i espais candidats)
        int[][] bridgeConfigs = {
            {1, -2, 1, -1, 0, -1}, // Configuració 1
            {-1, -1, 0, -1, -1, 0}, // Configuració 2
            {2, -1, 1, -1, 1, 0}, // Configuració 3
            {-2, 1, -1, 0, -1, 1}, // Configuració 4
            {1, 1, 1, 0, 0, 1}, // Configuració 5
            {-1, 2, -1, 1, 0, 1} // Configuració 6
        };

        for (int[] config : bridgeConfigs) {
            // Pedra oposada
            int oppX = x + config[0];
            int oppY = y + config[1];

            // Espais candidats
            int c1X = x + config[2];
            int c1Y = y + config[3];
            int c2X = x + config[4];
            int c2Y = y + config[5];

            // Verificar condicions
            if (isValid(board, c1X, c1Y) && isValid(board, c2X, c2Y) && isValid(board, oppX, oppY)) {
                if (board.getPos(c1X, c1Y) == 0 && board.getPos(c2X, c2Y) == 0
                        && board.getPos(oppX, oppY) == player) {
                    return true; // Es detecta un pont
                }
            }
        }

        return false; // No s'han detectat ponts
    }

    /**
     * 
     * @param board tauler del torn
     * @param player color del jugador que ha posar fitxa
     * @param x coordenada x de la casella
     * @param y coordenada y de la casella
     * @return si una casella forma part d'un patró d'un extrem del tauler
     */
    private boolean isEdgeTemplate(HexGameStatus board, int player, int x, int y) {
        // Definir patrons de la vora específics per a un tauler hexagonal
        int[][] edgeTemplates = {
            {-1, 0, 0, 1}, // Dues caselles en línia horitzontal cap a la dreta
            {0, -1, 1, 0}, // Dues caselles en línia diagonal superior dreta
            {1, -1, 1, 0} // Dues caselles en línia diagonal inferior dreta
        };

        // Verificar cada plantilla
        for (int[] template : edgeTemplates) {
            int x1 = x + template[0];
            int y1 = y + template[1];
            int x2 = x + template[2];
            int y2 = y + template[3];

            // Verificar si ambdues caselles estan dins del tauler i són del jugador
            if (isValid(board, x1, y1) && isValid(board, x2, y2)) {
                if (board.getPos(x1, y1) == player && board.getPos(x2, y2) == player) {
                    return true; // La plantilla coincideix
                }
            }
        }

        return false; // Cap plantilla coincideix
    }

    /**
     * 
     * @param board tauler del torn
     * @param player color del jugador que ha de tirar
     * @param x coordenada x de la casella
     * @param y coordenada y de la casella
     * @return si la casella segueix un patró intern
     */
    private boolean isInteriorTemplate(HexGameStatus board, int player, int x, int y) {
        // Definir patrons d'interior específics per a un tauler hexagonal
        int[][][] interiorTemplates = {
            { // Trapezoid: Casella central envoltada per tres veïnes
                {0, -1}, {1, 0}, {0, 1}
            },
            { // Crescent: Dos veïnes en diagonal superior i inferior
                {-1, 1}, {1, -1}, {0, -2}
            },
            { // Diamond: Casella envoltada per quatre veïnes en forma de rombe
                {0, -1}, {-1, 0}, {0, 1}, {1, 0}
            }
        };

        // Verificar cada plantilla
        for (int[][] template : interiorTemplates) {
            boolean matches = true;

            for (int[] offset : template) {
                int nx = x + offset[0];
                int ny = y + offset[1];

                // Verificar si la casella està dins dels límits i pertany al jugador
                if (!isValid(board, nx, ny) || board.getPos(nx, ny) != player) {
                    matches = false;
                    break;
                }
            }

            if (matches) {
                return true; // La plantilla coincideix
            }
        }

        return false; // Cap plantilla coincideix
    }

    /**
     * Verifica si una posició (x, y) està dins dels límits del tauler.
     *
     * @param board tauler del torn
     * @param x Coordenada X de la posició.
     * @param y Coordenada Y de la posició.
     * @return true si la posició està dins dels límits del tauler, false en cas
     * contrari.
     */
    private boolean isValid(HexGameStatus board, int x, int y) {
        return x >= 0 && x < board.getSize() && y >= 0 && y < board.getSize();
    }

    /** Avalua les amenaces dobles i control d'escales.
     * 
     * @param board tauler del torn
     * @param player color del jugador
     * @return resultat de les amenaçes del tauler
     */
    private int evaluateThreats(HexGameStatus board, int player) {
        int threatScore = 0;
        int opponent = (player == 1) ? 2 : 1;

        for (int x = 0; x < board.getSize(); x++) {
            for (int y = 0; y < board.getSize(); y++) {
                if (board.getPos(x, y) == player) {
                    // Amenaces dobles
                    if (isDoubleThreat(board, player, x, y)) {
                        int doubleThreatScore = weightDoubleThreat;

                        // Multiplica el pes si l'amenaça està alineada amb l'objectiu
                        if (isAlignedWithGoal(player, x, y, x, y)) {
                            doubleThreatScore *= alignmentMultiplier;
                        }

                        threatScore += doubleThreatScore;
                    }

                    // Escales (patrons en zig-zag)
                    if (isLadder(board, player, x, y)) {
                        int ladderScore = weightLadder;

                        // Multiplica el pes si l'escala està alineada amb l'objectiu
                        if (isAlignedWithGoal(player, x, y, x, y)) {
                            ladderScore *= alignmentMultiplier;
                        }

                        threatScore += ladderScore;
                    }
                }

                // Amenaces de l'oponent
                if (board.getPos(x, y) == opponent) {
                    // Penalització per ponts de l'oponent
                    if (isBridge(board, opponent, x, y)) {
                        threatScore -= weightBlockOpponent;
                    }
                }
            }
        }
        return threatScore;
    }

    /**
     * Verifica si una casella genera almenys dues amenaces.
     * @param board tauler del torn
     * @param player color del jugador que ha de tirar
     * @param x coordenada x de la casella
     * @param y coordenada y de la casella
     * @return si una casella genera dues amenaçes mínim
     */
    private boolean isDoubleThreat(HexGameStatus board, int player, int x, int y) {
        // Comptador per a les amenaces
        int threats = 0;

        // Direccions possibles en un tauler hexagonal
        int[][] directions = {
            {1, 0}, {0, 1}, {-1, 1}, {-1, 0}, {0, -1}, {1, -1}
        };

        // Recórrer les direccions des de la casella (x, y)
        for (int[] dir : directions) {
            int nx = x + dir[0];
            int ny = y + dir[1];

            // Verificar si la posició veïna està dins dels límits i és buida
            if (isValid(board, nx, ny) && board.getPos(nx, ny) == 0) {
                // Si és una casella buida, compta com una possible amenaça
                threats++;

                // Si trobem almenys dues amenaces, retornem true
                if (threats >= 2) return true;
            }
        }
        return false;
    }

    private int evaluateDiagonalThreats(HexGameStatus board, int opponent) {
        int threatScore = 0;

        if (isDiagonalThreat(board, opponent, true)) { // Diagonal principal
            threatScore -= weightDiagonalThreat;
        }

        if (isDiagonalThreat(board, opponent, false)) { // Diagonal secundària
            threatScore -= weightDiagonalThreat;
        }

        return threatScore;
    }

    /**
     * Verifica si una diagonal específica està completament ocupada per fitxes
     * de l'oponent o està buida, fet que representa una amenaça diagonal.
     *
     * @param board tauler del torn
     * @param opponent El jugador oponent (1 o 2).
     * @param isMainDiagonal true per la diagonal principal, false per la
     * diagonal secundària.
     * @return true si la diagonal representa una amenaça, false en cas
     * contrari.
     */
    private boolean isDiagonalThreat(HexGameStatus board, int opponent, boolean isMainDiagonal) {
        int size = board.getSize();

        for (int i = 0; i < size; i++) {
            int x = i;
            int y = isMainDiagonal ? i : size - 1 - i; // Coordenades segons la diagonal

            int cellState = board.getPos(x, y);

            // Si trobem una casella del jugador o una bloquejada, no és una amenaça
            if (cellState != opponent && cellState != 0) {
                return false;
            }
        }

        return true; // Totes les caselles compleixen la condició d'amenaça
    }

    /**
     * Verifica si existeix un patró de "escala" (zig-zag) a partir d'una
     * posició donada.
     * @param board tauler del torn
     * @param player El jugador (1 o 2).
     * @param x Coordenada X de la posició inicial.
     * @param y Coordenada Y de la posició inicial.
     * @return true si existeix una escala, false en cas contrari.
     */
    private boolean isLadder(HexGameStatus board, int player, int x, int y) {
        // Direccions possibles en un tauler hexagonal
        int[][] directions = {
            {1, 0}, {0, 1}, {-1, 1}, {-1, 0}, {0, -1}, {1, -1}
        };

        // Verificar si hi ha un patró d'escala
        for (int[] dir1 : directions) {
            int neighborX1 = x + dir1[0];
            int neighborY1 = y + dir1[1];

            if (isValid(board, neighborX1, neighborY1) && board.getPos(neighborX1, neighborY1) == player) {
                for (int[] dir2 : directions) {
                    if (dir2 != dir1) { // Evitar tornar a la mateixa direcció
                        int neighborX2 = neighborX1 + dir2[0];
                        int neighborY2 = neighborY1 + dir2[1];

                        if (isValid(board, neighborX2, neighborY2) && board.getPos(neighborX2, neighborY2) == 0) {
                            return true; // Escala detectada
                        }
                    }
                }
            }
        }

        return false; // No s'ha trobat cap escala
    }

    /**
     * Avalua la influència global del jugador. Falta revisar
     * @param board tauler del torn
     * @param player color del jugador que ha de tirar
     * @return puntuació d'influencia (segons si l'area és central o no)
     */
    private int evaluateInfluence(HexGameStatus board, int player) {
        int influenceScore = 0;

        for (int x = 0; x < board.getSize(); x++) {
            for (int y = 0; y < board.getSize(); y++) {
                if (board.getPos(x, y) == player) {
                    // Ponderar si la posició està a l'àrea central
                    if (isCentralArea(board, x, y)) {
                        influenceScore += weightCentralArea;
                    }

                    // Ponderar si la posició està a l'àrea de cantonada
                    if (isCornerArea(board, x, y)) {
                        influenceScore += weightCornerArea;
                    }
                }
            }
        }
        return influenceScore;
    }

    /**
     * Verifica si una posició està a l'àrea central del tauler.
     * @param board tauler del torn
     * @param x Coordenada X de la posició.
     * @param y Coordenada Y de la posició.
     * @return true si la posició està a l'àrea central, false en cas contrari.
     */
    private boolean isCentralArea(HexGameStatus board, int x, int y) {
        int size = board.getSize();

        // Definir els límits de l'àrea central
        int centralStart = size / 3;        // Inici de l'àrea central
        int centralEnd = (2 * size) / 3;   // Fi de l'àrea central

        // Verificar si (x, y) està dins d'aquests límits
        return x >= centralStart && x <= centralEnd && y >= centralStart && y <= centralEnd;
    }

    /**
     * Verifica si una posició està a l'àrea de les cantonades del tauler.
     * @param board tauler del torn
     * @param x Coordenada X de la posició.
     * @param y Coordenada Y de la posició.
     * @return true si la posició està a l'àrea de les cantonades, false en cas
     * contrari.
     */
    private boolean isCornerArea(HexGameStatus board, int x, int y) {
        int size = board.getSize();
        int cornerRange = size / 4; // Distància des de les cantonades per definir l'àrea

        // Verificar si (x, y) està dins del rang de les cantonades
        return (x < cornerRange && y < cornerRange)
                || // Cantonada superior esquerra
                (x >= size - cornerRange && y < cornerRange)
                || // Cantonada superior dreta
                (x < cornerRange && y >= size - cornerRange)
                || // Cantonada inferior esquerra
                (x >= size - cornerRange && y >= size - cornerRange); // Cantonada inferior dreta
    }

    /**
     * Verifica si una jugada està alineada amb la direcció objectiu del
     * jugador.
     *
     * @param player El jugador (1 o 2).
     * @param startX Coordenada X inicial.
     * @param startY Coordenada Y inicial.
     * @param endX Coordenada X final.
     * @param endY Coordenada Y final.
     * @return true si la jugada avança cap als extrems objectiu del jugador,
     * false en cas contrari.
     */
    private boolean isAlignedWithGoal(int player, int startX, int startY, int endX, int endY) {
        if (player == 1) {
            // Jugador 1: Avança cap a l'est
            return endX > startX; // Moviment en direcció positiva X
        } else if (player == 2) {
            // Jugador 2: Avança cap al sud
            return endY > startY; // Moviment en direcció positiva Y
        }
        return false; // Cas no vàlid
    }

    /**
     * Penalitza els moviments del jugador que no estan alineats amb la seva
     * direcció objectiu.
     * @param board tauler del torn
     * @param player El jugador (1 o 2).
     * @return La penalització total per moviments no alineats.
     */
    private int evaluateMisalignedMoves(HexGameStatus board, int player) {
        int misalignmentPenaltyScore = 0;

        for (int x = 0; x < board.getSize(); x++) {
            for (int y = 0; y < board.getSize(); y++) {
                if (board.getPos(x, y) == player) {
                    for (int[] dir : getHexDirections()) {
                        int neighborX = x + dir[0];
                        int neighborY = y + dir[1];

                        if (isValid(board, neighborX, neighborY) && board.getPos(neighborX, neighborY) == 0) {
                            // Verifica si el moviment està alineat amb l'objectiu
                            if (!isAlignedWithGoal(player, x, y, neighborX, neighborY)) {
                                misalignmentPenaltyScore += misalignmentPenalty;
                            }
                        }
                    }
                }
            }
        }

        return misalignmentPenaltyScore;
    }

    /**
     * Retorna les direccions dels moviments possibles en un tauler hexagonal.
     *
     * @return Un array amb les direccions dels moviments possibles.
     */
    private int[][] getHexDirections() {
        return new int[][]{
            {1, 0}, // Est
            {0, 1}, // Nord-est
            {-1, 1}, // Nord-oest
            {-1, 0}, // Oest
            {0, -1}, // Sud-oest
            {1, -1} // Sud-est
        };
    }

    /**
     * Calcula la distància mínima entre els costats del tauler utilitzant
     * Dijkstra.
     *
     * @param player El jugador (1 o 2).
     * @param board tauler del torn
     * @return La distància mínima en passos buits entre els extrems del
     * jugador.
     */
    public int calculateDistanceToVictory(HexGameStatus board, int player) {
        int size = board.getSize();
        int[][] distances = new int[size][size];
        for (int[] row : distances) {
            Arrays.fill(row, Integer.MAX_VALUE);
        }

        PriorityQueue<Node> priorityQueue = new PriorityQueue<>(Comparator.comparingInt(cell -> cell.distance));
        Set<Point> visitats = new HashSet<>();

        // Inicialitza les distàncies des del costat inicial del jugador
        if (player == 1) { // Oest a Est
            for (int y = 0; y < size; y++) {
                Point start = new Point(0, y);
                int cellState = board.getPos(start);
                if (cellState == player || cellState == 0) {
                    distances[start.x][start.y] = cellState == 0 ? 1 : 0;
                    priorityQueue.add(new Node(start, distances[start.x][start.y]));
                }
            }
        } else { // Nord a Sud
            for (int x = 0; x < size; x++) {
                Point start = new Point(x, 0);
                int cellState = board.getPos(start);
                if (cellState == player || cellState == 0) {
                    distances[start.x][start.y] = cellState == 0 ? 1 : 0;
                    priorityQueue.add(new Node(start, distances[start.x][start.y]));
                }
            }
        }

        // Direccions possibles en un tauler hexagonal
        int[][] direccions = {
            {1, 0}, {0, 1}, {-1, 1}, {-1, 0}, {0, -1}, {1, -1}
        };

        // Dijkstra per calcular les distàncies mínimes
        while (!priorityQueue.isEmpty()) {
            Node current = priorityQueue.poll();

            if (visitats.contains(current.point)) {
                continue;
            }
            visitats.add(current.point);

            // Si arribem al costat oposat, podem aturar-nos
            if ((player == 1 && current.point.x == size - 1)
                    || (player == 2 && current.point.y == size - 1)) {
                return distances[current.point.x][current.point.y];
            }

            // Explorar veïns
            for (int[] dir : direccions) {
                int newX = current.point.x + dir[0];
                int newY = current.point.y + dir[1];
                Point vei = new Point(newX, newY);

                if (isValid(board, newX, newY)) {
                    int cellState = board.getPos(vei);
                    int newDistance = distances[current.point.x][current.point.y] + (cellState == 0 ? 1 : 0);

                    if (newDistance < distances[newX][newY]) {
                        distances[newX][newY] = newDistance;
                        priorityQueue.add(new Node(vei, newDistance));
                    }
                }
            }
        }
        // Si no es pot connectar, retornem un valor alt
        return Integer.MAX_VALUE;
    }

    /**
     * Classe auxiliar per emmagatzemar les cel·les del tauler juntament amb les
     * seves distàncies.
     */
    private static class Node {

        Point point;
        int distance;

        Node(Point point, int distance) {
            this.point = point;
            this.distance = distance;
        }
    }
}
