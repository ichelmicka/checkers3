package com.example.persistence;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "games")
public class GameEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String playerBlack;
    private String playerWhite;
    private Instant startedAt;
    private Instant finishedAt;
    private String result;

    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("moveNumber ASC")
    private List<MoveEntity> moves = new ArrayList<>();

    public GameEntity() {}

    public GameEntity(String playerBlack, String playerWhite) {
        this.playerBlack = playerBlack;
        this.playerWhite = playerWhite;
        this.startedAt = Instant.now();
    }

    public Long getId() { return id; }
    public String getPlayerBlack() { return playerBlack; }
    public void setPlayerBlack(String playerBlack) { this.playerBlack = playerBlack; }
    public String getPlayerWhite() { return playerWhite; }
    public void setPlayerWhite(String playerWhite) { this.playerWhite = playerWhite; }
    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }
    public Instant getFinishedAt() { return finishedAt; }
    public void setFinishedAt(Instant finishedAt) { this.finishedAt = finishedAt; }
    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }
    public List<MoveEntity> getMoves() { return moves; }

    public void addMove(MoveEntity m) {
        m.setGame(this);
        moves.add(m);
    }
}