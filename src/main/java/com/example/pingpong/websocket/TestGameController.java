package com.example.pingpong.websocket;

import com.example.pingpong.game.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Controller
public class TestGameController {
//    private final SimpMessagingTemplate messagingTemplate;
//    public TestGameController(SimpMessagingTemplate messagingTemplate) {
//        this.messagingTemplate = messagingTemplate;
//    }
//    private static List<String> list = new ArrayList<>();
////    private static Map<String, List<String>> matchUsers = new HashMap<>();
//    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
//
//    private static Map<String, GameInfomation> gameRooms = new HashMap<>();
//    @MessageMapping("/SingleMode")
//    public void singlePlay(SimpMessageHeaderAccessor accessor) {
//        System.out.println("single play accessor: " + accessor.getSessionId());
//        List<String> dummyList = new ArrayList<>();
//        dummyList.add(accessor.getSessionId());
//        dummyList.add("ai");
//        matchingSuccess(dummyList);
//    }
//
//    @MessageMapping("/matchingJoin")
//    public void test(SimpMessageHeaderAccessor accessor) {
//        System.out.println("matching join accessor: " + accessor.getSessionId());
//        list.add(accessor.getSessionId());
//        // 2명이면 게임방 만들고 2명에 이벤트 전송 후 list 초기화
//        if (list.size() == 2) {
//            System.out.println("matching success 2 User");
//            matchingSuccess(list);
//            list.clear();
//        }
//    }
//
//    @MessageMapping("/cancelMatching")
//    public void cancelMatching(SimpMessageHeaderAccessor accessor) {
//        System.out.println("matching cancle aceessor: " + accessor.getSessionId());
//        list.remove(accessor.getSessionId());
//    }
//
//    @SendTo("/topic/matching-success")
//    public void matchingSuccess(List<String> userList) {
//        String gameRoomId = String.valueOf(System.currentTimeMillis());
//        System.out.println(gameRoomId);
//        ObjectMapper objectMapper = new ObjectMapper();
//        ObjectNode jsonObject = objectMapper.createObjectNode();
//        jsonObject.put("gameRoomId", gameRoomId);
////        matchUsers.put(gameRoomId, userList);
//        String destination = "/topic/matching-success";
//        this.gameRooms.put(gameRoomId, new GameInfomation());
//        this.gameRooms.get(gameRoomId).setUser(0, userList.get(0));
//        this.gameRooms.get(gameRoomId).setUser(1, userList.get(1));
//        if (userList.get(1).equals("ai")) {
//            System.out.println("혼자니?");
//            jsonObject.put("ai", true);
//            this.gameRooms.get(gameRoomId).setSingleMode(true);
//        }
//        Runnable positionUpdateTask = () -> {
//            positionUpdate(gameRoomId);
//        };
//        long initialDelay = 0;
//        long period = 1000 / 60;
//        ScheduledFuture<?> timer = executorService.scheduleAtFixedRate(positionUpdateTask, initialDelay, period, TimeUnit.MILLISECONDS);
//        this.gameRooms.get(gameRoomId).setTimer(timer);
//        messagingTemplate.convertAndSend(destination, jsonObject);
//    }
//
//
//    // 여기서 부터는 게임 관련 로직 ////////
//
//
//    @MessageMapping("/positionUpdate")
//    public void positionUpdate(String roomName) {
//        GameInfomation gameRoom = gameRooms.get(roomName);
//        ballUpdate(gameRoom);
//        ballCollision(gameRoom);
//        boolean gameFinished = gameScoreCheck(gameRoom);
//        messagingTemplate.convertAndSend("/topic/position_update/" + roomName, gameRoom.getElement());
//        if (gameFinished)
//            finishGame(gameRoom, roomName);
//    }
//
//    @MessageMapping("/paddle_move")
//    public void paddleMove(SimpMessageHeaderAccessor accessor, PaddleMoveData data) {
//        System.out.println("/paddle_move/" + data.getGameRoomId() + " " + data.getPaddleStatus());
//        GameInfomation gameRoom = gameRooms.get(data.getGameRoomId());
//        if (gameRoom == null) {
//            return;
//        }
//        if (accessor.getSessionId().equals(gameRoom.getUser(0))) {
//            gameRoom.setLeftPaddleStatus(data.getPaddleStatus());
//        } else {
//            gameRoom.setRightPaddleStatus(data.getPaddleStatus());
//        }
//    }
//
//
//    private void ballUpdate(GameInfomation gameRoom) {
//        GameElement element = gameRoom.getElement();
//        Ball ball = element.getBall();
//
//        updatePaddlePosition(gameRoom.getLeftPaddleStatus(), element.getLeftPaddle());
//        if (gameRoom.getSingleMode()) {
//            updateAiPaddlePosition(gameRoom.getRightPaddleStatus(), element.getRightPaddle(), ball);
//        } else
//            updatePaddlePosition(gameRoom.getRightPaddleStatus(), element.getRightPaddle());
//        ball.setX(ball.getX() + gameRoom.getVelocityX());
//        ball.setY(ball.getY() + gameRoom.getVelocityY());
//
//        double velocityX = gameRoom.getVelocityX();
//        double velocityY = gameRoom.getVelocityY();
//        gameRoom.setVelocityX(velocityX < 0 ? velocityX - 0.01 : velocityX + 0.01);
//        gameRoom.setVelocityY(velocityY < 0 ? velocityY - 0.01 : velocityY + 0.01);
//    }
//
//    private void ballCollision(GameInfomation gameRoom) {
//        Ball ball = gameRoom.getElement().getBall();
//        Paddle leftPaddle = gameRoom.getElement().getLeftPaddle();
//        Paddle rightPaddle = gameRoom.getElement().getRightPaddle();
//
//        // 천장과 바닥 충돌 확인
//        if (ball.getY() - ball.getRadius() < 0) {
//            ball.setY(ball.getRadius());
//            gameRoom.setVelocityY(-gameRoom.getVelocityY());
//        } else if (ball.getY() + ball.getRadius() > 100) {
//            ball.setY(100 - ball.getRadius());
//            gameRoom.setVelocityY(-gameRoom.getVelocityY());
//        }
//
//        // 패들과 공의 충돌 확인
//        if (ball.getX() - ball.getRadius() < leftPaddle.getX() + leftPaddle.getWidth() &&
//                ball.getX() + ball.getRadius() > leftPaddle.getX() &&
//                ball.getY() + ball.getRadius() >= leftPaddle.getY() &&
//                ball.getY() - ball.getRadius() <= leftPaddle.getY() + leftPaddle.getHeight()) {
//            double deltaY = ball.getY() - (leftPaddle.getY() + leftPaddle.getHeight() / 2);
//            if (ball.getX() + ball.getRadius() > leftPaddle.getX() + leftPaddle.getWidth() / 2) {
//                gameRoom.setVelocityX(gameRoom.getVelocityX() < 0 ? -gameRoom.getVelocityX() : gameRoom.getVelocityX());
//            }
//            gameRoom.setVelocityY(deltaY * 0.2);
//        }
//
//        if (ball.getX() + ball.getRadius() > rightPaddle.getX() &&
//                ball.getX() - ball.getRadius() < rightPaddle.getX() + rightPaddle.getWidth() &&
//                ball.getY() + ball.getRadius() >= rightPaddle.getY() &&
//                ball.getY() - ball.getRadius() <= rightPaddle.getY() + rightPaddle.getHeight()) {
//            double deltaY = ball.getY() - (rightPaddle.getY() + rightPaddle.getHeight() / 2);
//            if (ball.getX() - ball.getRadius() < rightPaddle.getX() + rightPaddle.getWidth() / 2) {
//                gameRoom.setVelocityX(gameRoom.getVelocityX() > 0 ? -gameRoom.getVelocityX() : gameRoom.getVelocityX());
//            }
//            gameRoom.setVelocityY(deltaY * 0.2);
//        }
//    }
//
//    private void updatePaddlePosition(int status, Paddle paddle) {
//        double speed = 3.0;
//        if (status == 1 && paddle.getY() > 0) {
//            paddle.setY(paddle.getY() - speed);
//        } else if (status == 2 && paddle.getY() < 100 - paddle.getHeight()) {
//            paddle.setY(paddle.getY() + speed);
//        }
//    }
//    private void updateAiPaddlePosition(int status, Paddle paddle, Ball ball) {
//        if (ball.getY() + ball.getRadius() < paddle.getY() + paddle.getHeight() / 2) {
//            // 공이 패들의 위쪽에 있으면, 패들을 위로 움직입니다.
//            double speed = 3.0;
//            if (paddle.getY() > 0) {
//                paddle.setY(paddle.getY() - speed);
//            }
//        } else {
//            // 공이 패들의 아래쪽에 있으면, 패들을 아래로 움직입니다.
//            double speed = 3.0;
//            if (paddle.getY() < 100 - paddle.getHeight()) {
//                paddle.setY(paddle.getY() + speed);
//            }
//        }
//    }
//
//    private boolean gameScoreCheck(GameInfomation gameRoom) {
//        Ball ball = gameRoom.getElement().getBall();
//        Integer leftScore = gameRoom.getElement().getLeftScore();
//        Integer rightScore = gameRoom.getElement().getRightScore();
//
//        if (ball.getX() < 0 || ball.getX() + ball.getRadius() * 2 > 100) {
//            if (ball.getX() < 0) {
//                gameRoom.getElement().setRightScore(rightScore + 1);
//            } else {
//                gameRoom.getElement().setLeftScore(leftScore + 1);
//            }
//            resetBallPosition(gameRoom);
//        }
//
//        return gameRoom.getElement().getLeftScore() == gameRoom.getMaxScore() || gameRoom.getElement().getRightScore() == gameRoom.getMaxScore();
//    }
//
//    private void resetBallPosition(GameInfomation gameRoom) {
//        Ball ball = gameRoom.getElement().getBall();
//        ball.setX(50 - ball.getRadius());
//        ball.setY(50 - ball.getRadius());
//
//        double directX = Math.random() < 0.5 ? -1.0 : 1.0;
//        double directY = Math.random() < 0.5 ? -1.0 : 1.0;
//
//        gameRoom.setVelocityX(0.5 * directX);
//        gameRoom.setVelocityY(0.25 * directY);
//    }
//
//    private void finishGame(GameInfomation gameRoom, String roomName) {
//        if (gameRoom.getTimer() != null) {
//            gameRoom.getTimer().cancel(false);
//        }
//        System.out.println("game finished");
//        System.out.println("left score: " + gameRoom.getElement().getLeftScore() + ", right score: " + gameRoom.getElement().getRightScore());
//        messagingTemplate.convertAndSend( "/topic/finish_game" + roomName, 0);
//    }
}
