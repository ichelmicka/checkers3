package com.example.ui;

import com.example.model.Board;
import com.example.model.MoveResult;
import com.example.model.Position;
import com.example.model.Stone;
import com.example.rules.TerritoryScorer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;


/**
 * GUI w Swing do gry:
 * - plansza rysowana Swingiem,
 * - obsługa kliknięć i przycisków,
 * - licznik terytorium.
 */
public class MainGui {

    private final int boardSize = 19;
    private Board board = new Board(boardSize);
    private Stone myColor = Stone.EMPTY;
    private Stone currentTurn = Stone.BLACK;

    private JButton passButton;
    private JButton resgnButton;

    private JFrame frame;
    private BoardPanel boardPanel;
    private JPanel sidePanel;
    private JPanel bottom;
    private JLabel myColorLabel;
    private JLabel statusLabel;
    private JLabel connectionLabel;
    private JLabel scoreLabel;

    //private final int cellSize = 48; // piksele
    //private final int margin = 20;


    private GoClient client;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainGui().start());
    }

    public void start() {
        connectToServer();
        createGui();
    }

    private void connectToServer() {
        client = new GoClient("localhost", 8888, this::onServerMessage, this::onServerError);

        client.send("JOIN Player1");
    }

    private void createGui() {
        frame = new JFrame("Go Client");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // PANEL GÓRNY (status połączenia) ---
        statusLabel = new JLabel("Connecting...");
        JPanel top = new JPanel();
        top.add(statusLabel);
        frame.add(top, BorderLayout.NORTH);

        // PANEL PLANSZY ---
        boardPanel = new BoardPanel();
        boardPanel.setPreferredSize(new Dimension(600, 600));
        frame.add(boardPanel, BorderLayout.CENTER);

        // PANEL BOCZNY (kolor gracza, tura, itp.) ---
        JPanel sidePanel = new JPanel();
        sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.Y_AXIS));
        sidePanel.setPreferredSize(new Dimension(180, 400));

        // PANEL DOLNY (wynik)
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton passButton = new JButton("PASS"); 
        passButton.addActionListener(e -> sendPass());
        bottom.add(passButton);

        JButton rsgnButton = new JButton("RESIGN"); 
        rsgnButton.addActionListener(e -> sendResign());
        bottom.add(rsgnButton);

        scoreLabel = new JLabel("Score: ?");
        bottom.add(scoreLabel);

        frame.add(bottom, BorderLayout.SOUTH);

        //boczny panel
        myColorLabel = new JLabel("You are: ?");
        connectionLabel = new JLabel("Status: Connecting...");

        sidePanel.add(myColorLabel);
        sidePanel.add(Box.createVerticalStrut(10));
        sidePanel.add(Box.createVerticalStrut(10));
        sidePanel.add(connectionLabel);
        sidePanel.add(Box.createVerticalStrut(10));

        frame.add(sidePanel, BorderLayout.EAST);

        

        // WYŚWIETLENIE OKNA ---
        frame.pack();
        frame.setVisible(true);

    }

    //obsluga wiadomosci z serwera

    private void onServerMessage(String msg) {
        System.out.println("SERVER: " + msg);

        if (msg.startsWith("ASSIGN")) {
            // ASSIGN <playerId> <color>
            String[] p = msg.split(" ");
            myColor = Stone.valueOf(p[2]);
            SwingUtilities.invokeLater(() -> {
                myColorLabel.setText("You are: " + myColor);
                connectionLabel.setText("Status: Connected");
            });
        }

        else if (msg.startsWith("INFO")) {
            SwingUtilities.invokeLater(() -> statusLabel.setText(msg.substring(5)));
        }

        else if (msg.startsWith("ERROR")) {
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(frame, msg, "Error", JOptionPane.ERROR_MESSAGE));
        }
        else if (msg.startsWith("WINNER")) {
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(frame, msg));
        }
        else if (msg.startsWith("BOARD")) {
            // następne linie to tekst planszy
            readBoardFromServer();
            SwingUtilities.invokeLater(this::showScore);
        }
    }

    private void showScore() {
        if (board == null) return;
        TerritoryScorer.Score s = TerritoryScorer.score(board);
        String msg = String.format("Score:\nBlack: stones=%d territory=%d total=%d\nWhite: stones=%d territory=%d total=%d", s.blackStones, s.blackTerritory, s.blackScore, s.whiteStones, s.whiteTerritory, s.whiteScore);
        scoreLabel.setText(msg);
    }

    private void sendPass() {
        client.send("PASS");
    }

    private void sendResign() {
        client.send("RESIGN");
    }


    private void readBoardFromServer() {
        try {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < boardSize; i++) {
                sb.append(client.getReader().readLine()).append("\n");
            }
            board = Board.fromString(sb.toString());
            SwingUtilities.invokeLater(boardPanel::repaint);
        } catch (Exception e) {
            onServerError("Failed to read board: " + e.getMessage());
        }
    }

    private void onServerError(String msg) {
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(frame, msg, "Connection error", JOptionPane.ERROR_MESSAGE));
    }

    //panel rysujacy plansze

    private class BoardPanel extends JPanel {
        public BoardPanel() {
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (myColor == Stone.EMPTY) return;

                    int cellSize = getWidth() / boardSize;
                    int x = e.getX() / cellSize;
                    int y = e.getY() / cellSize;

                    client.send("MOVE " + x + " " + y);
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

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
        }
    }


    /**
     * Lokalny scorer.
     * Liczy kamienie oraz flood-fill pustych regionów.
     * Region otoczony przez tylko jeden kolor przypisuje punkt temu kolorowi.
     */
    private static final class TerritoryScorerLocal {
        private TerritoryScorerLocal() {}

        /** 
         * Oblicza wynik dla danej planszy.
         * 
         * @param board plansza do oceny
         * @return struktura Score zawierająca liczbę kamieni, terytorium i wynik
         */
        public static Score score(Board board) {
            int size = board.getSize();
            boolean[][] visited = new boolean[size][size];

            int blackStones = 0;
            int whiteStones = 0;
            int blackTerritory = 0;
            int whiteTerritory = 0;

            for (int y = 0; y < size; y++) {
                for (int x = 0; x < size; x++) {
                    Stone s = board.get(x, y);
                    if (s == Stone.BLACK) blackStones++;
                    else if (s == Stone.WHITE) whiteStones++;
                }
            }

            for (int y = 0; y < size; y++) {
                for (int x = 0; x < size; x++) {
                    if (visited[y][x]) continue;
                    if (board.get(x, y) != Stone.EMPTY) continue;

                    ArrayDeque<Position> stack = new ArrayDeque<>();
                    stack.push(new Position(x, y));
                    visited[y][x] = true;

                    int regionSize = 0;
                    EnumSet<Stone> bordering = EnumSet.noneOf(Stone.class);

                    while (!stack.isEmpty()) {
                        Position p = stack.pop();
                        regionSize++;
                        List<Position> neigh = board.getNeighbours(p.x, p.y);
                        for (Position n : neigh) {
                            Stone ns = board.get(n.x, n.y);
                            if (ns == Stone.EMPTY) {
                                if (!visited[n.y][n.x]) {
                                    visited[n.y][n.x] = true;
                                    stack.push(new Position(n.x, n.y));
                                }
                            } else {
                                bordering.add(ns);
                            }
                        }
                    }

                    if (bordering.size() == 1) {
                        Stone owner = bordering.iterator().next();
                        if (owner == Stone.BLACK) blackTerritory += regionSize;
                        else if (owner == Stone.WHITE) whiteTerritory += regionSize;
                    }
                }
            }

            int blackScore = blackStones + blackTerritory;
            int whiteScore = whiteStones + whiteTerritory;
            return new Score(blackStones, whiteStones, blackTerritory, whiteTerritory, blackScore, whiteScore);
        }

        /** Wynik. */
        public static final class Score {
            public final int blackStones;
            public final int whiteStones;
            public final int blackTerritory;
            public final int whiteTerritory;
            public final int blackScore;
            public final int whiteScore;

            /**
             * Konstruktor wyniku.
             * 
             * @param blackStones liczba czarnych kamieni na planszy
             * @param whiteStones liczba białych kamieni na planszy
             * @param blackTerritory liczba pól terytorialnych czarnego gracza
             * @param whiteTerritory liczba pól terytorialnych białego gracza
             * @param blackScore suma czarnych
             * @param whiteScore syma białych
             */
            public Score(int blackStones, int whiteStones, int blackTerritory, int whiteTerritory, int blackScore, int whiteScore) {
                this.blackStones = blackStones;
                this.whiteStones = whiteStones;
                this.blackTerritory = blackTerritory;
                this.whiteTerritory = whiteTerritory;
                this.blackScore = blackScore;
                this.whiteScore = whiteScore;
            }
        }
    }
}