package com.priyanshu886291kumar.projects.AirBnb_SpringBoot.repository;

import com.priyanshu886291kumar.projects.AirBnb_SpringBoot.entity.Rooms.Room;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRepository extends JpaRepository<Room, Long> {
}