package com.priyanshu886291kumar.projects.AirBnb_SpringBoot.service.room;

import com.priyanshu886291kumar.projects.AirBnb_SpringBoot.dto.Room.RoomDTO;
import com.priyanshu886291kumar.projects.AirBnb_SpringBoot.entity.Hotel.Hotel;
import com.priyanshu886291kumar.projects.AirBnb_SpringBoot.entity.Rooms.Room;
import com.priyanshu886291kumar.projects.AirBnb_SpringBoot.entity.Users.User;
import com.priyanshu886291kumar.projects.AirBnb_SpringBoot.exception.ResourceNotFoundException;
import com.priyanshu886291kumar.projects.AirBnb_SpringBoot.exception.UnauthorizedException;
import com.priyanshu886291kumar.projects.AirBnb_SpringBoot.repository.HotelRepository;
import com.priyanshu886291kumar.projects.AirBnb_SpringBoot.repository.InventoryRepository;
import com.priyanshu886291kumar.projects.AirBnb_SpringBoot.repository.RoomRepository;
import com.priyanshu886291kumar.projects.AirBnb_SpringBoot.service.bookingsAndPayments.IBookingService;
import com.priyanshu886291kumar.projects.AirBnb_SpringBoot.service.inventory.IInventoryService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static com.priyanshu886291kumar.projects.AirBnb_SpringBoot.util.Apputils.getCurrentUser;

@Service
@AllArgsConstructor
@Slf4j
public class CRoomService implements IRoomService {
    private final RoomRepository roomRepository;
    private final ModelMapper modelMapper;
    private final HotelRepository hotelRepository;
    private final InventoryRepository inventoryRepository;
    private  final IInventoryService iInventoryService;
    private final IBookingService iBookingService;
    @Override
    public RoomDTO createNewRoom(Long hotelId,RoomDTO roomDTO) {
        log.info("Creating a new Room in Hotel with HotelId: "+hotelId);
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(()->new ResourceNotFoundException("Hotel not found with HotelId: "+hotelId));

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!user.equals(hotel.getOwner())){
            throw new UnauthorizedException("This hotel is not owned by the user with id: " + hotelId);
        }

        Room room = modelMapper.map(roomDTO,Room.class);
        room.setHotel(hotel);
        hotel.getRooms().add(room);

        room = roomRepository.save(room);
        // Create Inventory as soon as room is created and hotel is active
        if(hotel.isActive()){
            iInventoryService.initializeRoomForAYear(room);
        }

        return modelMapper.map(room, RoomDTO.class);
    }

    @Override
    public List<RoomDTO> getAllRoomsInHotel(Long hotelId) {
        log.info("Getting all  Rooms in Hotel with HotelId: "+hotelId);
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(()->new ResourceNotFoundException("Hotel not found with HotelId: "+hotelId));

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!user.equals(hotel.getOwner())){
            throw new UnauthorizedException("This hotel is not owned by the user with id: "+user.getId());
        }

        return hotel.getRooms()
                .stream()
                .map((element)->modelMapper.map(element, RoomDTO.class)).collect(Collectors.toUnmodifiableList());


    }

    @Override
    public RoomDTO getRoomById(Long roomId) {
        log.info("Getting all  Rooms with RoomId: "+roomId);
        Room room = roomRepository.findById(roomId)
                .orElseThrow(()->new ResourceNotFoundException("Room not found with RoomId: "+roomId));

        return modelMapper.map(room,RoomDTO.class);
    }
    @Transactional
    @Override
    public void deleteRoomById(Long roomId) {
        log.info("Deleting the room with the room Id: "+roomId);
        Room room = roomRepository.findById(roomId)
                .orElseThrow(()->new ResourceNotFoundException("Room not found with RoomId: "+roomId));
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!user.equals(room.getHotel().getOwner())){
            throw new UnauthorizedException("This hotel is not owned by the user with id: "+roomId);
        }


        // DELETE ALL THE FUTURE INVENTORY

        iBookingService.deleteAllBookingsByRoom(roomId);

        iInventoryService.deleteAllInventories(room);


        room.getHotel().getRooms().remove(room);


        roomRepository.deleteById(roomId);

    }

    @Override
    public RoomDTO updateRoomById(Long hotelId, Long roomId, RoomDTO roomDto) {

        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found"));

        User user = getCurrentUser();
        if (!user.equals(hotel.getOwner())) {
            throw new UnauthorizedException("Not the owner");
        }

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));

        // ✅ PATCH-style updates
        if (roomDto.getBasePrice() != null) {
            room.setBasePrice(roomDto.getBasePrice());
        }

        if (roomDto.getCapacity() != null) {
            room.setCapacity(roomDto.getCapacity());
        }

        if (roomDto.getAmenities() != null) {
            room.setAmenities(roomDto.getAmenities());
        }

        if (roomDto.getPhotos() != null) {
            room.setPhotos(roomDto.getPhotos());
        }

        room = roomRepository.save(room);
        return modelMapper.map(room, RoomDTO.class);
    }

}
