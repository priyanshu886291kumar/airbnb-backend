package com.priyanshu886291kumar.projects.AirBnb_SpringBoot.dto.User;

import com.priyanshu886291kumar.projects.AirBnb_SpringBoot.entity.enums.Gender;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UserDTO {
    private Long id;
    private String email;
    private String name;
    private Gender gender;
    private LocalDate dateOfBirth;
}
