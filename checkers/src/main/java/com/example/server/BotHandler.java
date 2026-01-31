package com.example.server;

import com.example.model.*;
import com.example.game.Game;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BotHandler implements Connection {

    private final Server server;
    private Game game;
    private Player player;
    private Stone myColor;
    private final Board board;
    private boolean lastMoveFailed = false;
    private boolean twoLastMovesFailed = false;

    public BotHandler(Server server, int boardSize) {
        this.server = server;
        this.game = server.getGame();
        this.board = new Board(boardSize);
    }

    public void setPlayer(Player p) {
        this.player = p;
    }

    public Player getPlayer() {
        return player;
    }

    private void syncBoard() {
        Board src = game.getBoard();
        for (int y = 0; y < src.getSize(); y++) {
            for (int x = 0; x < src.getSize(); x++) {
                board.set(x, y, src.get(x, y));
            }
        }
    }


    /**
     * Serwer wysyła do bota komunikaty tak samo jak do ClientHandler.
     */
    public void send(String msg) {

        if (msg.startsWith("ASSIGN")) {
            String[] parts = msg.split(" ");
            myColor = Stone.valueOf(parts[2]);
        }

        else if (msg.equals("START")) {
            // ignorujemy
        }

        else if (msg.startsWith("SCORING")) {
            server.handleAccept(this);
        }

        else if (msg.startsWith("INFO Next")) {
            syncBoard();
            Stone turn = Stone.valueOf(msg.split(" ")[3]);
            System.out.println(Stone.valueOf(msg.split(" ")[3]));
            if (turn == myColor) makeMove();
        }
        else if (msg.startsWith("INFO Next")) {
            syncBoard();
            Stone turn = Stone.valueOf(msg.split(" ")[3]);
            System.out.println(Stone.valueOf(msg.split(" ")[3]));
            if (turn == myColor) makeMove();
        }
        else if (msg.startsWith("PASS")) {
            syncBoard();
        }
        else if (msg.startsWith("ERROR")) {
            syncBoard();
            if (lastMoveFailed) {
                twoLastMovesFailed = true;
            }
            lastMoveFailed = true;
            makeMove();
        }
    }

    private void makeMove() {
        List<Position> empty = new ArrayList<>();

        for (int y = 0; y < board.getSize(); y++)
            for (int x = 0; x < board.getSize(); x++)
                if (board.get(x, y) == Stone.EMPTY)
                    empty.add(new Position(x, y));

        if (twoLastMovesFailed) {
            server.handlePass(this);
            lastMoveFailed = twoLastMovesFailed = false;
            return;
        }
        else if (empty.isEmpty()) {
            server.handlePass(this);
            return;
        }

        Position p = empty.get(new Random().nextInt(empty.size()));
        server.handleRawMove("MOVE " + p.x + " " + p.y, this);
    }
}
