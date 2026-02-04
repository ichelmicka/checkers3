package com.example.persistence;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "game_entity")
public class GameEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String black;
    private String white;

    private String result;

    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("moveNumber ASC")
    private List<MoveEntity> moves = new ArrayList<>();

    // getters / setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getBlack() { return black; }
    public void setBlack(String black) { this.black = black; }

    public String getWhite() { return white; }
    public void setWhite(String white) { this.white = white; }

    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<MoveEntity> getMoves() { return moves; }
    public void setMoves(List<MoveEntity> moves) { this.moves = moves; }

    public void addMove(MoveEntity m) {
        m.setGame(this);
        if (m.getMoveNumber() == null) {
            m.setMoveNumber(this.moves.size() + 1);
        }
        this.moves.add(m);
    }

}