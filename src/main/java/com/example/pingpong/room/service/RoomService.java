package com.example.pingpong.room.service;

import com.example.pingpong.global.util.UUIDGenerator;
import com.example.pingpong.room.dto.RoomsRequest;
import com.example.pingpong.game.dto.GameMode;
import com.example.pingpong.room.model.Rooms;
import com.example.pingpong.room.model.RoomStatus;
import com.example.pingpong.room.model.RoomType;
import com.example.pingpong.room.repository.RoomRepository;
import com.example.pingpong.user.model.User;
import com.example.pingpong.user.model.UserStatus;
import com.example.pingpong.user.repository.UserRepository;
import com.example.pingpong.user.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    public List<RoomsRequest> findAllRooms() {
        List<RoomsRequest> roomsRequestList = roomRepository.findAllByIsPrivate(true, RoomStatus.WAIT).stream().map(
                rooms -> RoomsRequest.builder()
                        .roomId(rooms.getRoomId())
                        .roomName(rooms.getRoomName())
                        .ownerNickname(rooms.getRoomOwnerNickname())
                        .build()
                ).collect(Collectors.toList());
        return roomsRequestList;
    }

    public Rooms createNewRoom( String userNickname, GameMode gameMode, RoomType roomType ) {
        String roomId = UUIDGenerator.Generate("ROOM");
        String roomName = userNickname + "'s room";
        Rooms room = buildRooms(userNickname, roomId, roomName, roomType);
        userService.updateUserStatus(userNickname, UserStatus.PENDING);
        return roomRepository.save(room);
    }

    public Rooms createMatchingRoom ( String userNickname, GameMode gameMode, RoomType roomType, Integer currentUserSize ) {
        String roomId = UUIDGenerator.Generate("ROOM");
        String roomName = userNickname + "'s With AI Room";
        Rooms room = Rooms.builder().roomId(roomId)
                .roomName(roomName)
                .roomOwnerNickname(userNickname)
                .curPlayersCnt(currentUserSize).maxPlayers(currentUserSize).startedDateTime(LocalDateTime.now())
                .roomStatus(RoomStatus.ONGOING).gameMode(gameMode).roomType(roomType).build();
        return roomRepository.save(room);
    }

    public User joinRooms(String roomId, String userNickname) throws Exception {
        Rooms Rooms = roomRepository.findByRoomId(roomId).get();
        Rooms.setCurPlayersCnt(Rooms.getCurPlayersCnt() + 1);

        User user = userRepository.findByNickname(userNickname).get();
        userService.updateUserStatus(user.getNickname(), UserStatus.IN_GAME);
        return user;
    }

    public User exitRooms(String roomId, String userNickname) throws Exception{
        Rooms Rooms = roomRepository.findByRoomId(roomId).get();
        Rooms.setCurPlayersCnt(Rooms.getCurPlayersCnt() - 1);

        User user = userRepository.findByNickname(userNickname).get();
        userService.updateUserStatus(user.getNickname(), UserStatus.ONLINE);
        return user;
    }

    public void deleteRooms(String roomId) {
        roomRepository.deleteByRoomId(roomId);
    }

    public Rooms updateRooms ( String roomId, String userNickname, RoomsRequest updateRequest ) {
        Optional<Rooms> optionalRooms = roomRepository.findByRoomId(roomId);
        if (!optionalRooms.isPresent()) {
            throw new EntityNotFoundException("Rooms with id " + roomId + " not found!");
        }

        Rooms Rooms = optionalRooms.get();
        Rooms.setRoomType(updateRequest.getRoomType());
        Rooms.setGameMode(updateRequest.getGameMode());
        Rooms.setMaxPlayers(updateRequest.getMaxPlayers());

        return roomRepository.save(Rooms);
    }

    private Rooms buildRooms(String userNickname, String roomId, String roomName, RoomType roomType) {
        return Rooms.builder()
                .roomId(roomId)
                .roomName(roomName)
                .roomOwnerNickname(userNickname)
                .roomStatus(RoomStatus.WAIT)
                .roomType(roomType)
                .build();
    }
}
