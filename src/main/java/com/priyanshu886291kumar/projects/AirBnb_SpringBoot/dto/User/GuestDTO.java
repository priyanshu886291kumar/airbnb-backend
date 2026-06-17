package com.priyanshu886291kumar.projects.AirBnb_SpringBoot.dto.User;

import com.priyanshu886291kumar.projects.AirBnb_SpringBoot.entity.Users.User;
import com.priyanshu886291kumar.projects.AirBnb_SpringBoot.entity.enums.Gender;
import lombok.Data;

@Data
public class GuestDTO {
    private Long id;
    private User user;
    private String name;
    private Gender gender;
}
