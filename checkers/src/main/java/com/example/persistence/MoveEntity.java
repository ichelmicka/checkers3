package com.example.persistence;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "moves")
public class MoveEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int moveNumber;
    private int fromRow;
    private int fromCol;
    private int toRow;
    private int toCol;

    private boolean capture;
    @Column(length = 2000)
    private String extra; 

    private Instant createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id")
    private GameEntity game;

    public MoveEntity() {}

    public MoveEntity(int moveNumber, int fromRow, int fromCol, int toRow, int toCol, boolean capture, String extra) {
        this.moveNumber = moveNumber;
        this.fromRow = fromRow;
        this.fromCol = fromCol;
        this.toRow = toRow;
        this.toCol = toCol;
        this.capture = capture;
        this.extra = extra;
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public int getMoveNumber() { return moveNumber; }
    public void setMoveNumber(int moveNumber) { this.moveNumber = moveNumber; }
    public int getFromRow() { return fromRow; }
    public void setFromRow(int fromRow) { this.fromRow = fromRow; }
    public int getFromCol() { return fromCol; }
    public void setFromCol(int fromCol) { this.fromCol = fromCol; }
    public int getToRow() { return toRow; }
    public void setToRow(int toRow) { this.toRow = toRow; }
    public int getToCol() { return toCol; }
    public void setToCol(int toCol) { this.toCol = toCol; }
    public boolean isCapture() { return capture; }
    public void setCapture(boolean capture) { this.capture = capture; }
    public String getExtra() { return extra; }
    public void setExtra(String extra) { this.extra = extra; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public GameEntity getGame() { return game; }
    public void setGame(GameEntity game) { this.game = game; }
}