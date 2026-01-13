package com.example.game;

import com.example.model.*;
import com.example.rules.*;
import java.util.*;



public class Game {
    private Board board;
    private Map<String, Player> players = new HashMap<>();
    private Stone currentTurn;
    private GameState state;

    private final Rules rules = new GoRules();

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
    players.put(p.getId(), p);
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

    public void setState(GameState s) {
        this.state = s;
    }


    public Stone getCurrentTurn() {
        return currentTurn;
    }

    public MoveResult applyMove(Move move) {
        Player current = players.get(move.getPlayerId()); //wez gracza, ktory gra kolorem podanym w ruchu
        if (state != GameState.RUNNING) return MoveResult.error("Game not running");
        if (current.getColor() != currentTurn) return MoveResult.error("Not your turn");

        MoveResult result = rules.applyMove(board, move.getX(), move.getY(), current.getColor());

        if (!result.isOk()) {
            return result;
        }
        //aktualizacja planszy
        board = result.getBoardSnapshot();
        //policz jencow
        int captured = result.getCaptures().size();
        current.addPrisoners(captured);
        //zmiana tury
        if (currentTurn == Stone.BLACK) {
            currentTurn = Stone.WHITE;
        }
        else {
            currentTurn = Stone.BLACK;
        }
        //powiadom listenerow
        notifyMove(move, result, result.getBoardSnapshot());

        return result;
    }

    public String serializeState() {
        return board.toString();
    }

    public GameState getState(){
        return state;
    }

    public void nextTurn() {
        currentTurn = (currentTurn == Stone.BLACK ? Stone.WHITE : Stone.BLACK);
    }


}
