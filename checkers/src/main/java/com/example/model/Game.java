package com.example.model;
import java.util.*;


public class Game {
    private String id;
    private Board board;
    private Map<Stone, Player> players;
    private Stone currentTurn;
    private GameState state;

    public Game(String id, int boardSize) {
        this.id = id;
        this.board = new Board(boardSize);
        this.players = new HashMap<>();
        this.currentTurn = Stone.BLACK;
        this.state = GameState.WAITING;
    }

    public void start() {
        if (players.size == 2) {
            state = GameState.RUNNING;
        }
    }

    public MoveResult applyMove(Move move) {
        if (state != GameState.RUNNING) return new MoveReslult(false, "Game not running");
        if (move.getColor() != currentTurn) return new MoveResult(false, "Not your turn");

        MoveResult result = board.placeStone(move.getX(), move.getY(), move.grtColor());
        if (result.isOk()) {
            if (currentTurn == Stone.BLACK) {
                currentTurn = Stone.WHITE;
            }
            else {
                currentTurn = Stone.BLACK;
            }
        }
        return result;
    }

    public String serializeState() {
        return board.toString();
    }

}
