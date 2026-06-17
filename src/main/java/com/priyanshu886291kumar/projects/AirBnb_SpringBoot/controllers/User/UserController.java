package com.priyanshu886291kumar.projects.AirBnb_SpringBoot.controllers.User;


import com.priyanshu886291kumar.projects.AirBnb_SpringBoot.dto.Booking.BookingDTO;
import com.priyanshu886291kumar.projects.AirBnb_SpringBoot.dto.User.GuestDTO;
import com.priyanshu886291kumar.projects.AirBnb_SpringBoot.dto.User.UserDTO;
import com.priyanshu886291kumar.projects.AirBnb_SpringBoot.dto.User.UserProfileUpdateReqDTO;
import com.priyanshu886291kumar.projects.AirBnb_SpringBoot.service.bookingsAndPayments.IBookingService;
import com.priyanshu886291kumar.projects.AirBnb_SpringBoot.service.guest.IGuestService;
import com.priyanshu886291kumar.projects.AirBnb_SpringBoot.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
    private final UserService userService;
    private final IBookingService bookingService;
    private final IGuestService guestService;

    @PatchMapping("/profile")
    public ResponseEntity<Void>updateProfile(@RequestBody UserProfileUpdateReqDTO updateReqDTO){
        userService.updateProfile(updateReqDTO);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/myBookings")
    public ResponseEntity<List<BookingDTO>>getMyBookings(){
        return ResponseEntity.ok(bookingService.getMyBookings());

    }
    @GetMapping("/profile")
    public ResponseEntity<UserDTO>getMyProfile(){
        return ResponseEntity.ok(userService.getMyProfile());
    }

    @GetMapping("/guests")
    public ResponseEntity<List<GuestDTO>> getAllGuests() {
        return ResponseEntity.ok(guestService.getAllGuests());
    }
    @PostMapping("/guests")
    public ResponseEntity<GuestDTO> addNewGuest(@RequestBody GuestDTO guestDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(guestService.addNewGuest(guestDto));
    }
    @PutMapping("guests/{guestId}")
    public ResponseEntity<Void> updateGuest(@PathVariable Long guestId, @RequestBody GuestDTO guestDto) {
        guestService.updateGuest(guestId, guestDto);
        return ResponseEntity.noContent().build();

    }
    @DeleteMapping("guests/{guestId}")
    public ResponseEntity<Void> deleteGuest(@PathVariable Long guestId) {
        guestService.deleteGuest(guestId);
        return ResponseEntity.noContent().build();
    }


}
