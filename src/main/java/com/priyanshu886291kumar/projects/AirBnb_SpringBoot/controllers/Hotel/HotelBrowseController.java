package com.priyanshu886291kumar.projects.AirBnb_SpringBoot.controllers.Hotel;

import com.priyanshu886291kumar.projects.AirBnb_SpringBoot.dto.Hotel.HotelInfoDTO;
import com.priyanshu886291kumar.projects.AirBnb_SpringBoot.dto.Hotel.HotelPriceDTO;
import com.priyanshu886291kumar.projects.AirBnb_SpringBoot.dto.Hotel.HotelSearchRequest;
import com.priyanshu886291kumar.projects.AirBnb_SpringBoot.service.hotel.IHotelService;
import com.priyanshu886291kumar.projects.AirBnb_SpringBoot.service.inventory.IInventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/hotels")
@RequiredArgsConstructor
public class HotelBrowseController {

    private final IInventoryService iInventoryService;
    private final IHotelService iHotelService;

    @GetMapping("/search")
    public ResponseEntity<Page<HotelPriceDTO>>searchHotels(@RequestBody HotelSearchRequest hotelSearchRequest){
       Page<HotelPriceDTO>page =  iInventoryService.searchHotels(hotelSearchRequest);
       return ResponseEntity.ok(page);

    }
    @GetMapping("/{hotelId}/info")
    public ResponseEntity<HotelInfoDTO>getHotelInfo(@PathVariable Long hotelId){
        return ResponseEntity.ok(iHotelService.getInfobyHotelId(hotelId));
    }
}
