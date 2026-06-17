package com.priyanshu886291kumar.projects.AirBnb_SpringBoot.repository;

import com.priyanshu886291kumar.projects.AirBnb_SpringBoot.entity.Bookings.Booking;
import com.priyanshu886291kumar.projects.AirBnb_SpringBoot.entity.Hotel.Hotel;
import com.priyanshu886291kumar.projects.AirBnb_SpringBoot.entity.Users.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking,Long> {
    boolean existsByRoom_Id(Long roomId);

    @Modifying
    @Query("DELETE FROM Booking b WHERE b.room.id = :roomId")
    void deleteAllByRoomId(@Param("roomId") Long roomId);

    Optional<Booking> findBysessionId(String sessionId);

    List<Booking> findByHotel(Hotel hotel);

    List<Booking>findByHotelAndCreatedAtBetween(Hotel hotel, LocalDateTime startDateTime, LocalDateTime endDateTime);

     List<Booking> findByUser(User user);
}
