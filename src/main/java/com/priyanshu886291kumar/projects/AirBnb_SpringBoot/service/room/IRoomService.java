package com.priyanshu886291kumar.projects.AirBnb_SpringBoot.service.room;

import com.priyanshu886291kumar.projects.AirBnb_SpringBoot.dto.Room.RoomDTO;

import java.util.List;

public interface IRoomService {
    RoomDTO createNewRoom(Long hotelId,RoomDTO roomDTO);
    List<RoomDTO> getAllRoomsInHotel(Long hotelId);
    RoomDTO getRoomById(Long roomId);
    void deleteRoomById(Long roomId);

   RoomDTO updateRoomById(Long hotelId, Long roomId, RoomDTO roomDto);
}
