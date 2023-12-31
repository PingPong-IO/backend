package com.example.pingpong.game.dto.GameObjects;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Score {
    Integer leftScore;
    Integer rightScore;
    Integer rankScore;

    public Score() {
        this.leftScore = 0;
        this.rightScore = 0;
        this.rankScore = 0;
    }

    public void leftScoreUp() {
        this.leftScore++;
    }

    public void rightScoreUp() {
        this.rightScore++;
    }

    public void rankScoreUp() {
        this.rankScore++;
    }
}
