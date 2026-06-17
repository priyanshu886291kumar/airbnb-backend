package com.priyanshu886291kumar.projects.AirBnb_SpringBoot.controllers.Hotel;


import com.priyanshu886291kumar.projects.AirBnb_SpringBoot.dto.Booking.BookingDTO;
import com.priyanshu886291kumar.projects.AirBnb_SpringBoot.dto.Booking.BookingRequestDTO;
import com.priyanshu886291kumar.projects.AirBnb_SpringBoot.dto.User.GuestDTO;
import com.priyanshu886291kumar.projects.AirBnb_SpringBoot.service.bookingsAndPayments.IBookingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/bookings")
public class HotelBookingController {
    private final IBookingService bookingService;

    public HotelBookingController(IBookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping("/init")
    public ResponseEntity<BookingDTO>initialiseBooking(@RequestBody BookingRequestDTO bookingRequestDTO){
        return ResponseEntity.ok(bookingService.initialiseBooking(bookingRequestDTO));
    }
    @PostMapping("/{bookingId}/addGuests")
    public ResponseEntity<BookingDTO>addGuests(@RequestBody List<GuestDTO> guestDTOList, @PathVariable Long bookingId){
        return ResponseEntity.ok(bookingService.addGuest(guestDTOList,bookingId));

    }
    @PostMapping("/{bookingId}/payments")
    public ResponseEntity<Map<String,String>>initiatePayment(@PathVariable Long bookingId){
        String sessionUrl = bookingService.initiatePayment(bookingId);
        return ResponseEntity.ok(Map.of("sessionUrl",sessionUrl));

    }
    @PostMapping("/{bookingId}/cancel")
    public ResponseEntity<Void>cancelBooking(@PathVariable Long bookingId){
      bookingService.cancelBooking(bookingId);
        return ResponseEntity.noContent().build();

    }
}
