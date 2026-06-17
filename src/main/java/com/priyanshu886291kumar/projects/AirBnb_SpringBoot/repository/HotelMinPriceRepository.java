package com.priyanshu886291kumar.projects.AirBnb_SpringBoot.repository;

import com.priyanshu886291kumar.projects.AirBnb_SpringBoot.dto.Hotel.HotelPriceDTO;
import com.priyanshu886291kumar.projects.AirBnb_SpringBoot.entity.Hotel.Hotel;
import com.priyanshu886291kumar.projects.AirBnb_SpringBoot.entity.Hotel.HotelMinPrice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface HotelMinPriceRepository extends JpaRepository<HotelMinPrice,Long> {
    @Query("""
            SELECT new com.priyanshu886291kumar.projects.AirBnb_SpringBoot.dto.Hotel.HotelPriceDTO(i.hotel,AVG(i.price))
            FROM HotelMinPrice i
            WHERE i.hotel.city = :city
            AND i.date BETWEEN :startDate AND :endDate
            AND i.hotel.isActive = true
            GROUP BY i.hotel
           
            """)
    Page<HotelPriceDTO> findHotelsWithAvailableInventory(
            @Param("city") String city,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("roomsCount") Integer roomsCount,
            @Param("dateCount") Long dateCount,
            Pageable pageable
    );

    Optional<HotelMinPrice> findByHotelAndDate(Hotel hotel, LocalDate date);
}
