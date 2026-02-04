package com.example.ui;

import com.example.model.Board;
import com.example.model.Stone;
import com.example.rules.TerritoryScorer;
import com.example.model.GameState;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;


/**
 * GUI klienta gry Go (Swing).
 * Oczekiwane komunikaty serwera (obsługiwane):
 * - {@code ASSIGN <id> <color>} — przypisanie koloru klientowi
 * - {@code BOARD} + następne n linii — stan planszy
 * - {@code INFO Next turn: <color>} — (opcjonalnie) informacja o kolejce</li>
 * - {@code TURN <color>} — alternatywny, prosty komunikat określający aktualną turę</li>
 * - {@code ERROR ...}, {@code WINNER ...} — komunikaty informacyjne</li>
 */
public class MainGui {

    /** Rozmiar planszy (liczba wierszy/kolumn). */
    private final int boardSize = 19;

    /** Lokalny snapshot planszy otrzymywany od serwera. */
    private Board board = new Board(boardSize);

    /** Kolor przypisany temu klientowi (EMPTY = jeszcze nie przypisano). */
    private Stone myColor = Stone.EMPTY;

    /** Stan gry otrzymywany ode serwera */
    private GameState gameState = GameState.WAITING;

    /** Liczba wiezniow bialego, otrzymywana od serwera */
    private int whiteCaptures = 0;

    /** Liczba wiezniow bialego, otrzymywana od serwera */
    private int blackCaptures = 0;

    /**
     * Aktualna tura (kolor, który ma grać).  
     * Ustawiana na podstawie komunikatów serwera.
     */
    private Stone currentTurn = Stone.BLACK;

    private JButton passButton;
    private JButton resgnButton;
    private JButton acceptButton;

    private JFrame frame;
    private BoardPanel boardPanel;
    private JPanel sidePanel;
    private JPanel bottom;
    private JLabel myColorLabel;
    private JLabel statusLabel;
    private JLabel connectionLabel;
    private JTextArea scoreArea;

    private GoClient client;

    private final HttpClient httpClient = HttpClient.newHttpClient();


    /**
     * Punkt wejścia uruchamiający GUI w wątku event-dispatch.
     *
     * @param args argumenty linii poleceń (nieużywane)
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainGui().start());
    }

    /** Inicjuje połączenie z serwerem i buduje GUI. */
    public void start() {
        connectToServer();
        createGui();
    }

    /**
     * Nawiązuje połączenie z serwerem i rejestruje callbacki obsługi wiadomości / błędów.
     * Wysyła natychmiast komendę JOIN.
     */
    private void connectToServer() {
        client = new GoClient("localhost", 8888, this::onServerMessage, this::onServerError);
        // wyślij JOIN (nazwa gracza)
        client.send("JOIN Player1");
    }

    /** Buduje okno Swing z planszą, panelem bocznym i przyciskami. */
    private void createGui() {
        frame = new JFrame("Go Client");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // PANEL GÓRNY (status połączenia / tura)
        statusLabel = new JLabel("Connecting...");
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(statusLabel);
        frame.add(top, BorderLayout.NORTH);

        // PANEL PLANSZY
        boardPanel = new BoardPanel();
        boardPanel.setPreferredSize(new Dimension(600, 600));
        frame.add(boardPanel, BorderLayout.CENTER);

        // PANEL BOCZNY (informacje i wynik)
        sidePanel = new JPanel();
        sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.Y_AXIS));
        sidePanel.setPreferredSize(new Dimension(240, 400));

        myColorLabel = new JLabel("You are: ?");
        connectionLabel = new JLabel("Status: Connecting...");

        sidePanel.add(myColorLabel);
        sidePanel.add(Box.createVerticalStrut(10));
        sidePanel.add(connectionLabel);
        sidePanel.add(Box.createVerticalStrut(10));

        // pole do wyświetlania wyniku (JTextArea, bo JLabel nie łamie linii)
        scoreArea = new JTextArea(6, 20);
        scoreArea.setEditable(false);
        scoreArea.setLineWrap(true);
        scoreArea.setWrapStyleWord(true);
        scoreArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        scoreArea.setText("Score:\nBlack: ?\nWhite: ?");
        JScrollPane scoreScroll = new JScrollPane(scoreArea);
        scoreScroll.setPreferredSize(new Dimension(220, 120));
        sidePanel.add(scoreScroll);

        frame.add(sidePanel, BorderLayout.EAST);

        // PANEL DOLNY (przyciski)
        bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        passButton = new JButton("PASS");
        passButton.addActionListener(e -> sendPass());
        bottom.add(passButton);

        resgnButton = new JButton("RESIGN");
        resgnButton.addActionListener(e -> sendResign());
        bottom.add(resgnButton);

        acceptButton = new JButton("ACCEPT");
        acceptButton.addActionListener(e -> client.send("ACCEPT"));
        bottom.add(acceptButton);

        acceptButton = new JButton("RESUME");
        acceptButton.addActionListener(e -> client.send("RESUME"));
        bottom.add(acceptButton);

        JButton replayButton = new JButton("REPLAY DB");
        replayButton.addActionListener(e -> {
            String idStr = JOptionPane.showInputDialog(frame, "Enter game id to replay:", "Replay", JOptionPane.QUESTION_MESSAGE);
            if (idStr == null || idStr.isBlank()) return;
            try {
                long gid = Long.parseLong(idStr.trim());
                startReplayFromDatabase(gid);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "Bad id", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        bottom.add(replayButton);


        frame.add(bottom, BorderLayout.SOUTH);

        // pokaż okno
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    /**
     * Callback obsługujący komunikaty z serwera.
     * Rozpoznaje ASSIGN, BOARD, INFO Next turn, TURN, ERROR i WINNER.
     *
     * @param msg linia tekstu od serwera
     */
    private void onServerMessage(String msg) {
        System.out.println("SERVER: " + msg);

        if (msg.startsWith("ASSIGN")) {
            // ASSIGN <playerId> <color>
            String[] p = msg.split(" ");
            if (p.length >= 3) {
                myColor = Stone.valueOf(p[2]);
                SwingUtilities.invokeLater(() -> {
                    myColorLabel.setText("You are: " + myColor);
                    connectionLabel.setText("Status: Connected");
                });
            }
        } else if (msg.startsWith("INFO Next turn:")) {
            // Serwer czasem wysyła "INFO Next turn: <color>"
            String colorPart = msg.substring("INFO Next turn:".length()).trim();
            try {
                Stone turn = Stone.valueOf(colorPart.toUpperCase());
                setCurrentTurn(turn);
            } catch (IllegalArgumentException ignored) {
            }
        } else if (msg.startsWith("TURN")) {
            // Alternatywna, prosta forma: "TURN <color>"
            String[] p = msg.split(" ");
            if (p.length >= 2) {
                try {
                    Stone turn = Stone.valueOf(p[1]);
                    setCurrentTurn(turn);
                } catch (IllegalArgumentException ignored) {
                }
            }
        } else if (msg.startsWith("INFO")) {
            SwingUtilities.invokeLater(() -> statusLabel.setText(msg.substring(5)));
        } else if (msg.startsWith("ERROR")) {
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(frame, msg, "Error", JOptionPane.ERROR_MESSAGE));
        } else if (msg.startsWith("WINNER")) {
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(frame, msg));
        } else if (msg.startsWith("BOARD")) {
            // następne n linii to tekst planszy
            readBoardFromServer();
            SwingUtilities.invokeLater(() -> {
                showScore();
                boardPanel.repaint();
            });
        } else if (msg.equals("WELCOME")) { 
            gameState = GameState.RUNNING; 
        } else if (msg.equals("SCORING")) { 
            gameState = GameState.SCORING;
            boardPanel.repaint(); 
        } else if (msg.equals("RESUME")) { 
            gameState = GameState.RUNNING;
            board.clearDeadMarks();
            boardPanel.repaint(); 
        } else if (msg.equals("END")) { 
            gameState = GameState.FINISHED; 
        } else if (msg.startsWith("MARK")) {
            String[] p = msg.split(" ");
            int x = Integer.parseInt(p[1]);
            int y = Integer.parseInt(p[2]);
            boolean dead = p[3].equals("DEAD");

            board.markGroup(x, y, dead);
            boardPanel.repaint();
        }
        else if (msg.startsWith("ACCEPTED")) {
            String who = msg.split(" ")[1];
            System.out.println("Gracz " + who + " zaakceptował wynik.");
        } else if (msg.equals("END")) {
            gameState = GameState.FINISHED;
            JOptionPane.showMessageDialog(frame, "Gra zakończona - wynik policzony.");
            boardPanel.repaint();
        } else if (msg.startsWith("CAPTURED BY BLACK")) {
            String[] p = msg.split(" ");
            blackCaptures = Integer.parseInt(p[3]);
        } else if (msg.startsWith("CAPTURED BY WHITE")) {
            String[] p = msg.split(" ");
            whiteCaptures = Integer.parseInt(p[3]);
        }


    }

    /**
     * Ustawia aktualny kolor tury i aktualizuje widok/status.
     *
     * @param turn kolor który ma ruch
     */
    private void setCurrentTurn(Stone turn) {
        this.currentTurn = (turn == null) ? Stone.EMPTY : turn;
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText("Current turn: " + this.currentTurn + (this.myColor == this.currentTurn ? " (your turn)" : ""));
        });
    }

    /** Wyświetla/odświeża wynik za pomocą klasy {@link TerritoryScorer}. */
    private void showScore() {
        if (board == null) return;
        TerritoryScorer.Score s = TerritoryScorer.score(board);
        StringBuilder sb = new StringBuilder();
        sb.append("Score:\n");
        sb.append(String.format("Black: stones=%d territory=%d captures=%d\n", s.blackStones, s.blackTerritory, blackCaptures));
        sb.append(String.format("White: stones=%d territory=%d captures=%d\n", s.whiteStones, s.whiteTerritory, whiteCaptures));
        SwingUtilities.invokeLater(() -> scoreArea.setText(sb.toString()));
    }

    /** Wysyła PASS tylko jeśli to twoja tura. */
    private void sendPass() {
        if (client == null) return;
        if (myColor != currentTurn) {
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(frame, "Not your turn — cannot pass now.", "Wait", JOptionPane.INFORMATION_MESSAGE));
            return;
        }
        client.send("PASS");
    }

    /** Wysyła RESIGN (wyręcza grę) — można wysłać w dowolnym momencie. */
    private void sendResign() {
        if (client == null) return;
        int ok = JOptionPane.showConfirmDialog(frame, "Are you sure you want to resign?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (ok == JOptionPane.YES_OPTION) client.send("RESIGN");
    }

    /**
     * Odczytuje planszę (boardSize linii tekstu) z wejścia klienta i aktualizuje lokalny snapshot.
     * Oczekuje, że klient (GoClient) ma metodę {@code getReader()} zwracającą BufferedReader.
     */
    private void readBoardFromServer() {
        try {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < boardSize; i++) {
                String line = client.getReader().readLine();
                if (line == null) throw new IllegalStateException("Unexpected end of board from server");
                sb.append(line).append("\n");
            }
            board.updateFromString(sb.toString());
            SwingUtilities.invokeLater(() -> {
                showScore();
                boardPanel.repaint();
            });
        } catch (IOException e) {
            onServerError("Failed to read board: " + e.getMessage());
        } catch (Exception e) {
            onServerError("Failed to read board: " + e.getMessage());
        }
    }

    /** Pokazuje błąd połączenia w EDT. */
    private void onServerError(String msg) {
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(frame, msg, "Connection error", JOptionPane.ERROR_MESSAGE));
    }

    /**
     * Pobiera ruchy z persistence microservice i odtwarza je na lokalnej planszy.
     */
    private void startReplayFromDatabase(long gameId) {
        new Thread(() -> {
            try {
                String url = "http://localhost:8080/api/games/" + gameId + "/moves/raw";
                HttpRequest req = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .GET()
                        .build();
                HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());

                System.out.println("REPLAY HTTP STATUS: " + resp.statusCode());
                System.out.println("REPLAY HTTP BODY:\n" + resp.body());

                if (resp.statusCode() != 200) {
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(frame,
                            "Game not found or server error: " + resp.statusCode() + "\nBody:\n" + resp.body(),
                            "Error", JOptionPane.ERROR_MESSAGE));
                    return;
                }

                String body = resp.body();
                if (body == null || body.trim().isEmpty() || body.trim().equalsIgnoreCase("null")) {
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(frame,
                            "Replay returned empty body (null). Body:\n" + body,
                            "Replay error", JOptionPane.ERROR_MESSAGE));
                    return;
                }

                List<ReplayMove> moves = new ArrayList<>();
                String[] lines = body.split("\\r?\\n");
                for (String line : lines) {
                    line = line.trim();
                    if (line.isEmpty()) continue;
                    String[] parts = line.split("\\s+");
                    if (parts.length < 3) {
                        System.err.println("Skipping bad line in replay: '" + line + "'");
                        continue;
                    }
                    String colorStr = parts[0];
                    String p1 = parts[1];
                    String p2 = parts[2];
                    if ("null".equalsIgnoreCase(p1) || "null".equalsIgnoreCase(p2)) {
                        System.err.println("Skipping line with null coordinates: '" + line + "'");
                        continue;
                    }
                    int x = Integer.parseInt(p1);
                    int y = Integer.parseInt(p2);
                    String colorSan = (colorStr == null || colorStr.equalsIgnoreCase("null")) ? "BLACK" : colorStr;
                    moves.add(new ReplayMove(colorSan.toUpperCase(), x, y));
                }

                if (moves.isEmpty()) {
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(frame, "No moves found for that game."));
                    return;
                }

                SwingUtilities.invokeLater(() -> applyMovesWithTimer(moves));

            } catch (Exception e) {
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(frame, "Replay failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE));
            }
        }).start();
    }


    /**
     * Aplikuje listę ruchów z lekką animacją.
     */
    private void applyMovesWithTimer(List<ReplayMove> moves) {
        if (moves == null || moves.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "No moves to replay.");
            return;
        }

        // reset planszy
        board = new Board(boardSize);
        boardPanel.repaint();

        final int[] idx = {0};
        int delayMs = 500; // przerwa między ruchami
        Timer timer = new Timer(delayMs, null);
        timer.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (idx[0] >= moves.size()) {
                timer.stop();
                JOptionPane.showMessageDialog(frame, "Replay finished.");
                return;
            }
            ReplayMove m = moves.get(idx[0]++);
            Stone stone;
            try {
                if (m.color == null) {
                    System.err.println("ReplayMove color is null for move " + (idx[0]-1));
                }
                try {
                    stone = Stone.valueOf(m.color);
                } catch (Exception ex) {
                    // fallback: parity ruchu
                    stone = ((idx[0]) % 2 == 1) ? Stone.BLACK : Stone.WHITE;
                }

                // —— TU WAŻNE: ustaw kamień bez reguł (raw)
                try {
                    board.set(m.x, m.y, stone); // zobacz dalej: implementacja set() niżej
                } catch (NoSuchMethodError | AbstractMethodError nsme) {
                    // jeśli twoje Board ma inną nazwę metody, zastąp odpowiednio
                    System.err.println("Board.set(...) method not found — implement Board.set(x,y,stone) for replay.");
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }
            boardPanel.repaint();
        }
    });

        timer.start();
    }


    /** Pomocnicza klasa trzymająca ruch replaya. */
    private static class ReplayMove {
        final String color;
        final int x, y;
        ReplayMove(String color, int x, int y) { this.color = color; this.x = x; this.y = y; }
    }


    /**
     * Panel rysujący planszę i obsługujący kliknięcia myszy.
     * Kliknięcie wysyła komendę MOVE dopiero wtedy, gdy:
     *  - klient ma już przypisany kolor (ASSIGN),
     *  - oraz gdy {@code myColor == currentTurn} (to twoja tura).
     */
    private class BoardPanel extends JPanel {
        public BoardPanel() {
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    int cellSize = getWidth() / boardSize; 
                    int x = e.getX() / cellSize; 
                    int y = e.getY() / cellSize;
                    if (x < 0 || x >= boardSize || y < 0 || y >= boardSize) return; 
                    // 1. Jeśli gra skończona - nic nie rób 
                    if (gameState == GameState.FINISHED) 
                    { 
                        JOptionPane.showMessageDialog(frame, "Game has ended."); return; 
                    } 
                    // 2. Jeśli SCORING - wysyłamy MARK  
                    else if (gameState == GameState.SCORING) {   
                        // lewy przycisk = DEAD, prawy = ALIVE 
                        boolean dead = SwingUtilities.isLeftMouseButton(e); 
                        String status = dead ? "DEAD" : "ALIVE"; 
                        client.send("MARK " + x + " " + y + " " + status); return; 
                    }
                    // 3. Jesli RUNNING - normalne ruchy MOVE
                    else if (gameState == GameState.RUNNING) {
                        if (myColor == Stone.EMPTY) {
                            JOptionPane.showMessageDialog(frame, "Not yet assigned a color.", "Info", JOptionPane.INFORMATION_MESSAGE);
                            return;
                        }

                        // jeśli nie twoja tura — blokuj ruch
                        if (myColor != currentTurn) {
                            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(frame, "Not your turn.", "Wait", JOptionPane.INFORMATION_MESSAGE));
                            return;
                        }
                        if (client != null) client.send("MOVE " + x + " " + y);
                    }
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g;
            int cell = getWidth() / boardSize;

            g.setColor(new Color(240, 200, 120));
            g.fillRect(0, 0, getWidth(), getHeight());

            g.setColor(Color.BLACK);
            for (int i = 0; i < boardSize; i++) {
                g.drawLine(cell / 2, cell / 2 + i * cell, cell / 2 + (boardSize - 1) * cell, cell / 2 + i * cell);
                g.drawLine(cell / 2 + i * cell, cell / 2, cell / 2 + i * cell, cell / 2 + (boardSize - 1) * cell);
            }

            for (int y = 0; y < boardSize; y++) {
                for (int x = 0; x < boardSize; x++) {
                    Stone s = board.get(x, y);
                    if (s == Stone.EMPTY) continue;

                    int cx = cell / 2 + x * cell;
                    int cy = cell / 2 + y * cell;
                    int r = cell / 2 - 4;

                    if (s == Stone.BLACK) {
                        g.setColor(Color.BLACK);
                        g.fillOval(cx - r, cy - r, r * 2, r * 2);
                    } else {
                        g.setColor(Color.WHITE);
                        g.fillOval(cx - r, cy - r, r * 2, r * 2);
                        g.setColor(Color.BLACK);
                        g.drawOval(cx - r, cy - r, r * 2, r * 2);
                    }
                }
            }

            if (gameState == GameState.SCORING) {
                for (int y = 0; y < boardSize; y++) { 
                    for (int x = 0; x < boardSize; x++) 
                    { 
                        if (board.get(x, y) == Stone.EMPTY) continue; 
                        // DEAD - czerwony, ALIVE - zielony
                        Color overlay = board.isDead(x, y) ? new Color(255, 0, 0, 120) : new Color(0, 255, 0, 120); 
                        g2.setColor(overlay); 
                        int px = cell / 2 + x * cell - cell / 2; 
                        int py = cell / 2 + y * cell - cell / 2; 
                        g2.fillOval(px, py, cell, cell); 
                    } 
                }
            }

        }
    }
}