package edu.upc.epsevg.prop.hex;
import java.awt.Point;

public class TestCalculateSpacesToConnect {
    public static void main(String[] args) {
        HexGameStatus gameStatus = new HexGameStatus(5);
        Heuristica heuristica = new Heuristica(gameStatus);

        // Caso 1
        gameStatus.placeStone(new Point(1, 0)); // Piedra jugador 1
        System.out.println("Caso 1:");
        System.out.println(gameStatus);
        System.out.println("Espacios para conectar: " + heuristica.calculateSpacesToConnect(1));

        // Caso 2
        gameStatus = new HexGameStatus(5);
        heuristica = new Heuristica(gameStatus);
        gameStatus.placeStone(new Point(1, 0)); // Piedra jugador 1
        
        gameStatus.placeStone(new Point(3, 0)); // Piedra jugador 2
        System.out.println("\nCaso 2:");
        System.out.println(gameStatus);
        System.out.println("Espacios para conectar: " + heuristica.calculateSpacesToConnect(1));

        // Caso 3
        gameStatus = new HexGameStatus(5);
        heuristica = new Heuristica(gameStatus);
        gameStatus.placeStone(new Point(4, 0)); // Piedra jugador 1
        gameStatus.currentPlayer = PlayerType.PLAYER1;
        gameStatus.placeStone(new Point(0, 4)); // Piedra jugador 1
        System.out.println("\nCaso 3:");
        System.out.println(gameStatus);
        System.out.println("Espacios para conectar: " + heuristica.calculateSpacesToConnect(1));

        // Caso 4
        gameStatus = new HexGameStatus(5);
        heuristica = new Heuristica(gameStatus);
        gameStatus.placeStone(new Point(4, 0)); // Piedra jugador 1
        gameStatus.placeStone(new Point(0, 4)); // Piedra jugador 2
        gameStatus.placeStone(new Point(2, 2)); // Piedra jugador 1
        System.out.println("\nCaso 4:");
        System.out.println(gameStatus);
        System.out.println("Espacios para conectar: " + heuristica.calculateSpacesToConnect(1));
    }
}

