package com.priyanshu886291kumar.projects.AirBnb_SpringBoot.service.inventory;

import com.priyanshu886291kumar.projects.AirBnb_SpringBoot.dto.Hotel.HotelPriceDTO;
import com.priyanshu886291kumar.projects.AirBnb_SpringBoot.dto.Hotel.HotelSearchRequest;
import com.priyanshu886291kumar.projects.AirBnb_SpringBoot.dto.Inventory.InventoryDTO;
import com.priyanshu886291kumar.projects.AirBnb_SpringBoot.dto.Inventory.UpdateInventoryRequestDTO;
import com.priyanshu886291kumar.projects.AirBnb_SpringBoot.entity.Inventories.Inventory;
import com.priyanshu886291kumar.projects.AirBnb_SpringBoot.entity.Rooms.Room;
import com.priyanshu886291kumar.projects.AirBnb_SpringBoot.entity.Users.User;
import com.priyanshu886291kumar.projects.AirBnb_SpringBoot.exception.ResourceNotFoundException;
import com.priyanshu886291kumar.projects.AirBnb_SpringBoot.repository.HotelMinPriceRepository;
import com.priyanshu886291kumar.projects.AirBnb_SpringBoot.repository.InventoryRepository;
import com.priyanshu886291kumar.projects.AirBnb_SpringBoot.repository.RoomRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import static com.priyanshu886291kumar.projects.AirBnb_SpringBoot.util.Apputils.getCurrentUser;

@Service
@RequiredArgsConstructor
@Slf4j
public class CInventoryService implements IInventoryService {
    private final RoomRepository roomRepository;
    private final ModelMapper modelMapper;
    private final InventoryRepository inventoryRepository;
    private final HotelMinPriceRepository hotelMinPriceRepository;
    @Transactional
    @Override
    public void initializeRoomForAYear(Room room) {
        if (inventoryRepository.existsByRoom(room)) {
            log.info("Inventory already exists for room {}, skipping init", room.getId());
            return;
        }
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusYears(1);
        for(;!today.isAfter(endDate);today = today.plusDays(1)){
            Inventory inventory = Inventory.builder()
                    .hotel(room.getHotel())
                    .room(room)
                    .city(room.getHotel().getCity())
                    .date(today)
                    .bookedCount(0)
                    .reservedCount(0)
                    .price(room.getBasePrice())
                    .surgeFactor(BigDecimal.ONE)
                    .totalCount(room.getTotalCount())
                    .closed(false)
                    .build();
            inventoryRepository.save(inventory);
        }

    }



    @Override
    public void deleteAllInventories(Room room) {
        log.info("Deleting the inventories of room with id: {}",room.getId());
        inventoryRepository.deleteByRoom(room);
    }

    @Override
    public Page<HotelPriceDTO> searchHotels(HotelSearchRequest hotelSearchRequest) {
        log.info("Searching hotels for {} city, from {} to {} ",hotelSearchRequest.getCity(),hotelSearchRequest.getStartDate(),hotelSearchRequest.getEndDate());
        Pageable pageable = PageRequest.of(hotelSearchRequest.getPage(),hotelSearchRequest.getSize());
        long dateCount = ChronoUnit.DAYS.between(hotelSearchRequest.getStartDate(),hotelSearchRequest.getEndDate())+1;
       Page<HotelPriceDTO>hotelPage =  hotelMinPriceRepository.findHotelsWithAvailableInventory(
                hotelSearchRequest.getCity(),hotelSearchRequest.getStartDate(),hotelSearchRequest.getEndDate(),hotelSearchRequest.getRoomsCount(),dateCount,pageable
        );

        return hotelPage;
    }

    @Override
    public @Nullable List<InventoryDTO> getAllInventoryByRoom(Long roomId) {
        Room room = roomRepository.findById(roomId).orElseThrow(()->new ResourceNotFoundException("Room not found"));
        User user = getCurrentUser();
        if(!user.equals(room.getHotel().getOwner())){
            throw new RuntimeException("Hotel does not belong to this owner");
        }
        return inventoryRepository.findByRoomOrderByDate(room).stream().map((element) -> modelMapper.map(element, InventoryDTO.class)).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateInventory(Long roomId, UpdateInventoryRequestDTO updateInventoryRequestDto) {
        log.info("Updating All inventory by room for room with id: {} between date range: {} - {}", roomId,
                updateInventoryRequestDto.getStartDate(), updateInventoryRequestDto.getEndDate());

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: "+roomId));

        User user = getCurrentUser();
        if(!user.equals(room.getHotel().getOwner())) throw new RuntimeException("You are not the owner of room with id: "+roomId);

        inventoryRepository.getInventoryAndLockBeforeUpdate(roomId, updateInventoryRequestDto.getStartDate(),
                updateInventoryRequestDto.getEndDate());

        inventoryRepository.updateInventory(roomId, updateInventoryRequestDto.getStartDate(),
                updateInventoryRequestDto.getEndDate(), updateInventoryRequestDto.getClosed(),
                updateInventoryRequestDto.getSurgeFactor());
    }
}
