package com.example.pingpong.game.dto.GameInformations;

import com.example.pingpong.game.dto.GameElements.GameElement;
import com.example.pingpong.game.dto.GameObjects.*;
import com.example.pingpong.game.dto.GameObjects.sendDataDTO.GameRoomIdMessage;
import com.example.pingpong.game.dto.GameObjects.sendDataDTO.OneOnOneNormalGameDto;
import com.example.pingpong.game.dto.GameObjects.sendDataDTO.PaddleMoveData;
import com.example.pingpong.game.dto.MatchingResult;
import com.example.pingpong.game.service.GameResultsService;
import com.example.pingpong.user.dto.UserQueue;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class OneOnOneNormalGameInformation extends GameInformation {
    private int maxScore;
    private String[] userSocketIds;
    private GameElement gameElement;

    OneOnOneNormalGameInformation(MatchingResult matchingResult, SimpMessagingTemplate messagingTemplate, GameResultsService gameResultsService) {
        super(matchingResult, messagingTemplate, gameResultsService);
        this.maxScore = 2;
        this.userSocketIds = new String[2];
        settingUserSocketIds(matchingResult.getUserQueue());
        this.gameElement = new GameElement();
//        this.gameElement = super.getGameElement();
        settingGameElement();
    }

    private void settingGameElement() {
        this.gameElement = new GameElement();
        this.gameElement.addBall();
        this.gameElement.addPaddle(5, 40, 2, 15);
        this.gameElement.addPaddle(93, 40, 2, 15);
    }

    private void settingUserSocketIds(ConcurrentLinkedQueue<UserQueue> userQueue) {
        this.userSocketIds = new String[2];
        Iterator<UserQueue> iterator = userQueue.iterator();
        for (int i = 0; i < 2; i++) {
            this.userSocketIds[i] = iterator.next().getSocketId();
        }
    }

    public void positionUpdate(String roomName, String resultId) {
        System.out.println("/position_update/" + roomName);
        Ball ball = this.gameElement.getBallList().get(0);
        this.gameElement.getPaddleList().get(0).update();
        this.gameElement.getPaddleList().get(1).update();
        ball.update();
        ball.collision(gameElement);
        boolean gameFinished = gameScoreCheck();
        messagingTemplate.convertAndSend("/topic/position_update/" + roomName, new OneOnOneNormalGameDto(this.gameElement));
        if (gameFinished)
            finishGame(roomName, resultId);
    }

    public void paddleMove(SimpMessageHeaderAccessor accessor, PaddleMoveData data) {
        System.out.println("/paddle_move/" + data.getGameRoomId() + " " + accessor.getSessionId());
        if (accessor.getSessionId().equals(userSocketIds[0])) {
            System.out.println("left paddle move");
            this.gameElement.getPaddleList().get(0).setPaddleStatus(data.getPaddleStatus());
        } else {
            System.out.println("right paddle move");
            this.gameElement.getPaddleList().get(1).setPaddleStatus(data.getPaddleStatus());
        }
    }

    private boolean gameScoreCheck() {
        Ball ball = gameElement.getBallList().get(0);
        Integer leftScore = gameElement.getScore().getLeftScore();
        Integer rightScore = gameElement.getScore().getRightScore();

        if (ball.getPosX() < 0 || ball.getPosX() + ball.getRadius() * 2 > 100) {
            if (ball.getPosX() < 0) {
                this.gameElement.getScore().setRightScore(rightScore + 1);
            } else {
                this.gameElement.getScore().setLeftScore(leftScore + 1);
            }
            ball.resetPosition();
        }

        return this.gameElement.getScore().getLeftScore() == this.maxScore || this.gameElement.getScore().getRightScore() == this.maxScore;
    }



    private void finishGame(String roomName, String resultId) {
        if (this.getTimer() != null) {
            this.getTimer().cancel(true);
        }
        String winnerSocketId = this.userSocketIds[this.gameElement.getScore().getLeftScore() == this.maxScore ? 0 : 1];
        gameResultsService.finishGame(roomName, resultId);
        System.out.println("left score: " + this.gameElement.getScore().getLeftScore() + ", right score: " + this.gameElement.getScore().getRightScore());
        messagingTemplate.convertAndSend("/topic/finish_game/" + roomName, 0);
        System.out.println("game finished");
    }

    @Override
    public String getWinnerSocketId() {
        if (gameElement.getScore().getLeftScore() == maxScore) {
            return this.userSocketIds[0];
        } else if (gameElement.getScore().getRightScore() == maxScore) {
            return this.userSocketIds[1];
        } else {
            return null;
        }
    }

    @Override
    public String getLoserSocketId() {
        if (gameElement.getScore().getLeftScore() == maxScore) {
            return this.userSocketIds[1];
        } else if (gameElement.getScore().getRightScore() == maxScore) {
            return this.userSocketIds[0];
        } else {
            return null;
        }
    }

    @Override
    public Integer getWinnerScore() {
        return Math.max(gameElement.getScore().getLeftScore(), gameElement.getScore().getRightScore());
    }

    @Override
    public Integer getLoserScore() {
        return Math.min(gameElement.getScore().getLeftScore(), gameElement.getScore().getRightScore());
    }


    public void reStart(SimpMessageHeaderAccessor accessor, GameRoomIdMessage data, ScheduledExecutorService executorService) {
        System.out.println("reStart");
        if (accessor.getSessionId().equals(userSocketIds[0])) {
            gameElement.getScore().setLeftScore(-1);
        } else {
            gameElement.getScore().setRightScore(-1);
        }
        if (gameElement.getScore().getLeftScore() == -1 && gameElement.getScore().getRightScore() == -1) {
            System.out.println("reStart game");
            String gameRoomId = data.getGameRoomId();
            messagingTemplate.convertAndSend("/topic/restart_game/" + gameRoomId, 0);
            gameElement.getScore().setLeftScore(0);
            gameElement.getScore().setRightScore(0);
            gameElement.getBallList().get(0).resetPosition();
            // 여기서 DB에 있는 resultId를 가져와야함
            // String gameResultsId = gameResultsService.getGameResultsId(gameRoomId);
            String gameResultsId = "1123";
            Runnable positionUpdateTask = () -> positionUpdate(gameRoomId, gameResultsId);
            long initialDelay = 0;
            long period = 1000 / 60;
            ScheduledFuture<?> timer = executorService.scheduleAtFixedRate(positionUpdateTask, initialDelay, period, TimeUnit.MILLISECONDS);
            //게임 result DB 다시 만들어야함
        }
    }

    public void exitUser(SimpMessageHeaderAccessor accessor, GameRoomIdMessage data) {
        String remainSocketId = accessor.getSessionId().equals(userSocketIds[0]) ? userSocketIds[1] : userSocketIds[0];
        System.out.println("exit user: " + accessor.getSessionId() + ", remain user: " + remainSocketId);
        messagingTemplate.convertAndSend("/topic/exit_game/" + data.getGameRoomId(), 0);
    }
}
