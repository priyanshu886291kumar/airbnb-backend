package com.priyanshu886291kumar.projects.AirBnb_SpringBoot.controllers.Admin;

import com.priyanshu886291kumar.projects.AirBnb_SpringBoot.dto.Room.RoomDTO;
import com.priyanshu886291kumar.projects.AirBnb_SpringBoot.service.room.IRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/hotels/{hotelId}/rooms")
@RequiredArgsConstructor
public class RoomsAdminController {
    private final IRoomService iRoomService;

    @PostMapping
    public ResponseEntity<RoomDTO>createNewRoom(@PathVariable Long hotelId, @RequestBody RoomDTO roomDTO){
        RoomDTO room = iRoomService.createNewRoom(hotelId,roomDTO);
        return new ResponseEntity<>(room, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<RoomDTO>>getAllRoomsinHotel(@PathVariable Long hotelId){
        return  ResponseEntity.ok(iRoomService.getAllRoomsInHotel(hotelId));
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<RoomDTO>getRoomById(@PathVariable Long roomId, @PathVariable Long hotelId){
        return ResponseEntity.ok(iRoomService.getRoomById(roomId));
    }

    @DeleteMapping("/{roomId}")
    public ResponseEntity<Void>deleteRoomById(@PathVariable Long roomId, @PathVariable Long hotelId){
        iRoomService.deleteRoomById(roomId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{roomId}")
    public ResponseEntity<RoomDTO>updateRoomById(@PathVariable Long roomId, @PathVariable Long hotelId,
    @RequestBody RoomDTO roomDto){
        return ResponseEntity.ok(iRoomService.updateRoomById(hotelId,roomId,roomDto));

    }

}
