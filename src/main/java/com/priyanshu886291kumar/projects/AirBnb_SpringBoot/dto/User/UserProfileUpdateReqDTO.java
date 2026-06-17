package com.priyanshu886291kumar.projects.AirBnb_SpringBoot.dto.User;

import com.priyanshu886291kumar.projects.AirBnb_SpringBoot.entity.enums.Gender;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UserProfileUpdateReqDTO {
    private String name;
    private LocalDate dateOfBirth;
    private Gender gender;
}
