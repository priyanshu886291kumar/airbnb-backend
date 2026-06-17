package com.priyanshu886291kumar.projects.AirBnb_SpringBoot.dto.Room;

import lombok.Data;

import java.math.BigDecimal;


@Data
public class RoomDTO {
    private Long id;
    private String type;
    private BigDecimal basePrice;
    private String[]photos;
    private String[] amenities;
    private Integer capacity;
    private Integer totalCount;
}
