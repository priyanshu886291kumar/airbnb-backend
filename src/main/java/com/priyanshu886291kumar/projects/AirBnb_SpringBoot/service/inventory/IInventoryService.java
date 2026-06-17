package com.priyanshu886291kumar.projects.AirBnb_SpringBoot.service.inventory;

import com.priyanshu886291kumar.projects.AirBnb_SpringBoot.dto.Hotel.HotelPriceDTO;
import com.priyanshu886291kumar.projects.AirBnb_SpringBoot.dto.Hotel.HotelSearchRequest;
import com.priyanshu886291kumar.projects.AirBnb_SpringBoot.dto.Inventory.InventoryDTO;
import com.priyanshu886291kumar.projects.AirBnb_SpringBoot.dto.Inventory.UpdateInventoryRequestDTO;
import com.priyanshu886291kumar.projects.AirBnb_SpringBoot.entity.Rooms.Room;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;

import java.util.List;

public interface IInventoryService {
    void initializeRoomForAYear(Room room);

    void deleteAllInventories(Room room);


    Page<HotelPriceDTO> searchHotels(HotelSearchRequest hotelSearchRequest);

    @Nullable List<InventoryDTO> getAllInventoryByRoom(Long roomId);

    void updateInventory(Long roomId, UpdateInventoryRequestDTO inventoryRequestDTO);
}
