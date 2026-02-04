package com.example.persistence;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "move_entity")
public class MoveEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id")
    private GameEntity game;

    private Integer moveNumber;

    private Integer fromRow;
    private Integer fromCol;
    private Integer toRow;
    private Integer toCol;

    private Boolean capture;

    @Column(columnDefinition = "TEXT")
    private String extra;

    private LocalDateTime createdAt = LocalDateTime.now();

    // getters / setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public GameEntity getGame() { return game; }
    public void setGame(GameEntity game) { this.game = game; }

    public Integer getMoveNumber() { return moveNumber; }
    public void setMoveNumber(Integer moveNumber) { this.moveNumber = moveNumber; }

    public Integer getFromRow() { return fromRow; }
    public void setFromRow(Integer fromRow) { this.fromRow = fromRow; }

    public Integer getFromCol() { return fromCol; }
    public void setFromCol(Integer fromCol) { this.fromCol = fromCol; }

    public Integer getToRow() { return toRow; }
    public void setToRow(Integer toRow) { this.toRow = toRow; }

    public Integer getToCol() { return toCol; }
    public void setToCol(Integer toCol) { this.toCol = toCol; }

    public Boolean getCapture() { return capture; }
    public void setCapture(Boolean capture) { this.capture = capture; }

    public String getExtra() { return extra; }
    public void setExtra(String extra) { this.extra = extra; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}