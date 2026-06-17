package com.priyanshu886291kumar.projects.AirBnb_SpringBoot.dto.Hotel;

import com.priyanshu886291kumar.projects.AirBnb_SpringBoot.dto.Room.RoomDTO;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class HotelInfoDTO {
    private HotelDTO hotel;
    private List<RoomDTO> rooms;


}
