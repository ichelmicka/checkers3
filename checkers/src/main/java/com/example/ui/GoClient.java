package com.example.ui;

import java.io.*;
import java.net.Socket;
import java.util.function.Consumer;

/**
 * Klient TCP do komunikacji z serwerem Go.
 * Odbiera linie tekstowe i przekazuje je do GUI.
 */
public class GoClient {

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    private final Consumer<String> onMessage;
    private final Consumer<String> onError;



    public GoClient(String host, int port, Consumer<String> onMessage, Consumer<String> onError) {
        this.onMessage = onMessage;
        this.onError = onError;

        try {
            socket = new Socket(host, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // osobny wątek do odbierania wiadomości
            Thread reader = new Thread(this::listenLoop);
            reader.setDaemon(true);
            reader.start();

        } catch (Exception e) {
            onError.accept("Connection failed: " + e.getMessage());
        }
    }

    /**
     * Odbieranie wiadomości z serwera w pętli.
     */
    private void listenLoop() {
        try {
            String line;
            while ((line = in.readLine()) != null) {
                onMessage.accept(line);
            }
        } catch (IOException e) {
            onError.accept("Connection lost: " + e.getMessage());
        }
    }

    /**
     * Wysyłanie komendy do serwera.
     */
    public void send(String msg) {
        if (out != null) {
            out.println(msg);
        }
    }

    /**
     * Zamykanie połączenia.
     */
    public void close() {
        try {
            socket.close();
        } catch (IOException ignored) {}
    }

    /**
     * Umożliwia GUI odczytanie surowego BufferedReadera,
     * np. do pobrania wielolinijkowego BOARD.
     */
    public BufferedReader getReader() {
        return in;
    }
}
