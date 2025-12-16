package com.example.server;

import com.example.model.Player;

import java.io.*;
import java.net.Socket;

/**
 * Obsługa jednego klienta: oczekuje pierwszej linii "JOIN <name>",
 * potem akceptuje tylko polecenia typu "MOVE x y".
 */
public class ClientHandler implements Runnable {
    private final Server server;
    private final Socket socket;
    private Player player;
    private BufferedReader in;
    private PrintWriter out;

    public ClientHandler(Server server, Socket socket) {
        this.server = server; this.socket = socket;
    }

    public void setPlayer(Player p) { this.player = p; }
    public Player getPlayer() { return player; }

    public void send(String msg) {
        if (out != null) {
            out.println(msg);
            out.flush();
        }
    }

    public void setOut(PrintWriter out) {
    this.out = out;
    }


    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            send("WELCOME");
            String line = in.readLine();
            if (line == null) return;
            String[] parts = line.trim().split("\\s+", 2);
            if (parts.length < 2 || !parts[0].equalsIgnoreCase("JOIN")) {
                send("ERROR Expecting: JOIN <name>");
                socket.close();
                return;
            }
            String name = parts[1];
            boolean ok = server.registerClient(this, name);
            if (!ok) { socket.close(); return; }

            // główna pętla czytająca ruchy
            while ((line = in.readLine()) != null) {
                server.handleRawMove(line, this);
            }
        } catch (IOException e) {
            System.out.println("Client disconnected: " + e.getMessage());
        } finally {
            try { socket.close(); } catch (IOException ignored) {}
        }
    }
}