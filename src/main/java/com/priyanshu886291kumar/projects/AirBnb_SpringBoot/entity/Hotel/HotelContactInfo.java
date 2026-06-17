package com.priyanshu886291kumar.projects.AirBnb_SpringBoot.entity.Hotel;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Embeddable
public class HotelContactInfo {
    private String address;

    private String phoneNumber;

    private String email;
    private String location;
}
