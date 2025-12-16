package com.example.game;

import com.example.model.*;
import java.util.*;



public class Game {
    private Board board;
    private Map<Stone, Player> players;
    private Stone currentTurn;
    private GameState state;

    public Game(int boardSize) {
        this.board = new Board(boardSize);
        this.players = new HashMap<>();
        this.currentTurn = Stone.BLACK;
        this.state = GameState.WAITING;
    }

    private final List<GameListener> listeners = new ArrayList<>();

    public void addListener(GameListener l) {
        listeners.add(l);
    }

    private void notifyMove(Move move, MoveResult result, Board snapshot) {
        for (GameListener l : listeners) {
            l.onMoveApplied(move, result, snapshot);
        }
    }

    public boolean join(Player p) {
    if (players.size() >= 2) return false;
    players.put(p.getColor(), p);
    if (players.size() == 2) start();
    return true;
    }

    public Board getBoard() {
         return board; 
    }

    public void start() {
        if (players.size() == 2) {
            state = GameState.RUNNING;
        }
    }

    public Stone getCurrentTurn() {
        return currentTurn;
    }

    public MoveResult applyMove(Move move) {
        Player current = players.get(currentTurn); //wez gracza, ktory gra kolorem podanym w ruchu
        if (state != GameState.RUNNING) return MoveResult.error("Game not running");
        if (current.getColor() != currentTurn) return MoveResult.error("Not your turn");

        MoveResult result = board.placeStone(move.getX(), move.getY(), current.getColor());

        if (!result.isOk()) {
            return result;
        }

        int captured = result.getCaptures().size();
        
        current.addPrisoners(captured);
        
        if (currentTurn == Stone.BLACK) {
            currentTurn = Stone.WHITE;
        }
        else {
            currentTurn = Stone.BLACK;
        }

        notifyMove(move, result, result.getBoardSnapshot());

        return result;
    }

    public String serializeState() {
        return board.toString();
    }

    public GameState getState(){
        return state;
    }

}
