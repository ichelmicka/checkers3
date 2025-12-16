package com.example.server;

import com.example.model.Player;
import com.example.model.PlayerFactory;
import com.example.model.Stone;
import com.example.model.Move;
import com.example.model.Board;
import com.example.model.MoveResult;
import com.example.game.Game;
import com.example.game.GameListener;


import java.io.*;
import java.net.*;
import java.util.concurrent.*;

/**
 * Singleton server — obsługuje do 2 graczy, deleguje logikę do Game.
 */
public class Server implements GameListener {
    private static Server instance;

    public static synchronized Server getInstance(int port, int boardSize) {
        if (instance == null) instance = new Server(port, boardSize);
        return instance;
    }

    private final int port;
    private final Game game;
    private final PlayerFactory playerFactory = new PlayerFactory();
    private final ConcurrentMap<String, ClientHandler> clients = new ConcurrentHashMap<>();
    private final ExecutorService exec = Executors.newCachedThreadPool();

    private Server(int port, int boardSize) {
        this.port = port;
        this.game = new Game(boardSize);
        this.game.addListener(this);
    }

    public void start() throws IOException {
        try (ServerSocket ss = new ServerSocket(port)) {
            System.out.println("Server listening on port " + port);
            while (true) {
                Socket s = ss.accept();
                ClientHandler h = new ClientHandler(this, s);
                exec.submit(h);
            }
        }
    }

    /**
     * Rejestracja klienta. Zwraca false jeśli gra pełna.
     */
    public synchronized boolean registerClient(ClientHandler handler, String name) {
        if (clients.size() >= 2) {
            handler.send("ERROR game already has two players");
            return false;
        }
        Player p = playerFactory.create(name);
        boolean joined = game.join(p);
        if (!joined) {
            handler.send("ERROR cannot join game");
            return false;
        }
        clients.put(p.getId(), handler);
        handler.setPlayer(p);
        handler.send("ASSIGN " + p.getId() + " " + p.getColor());
        broadcast("INFO Player " + p.getName() + " joined as " + p.getColor());
        if (clients.size() == 2) {
            broadcast("START");
            broadcastBoard();
            broadcast("INFO Current turn: " + game.getCurrentTurn());
        }
        return true;
    }

    /**
     * Handle move string from a client ("MOVE x y").
     */
    public void handleRawMove(String raw, ClientHandler origin) {
        Move m = Move.parse(raw, origin.getPlayer().getId());
        if (m == null) {
            origin.send("ERROR bad command. Use: MOVE <x> <y>");
            return;
        }
        MoveResult res = game.applyMove(m);
        if (!res.isOk()) {
            origin.send("ERROR " + res.getErrorMessage());
        } // jeśli sukces, ruchy są wysyłane do clients przez onMoveApplied() (observer)
    }

    // GameListener implementation — wywoływane po poprawnym ruchu
    @Override
    public void onMoveApplied(Move move, MoveResult result, Board snapshotBoard) {
        // broadcast info
        broadcast("MOVE " + move.playerId + " " + (move.pos.x) + " " + (move.pos.y));
        if (result.getCaptures() != null && !result.getCaptures().isEmpty()) {
            broadcast("CAPTURE " + result.getCaptures().size());
        }
        broadcast("BOARD\n" + snapshotBoard.toString());
        broadcast("INFO Next turn: " + game.getCurrentTurn());
    }

    public void broadcast(String msg) {
        clients.values().forEach(h -> h.send(msg));
    }

    public void broadcastBoard() {
        broadcast("BOARD\n" + game.getBoard().toString());
    }

    public static void main(String[] args) throws Exception {
        int port = 8888;
        int size = 19;
        if (args.length >= 1) port = Integer.parseInt(args[0]);
        if (args.length >= 2) size = Integer.parseInt(args[1]);
        Server s = Server.getInstance(port, size);
        s.start();
    }
}