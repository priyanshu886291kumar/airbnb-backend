package com.priyanshu886291kumar.projects.AirBnb_SpringBoot.dto.Hotel;

import com.priyanshu886291kumar.projects.AirBnb_SpringBoot.entity.Hotel.HotelContactInfo;
import lombok.Data;

@Data
public class HotelDTO {
    private Long id;
    private String name;
    private String city;
    private String[]photos;
    private String[] amenities;
    private HotelContactInfo hotelContactInfo;
    private Boolean isActive;
}
