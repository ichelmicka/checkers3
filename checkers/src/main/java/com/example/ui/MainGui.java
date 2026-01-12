package com.example.ui;

import com.example.model.Board;
import com.example.model.MoveResult;
import com.example.model.Position;
import com.example.model.Stone;
import com.example.rules.GoRules;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;

/**
 * GUI w Swing do gry (plansza, klikanie, wyświetlanie komunikatów).
 */
public class MainGui {
    private final int boardSize = 9;
    private Board board;
    private final GoRules rules = new GoRules();
    private Stone currentPlayer = Stone.BLACK;

    private final int cellSize = 48; // piksele
    private final int margin = 20;

    private JFrame frame;
    private BoardPanel boardPanel;
    private JLabel statusLabel;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainGui().createAndShowGui());
    }

    public MainGui() {
        board = new Board(boardSize);
    }

    private void createAndShowGui() {
        frame = new JFrame("Go / Checkers GUI");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        boardPanel = new BoardPanel();
        boardPanel.setPreferredSize(new Dimension(boardSize * cellSize + margin * 2, boardSize * cellSize + margin * 2));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton passBtn = new JButton("Pass");
        JButton scoreBtn = new JButton("Score");
        JButton resetBtn = new JButton("Reset");

        passBtn.addActionListener(e -> pass());
        scoreBtn.addActionListener(e -> showScore());
        resetBtn.addActionListener(e -> resetBoard());

        top.add(new JLabel("Player:"));
        statusLabel = new JLabel(currentPlayer == Stone.BLACK ? "BLACK" : "WHITE");
        top.add(statusLabel);
        top.add(passBtn);
        top.add(scoreBtn);
        top.add(resetBtn);

        frame.add(top, BorderLayout.NORTH);
        frame.add(boardPanel, BorderLayout.CENTER);

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void pass() {
        currentPlayer = (currentPlayer == Stone.BLACK) ? Stone.WHITE : Stone.BLACK;
        statusLabel.setText(currentPlayer == Stone.BLACK ? "BLACK" : "WHITE");
    }

    private void resetBoard() {
        board = new Board(boardSize);
        currentPlayer = Stone.BLACK;
        statusLabel.setText("BLACK");
        boardPanel.repaint();
    }

    private void showScore() {
        TerritoryScorerLocal.Score s = TerritoryScorerLocal.score(board);
        String msg = String.format("Black: stones=%d territory=%d total=%d\nWhite: stones=%d territory=%d total=%d",
                s.blackStones, s.blackTerritory, s.blackScore,
                s.whiteStones, s.whiteTerritory, s.whiteScore);
        JOptionPane.showMessageDialog(frame, msg, "Score", JOptionPane.INFORMATION_MESSAGE);
    }

    // panel rysujący planszę
    private class BoardPanel extends JPanel {
        public BoardPanel() {
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    int x = (e.getX() - margin) / cellSize;
                    int y = (e.getY() - margin) / cellSize;
                    if (x < 0 || x >= boardSize || y < 0 || y >= boardSize) return;

                    MoveResult mr = rules.applyMove(board, x, y, currentPlayer);
                    if (!mr.isOk()) {
                        JOptionPane.showMessageDialog(frame, mr.getErrorMessage(), "Invalid move", JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    // update board to snapshot returned by rules
                    board = mr.getBoardSnapshot();
                    currentPlayer = (currentPlayer == Stone.BLACK) ? Stone.WHITE : Stone.BLACK;
                    statusLabel.setText(currentPlayer == Stone.BLACK ? "BLACK" : "WHITE");
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // background
            g2.setColor(new Color(245, 222, 179)); // light wood
            g2.fillRect(0, 0, getWidth(), getHeight());

            // grid
            g2.setColor(Color.BLACK);
            for (int i = 0; i < boardSize; i++) {
                int xi = margin + i * cellSize + cellSize / 2;
                int yi = margin + i * cellSize + cellSize / 2;
                g2.drawLine(margin + cellSize/2, yi, margin + (boardSize-1) * cellSize + cellSize/2, yi);
                g2.drawLine(xi, margin + cellSize/2, xi, margin + (boardSize-1) * cellSize + cellSize/2);
            }
            // stones
            for (int y = 0; y < boardSize; y++) {
                for (int x = 0; x < boardSize; x++) {
                    Stone s = board.get(x, y);
                    if (s == Stone.EMPTY) continue;
                    int cx = margin + x * cellSize + cellSize/2;
                    int cy = margin + y * cellSize + cellSize/2;
                    int r = cellSize/2 - 6;
                    if (s == Stone.BLACK) {
                        g2.setColor(Color.BLACK);
                        g2.fillOval(cx - r, cy - r, r * 2, r * 2);
                    } else if (s == Stone.WHITE) {
                        g2.setColor(Color.WHITE);
                        g2.fillOval(cx - r, cy - r, r * 2, r * 2);
                        g2.setColor(Color.BLACK);
                        g2.drawOval(cx - r, cy - r, r * 2, r * 2);
                    }
                }
            }
        }
    }

    // lokalny scorer
    private static final class TerritoryScorerLocal {
        private TerritoryScorerLocal() {}

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

        public static final class Score {
            public final int blackStones;
            public final int whiteStones;
            public final int blackTerritory;
            public final int whiteTerritory;
            public final int blackScore;
            public final int whiteScore;

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