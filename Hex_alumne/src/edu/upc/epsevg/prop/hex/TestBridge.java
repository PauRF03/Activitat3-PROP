package edu.upc.epsevg.prop.hex;

import java.awt.Point;

public class TestBridge {
    public static void main(String[] args) {
        // Crear un tablero de tamaño 5x5
        HexGameStatus gameStatus = new HexGameStatus(5);
        Heuristica heuristica = new Heuristica(gameStatus);

        System.out.println("Estado inicial del tablero:");
        System.out.println(gameStatus);

        try {
            // Caso 1: Puente básico (directo)
            System.out.println("\n--- Caso 1: Puente básico ---");
            gameStatus.placeStone(new Point(1, 1)); // Jugador 1
            gameStatus.currentPlayer = PlayerType.PLAYER1; // Forzar turno de Jugador 1
            gameStatus.placeStone(new Point(3, 3)); // Jugador 1
            gameStatus.currentPlayer = PlayerType.PLAYER1;

            System.out.println("Después de colocar fichas en (1, 1) y (3, 3):");
            System.out.println(gameStatus);

            // Verificar puente
            boolean isBridge = heuristica.isBridge(1, 1, 1);
            System.out.println("¿Se detecta un puente desde (1, 1)? " + isBridge);

            // Completar puente
            gameStatus.placeStone(new Point(2, 2)); // Jugador 1
            System.out.println("Después de colocar ficha en (2, 2):");
            System.out.println(gameStatus);

            isBridge = heuristica.isBridge(1, 1, 1);
            System.out.println("¿Se detecta un puente desde (1, 1) ahora? " + isBridge);

            // Caso 2: Puente en los bordes
            System.out.println("\n--- Caso 2: Puente en los bordes ---");
            gameStatus = new HexGameStatus(5); // Reiniciar tablero
            heuristica = new Heuristica(gameStatus);

            gameStatus.placeStone(new Point(0, 0)); // Jugador 1
            gameStatus.currentPlayer = PlayerType.PLAYER1;
            gameStatus.placeStone(new Point(2, 1)); // Jugador 1
            gameStatus.currentPlayer = PlayerType.PLAYER1;

            System.out.println("Después de colocar fichas en (0, 0) y (2, 1):");
            System.out.println(gameStatus);

            isBridge = heuristica.isBridge(1, 0, 0);
            System.out.println("¿Se detecta un puente desde (0, 0)? " + isBridge);

            gameStatus.placeStone(new Point(1, 1)); // Jugador 1
            System.out.println("Después de colocar ficha en (1, 1):");
            System.out.println(gameStatus);

            isBridge = heuristica.isBridge(1, 0, 0);
            System.out.println("¿Se detecta un puente desde (0, 0) ahora? " + isBridge);

            // Caso 3: Puente con interferencia
            System.out.println("\n--- Caso 3: Puente con interferencia ---");
            gameStatus = new HexGameStatus(5); // Reiniciar tablero
            heuristica = new Heuristica(gameStatus);

            gameStatus.placeStone(new Point(1, 1)); // Jugador 1
            gameStatus.currentPlayer = PlayerType.PLAYER1;
            gameStatus.placeStone(new Point(3, 3)); // Jugador 1
            gameStatus.currentPlayer = PlayerType.PLAYER1;

            System.out.println("Después de colocar fichas en (1, 1) y (3, 3):");
            System.out.println(gameStatus);

            gameStatus.currentPlayer = PlayerType.PLAYER2;
            gameStatus.placeStone(new Point(2, 2)); // Jugador 2 (interferencia)

            System.out.println("Después de colocar ficha en (2, 2) por Jugador 2:");
            System.out.println(gameStatus);

            isBridge = heuristica.isBridge(1, 1, 1);
            System.out.println("¿Se detecta un puente desde (1, 1) ahora? " + isBridge);

            // Caso 4: Puente grande (tablero 7x7)
            System.out.println("\n--- Caso 4: Puente en tablero grande ---");
            gameStatus = new HexGameStatus(7); // Tablero más grande
            heuristica = new Heuristica(gameStatus);

            gameStatus.placeStone(new Point(1, 1)); // Jugador 1
            gameStatus.currentPlayer = PlayerType.PLAYER1;
            gameStatus.placeStone(new Point(5, 5)); // Jugador 1
            gameStatus.currentPlayer = PlayerType.PLAYER1;

            System.out.println("Después de colocar fichas en (1, 1) y (5, 5):");
            System.out.println(gameStatus);

            gameStatus.placeStone(new Point(3, 3)); // Jugador 1
            gameStatus.currentPlayer = PlayerType.PLAYER1;

            System.out.println("Después de colocar ficha en (3, 3):");
            System.out.println(gameStatus);

            isBridge = heuristica.isBridge(1, 1, 1);
            System.out.println("¿Se detecta un puente desde (1, 1)? " + isBridge);
            
            gameStatus.placeStone(new Point(2, 2)); // Jugador 1
            gameStatus.currentPlayer = PlayerType.PLAYER1;

            System.out.println("Después de colocar ficha en (2, 2):");
            System.out.println(gameStatus);

            isBridge = heuristica.isBridge(1, 1, 1);
            System.out.println("¿Se detecta un puente desde (1, 1)? " + isBridge);
        } catch (RuntimeException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
