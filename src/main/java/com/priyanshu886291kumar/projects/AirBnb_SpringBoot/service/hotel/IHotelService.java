package com.priyanshu886291kumar.projects.AirBnb_SpringBoot.service.hotel;

import com.priyanshu886291kumar.projects.AirBnb_SpringBoot.dto.Hotel.HotelDTO;
import com.priyanshu886291kumar.projects.AirBnb_SpringBoot.dto.Hotel.HotelInfoDTO;

import java.util.List;

public interface IHotelService {
    HotelDTO createNewHotel(HotelDTO hotel);
    HotelDTO getHotelById(Long id);
    HotelDTO updateHotelById(Long id,HotelDTO hotelDTO);
    void deleteHotelById(Long id);
    void activateHotel(Long hotelId);

    HotelInfoDTO getInfobyHotelId(Long hotelId);

    List<HotelDTO> getAllHotel();
}
