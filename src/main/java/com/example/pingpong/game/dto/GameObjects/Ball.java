package com.example.pingpong.game.dto.GameObjects;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Ball {
    private double posX;
    private double posY;
    private double radius;
    protected double velocityX;
    protected double velocityY;

//    public Ball() {
//        // 기본 생성자
//    }

/*    public Ball(double x, double y, double radius) {
        this.x = x;
        this.y = y;
        this.radius = radius;
    }*/
//
//    public double getX() {
//        return x;
//    }
//
//    public void setX(double x) {
//        this.x = x;
//    }
//
//    public double getY() {
//        return y;
//    }
//
//    public void setY(double y) {
//        this.y = y;
//    }
//
//    public double getRadius() {
//        return radius;
//    }
//
//    public void setRadius(double radius) {
//        this.radius = radius;
//    }
}
