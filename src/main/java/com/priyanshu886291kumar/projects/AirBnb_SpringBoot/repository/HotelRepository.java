package com.priyanshu886291kumar.projects.AirBnb_SpringBoot.repository;

import com.priyanshu886291kumar.projects.AirBnb_SpringBoot.entity.Hotel.Hotel;
import com.priyanshu886291kumar.projects.AirBnb_SpringBoot.entity.Users.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HotelRepository extends JpaRepository<Hotel, Long> {
    List<Hotel> findByOwner(User user);
}