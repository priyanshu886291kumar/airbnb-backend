package com.priyanshu886291kumar.projects.AirBnb_SpringBoot.service.bookingsAndPayments;

import com.priyanshu886291kumar.projects.AirBnb_SpringBoot.dto.Booking.BookingDTO;
import com.priyanshu886291kumar.projects.AirBnb_SpringBoot.dto.Booking.BookingRequestDTO;
import com.priyanshu886291kumar.projects.AirBnb_SpringBoot.dto.User.GuestDTO;
import com.priyanshu886291kumar.projects.AirBnb_SpringBoot.dto.Hotel.HotelReportDTO;
import com.priyanshu886291kumar.projects.AirBnb_SpringBoot.entity.Bookings.Booking;
import com.priyanshu886291kumar.projects.AirBnb_SpringBoot.entity.Hotel.Hotel;
import com.priyanshu886291kumar.projects.AirBnb_SpringBoot.entity.Inventories.Inventory;
import com.priyanshu886291kumar.projects.AirBnb_SpringBoot.entity.Rooms.Room;
import com.priyanshu886291kumar.projects.AirBnb_SpringBoot.entity.Users.Guest;
import com.priyanshu886291kumar.projects.AirBnb_SpringBoot.entity.Users.User;
import com.priyanshu886291kumar.projects.AirBnb_SpringBoot.entity.enums.BookingStatus;
import com.priyanshu886291kumar.projects.AirBnb_SpringBoot.exception.ResourceNotFoundException;
import com.priyanshu886291kumar.projects.AirBnb_SpringBoot.exception.UnauthorizedException;
import com.priyanshu886291kumar.projects.AirBnb_SpringBoot.repository.*;
import com.priyanshu886291kumar.projects.AirBnb_SpringBoot.strategy.PricingService;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.Refund;
import com.stripe.model.checkout.Session;
import com.stripe.param.RefundCreateParams;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import static com.priyanshu886291kumar.projects.AirBnb_SpringBoot.util.Apputils.getCurrentUser;

@Service
@RequiredArgsConstructor
@Slf4j
public class CBookingService implements IBookingService {
    private final GuestRepository guestRepository;
    private final ModelMapper modelMapper;
    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;
    private final BookingRepository bookingRepository;
    private final InventoryRepository inventoryRepository;
    private final CheckoutService checkoutService;
    private final PricingService pricingService;

    @Value("${frontend.url}")
    private String frontendUrl;
    @Override
    @Transactional
    public BookingDTO initialiseBooking(BookingRequestDTO bookingRequestDTO) {
        log.info("Initialising Booking for hotel : {}, room: {}, date: {}-{}",bookingRequestDTO.getHotelId(),bookingRequestDTO.getRoomId(),bookingRequestDTO.getCheckInDate(),bookingRequestDTO.getCheckOutDate());
        //Step1 : Get the hotel
        Hotel hotel = hotelRepository.findById(bookingRequestDTO.getHotelId()).orElseThrow(()->  new ResourceNotFoundException("Hotel not found with id: "+bookingRequestDTO.getHotelId()));
        Room room = roomRepository.findById(bookingRequestDTO.getRoomId()).orElseThrow(()-> new ResourceNotFoundException("Room not found with id: "+bookingRequestDTO.getRoomId()));
        List<Inventory> inventoryList = inventoryRepository.findAndLockAvailableInventory(
                bookingRequestDTO.getRoomId(),
                bookingRequestDTO.getCheckInDate(),
                bookingRequestDTO.getCheckOutDate(),
                bookingRequestDTO.getRoomsCount()
        );
        long daysCount = ChronoUnit.DAYS.between(bookingRequestDTO.getCheckInDate(), bookingRequestDTO.getCheckOutDate())+1;

        if (inventoryList.size() != daysCount) {
            throw new IllegalStateException("Room is not available anymore");
        }

        // Reserve the room/ update the booked count of inventories
        inventoryRepository.initBooking(room.getId(), bookingRequestDTO.getCheckInDate(),
                bookingRequestDTO.getCheckOutDate(), bookingRequestDTO.getRoomsCount());

        BigDecimal priceForOneRoom = pricingService.calculateTotalPrice(inventoryList);
        BigDecimal totalPrice = priceForOneRoom.multiply(BigDecimal.valueOf(bookingRequestDTO.getRoomsCount()));



        //Create the booking
        Booking booking = Booking.builder()
                .bookingStatus(BookingStatus.RESERVED)
                .hotel(hotel)
                .room(room)
                .checkInDate(bookingRequestDTO.getCheckInDate())
                .checkOutDate(bookingRequestDTO.getCheckOutDate())
                .user(getCurrentUser())
                .roomsCount(bookingRequestDTO.getRoomsCount())
                .amount(totalPrice)
                .build();
        booking = bookingRepository.save(booking);

        return modelMapper.map(booking, BookingDTO.class);

    }

    @Override
    public BookingDTO addGuest(List<GuestDTO> guestDTOList, Long bookingId) {
        log.info("Adding Guest  for Booking with id : {}",bookingId);
        //Step1 : Get the Booking
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(()->  new ResourceNotFoundException("Booking not found with id: "+bookingId));
        User user = getCurrentUser();
        if (!user.getId().equals(booking.getUser().getId())) {
            throw new UnauthorizedException(
                    "Booking does not belong to this user with id: " + user.getId()
            );
        }

        //Step2 : Check if the booking has Expired
        if(hasBoookingExpired(booking)){
            throw new IllegalStateException("Booking has already expired");
        }
        if(booking.getBookingStatus()!=BookingStatus.RESERVED){
            throw new IllegalStateException("Booking is not under reserved state, cannot add guests");
        }
        for(GuestDTO guestDTO:guestDTOList){
            Guest guest = modelMapper.map(guestDTO, Guest.class);
            guest.setUser(getCurrentUser());
            guest = guestRepository.save(guest);
            booking.getGuests().add(guest);
        }
        booking.setBookingStatus(BookingStatus.GUESTS_ADDED);
        booking = bookingRepository.save(booking);
        return modelMapper.map(booking,BookingDTO.class);


    }

    @Override
    @Transactional
    public void deleteAllBookingsByRoom(Long roomId) {


        if (!roomRepository.existsById(roomId)) {
            throw new ResourceNotFoundException(
                    "Room with roomId not found: " + roomId
            );
        }


        boolean hasBookings = bookingRepository.existsByRoom_Id(roomId);

        if (!hasBookings) {
            return;
        }


        bookingRepository.deleteAllByRoomId(roomId);
    }

    @Override
    @Transactional
    public String initiatePayment(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(()->new ResourceNotFoundException("Booking not found with id: "+bookingId));
        User user = getCurrentUser();
        if (!user.getId().equals(booking.getUser().getId())) {
            throw new UnauthorizedException(
                    "Booking does not belong to this user with id: " + user.getId()
            );
        }
        //Step2 : Check if the booking has Expired
        if(hasBoookingExpired(booking)){
            throw new IllegalStateException("Booking has already expired");
        }
        String sessionUrl = checkoutService.getCheckoutSession(booking,frontendUrl+"/payments/success",frontendUrl+"/payments/failure");

        booking.setBookingStatus(BookingStatus.PAYMENT_PENDING);
        bookingRepository.save(booking);

        return sessionUrl;
    }

    @Override
    @Transactional
    public void capturePayment(Event event) {
        if("checkout.session.completed".equals(event.getType())){
            Session session = (Session) event.getDataObjectDeserializer().getObject().orElse(null);
            if(session==null) return;
            String sessionId = session.getId();
            Booking booking = bookingRepository.findBysessionId(sessionId).orElseThrow(()->new ResourceNotFoundException("Booking not found"));
            booking.setBookingStatus(BookingStatus.CONFIRMED);
            bookingRepository.save(booking);
            inventoryRepository.findAndLockReservedInventory(booking.getRoom().getId(), booking.getCheckInDate(),
                    booking.getCheckOutDate(), booking.getRoomsCount());

            inventoryRepository.confirmBooking(booking.getRoom().getId(), booking.getCheckInDate(),
                    booking.getCheckOutDate(), booking.getRoomsCount());

            log.info("Successfully confirmed the booking for Booking ID: {}", booking.getId());

        }else{
            log.warn("Unhandled event type: {}", event.getType());

        }

    }

    @Override
    @Transactional
    public void cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(()->new ResourceNotFoundException("Booking not found with id: "+bookingId));
        User user = getCurrentUser();
        if (!user.getId().equals(booking.getUser().getId())) {
            throw new UnauthorizedException(
                    "Booking does not belong to this user with id: " + user.getId()
            );
        }
        if(booking.getBookingStatus()!=BookingStatus.CONFIRMED){
            throw new IllegalStateException("Only Confirmed Booking can be cancelled");
        }
        booking.setBookingStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
        inventoryRepository.findAndLockReservedInventory(booking.getRoom().getId(), booking.getCheckInDate(),
                booking.getCheckOutDate(), booking.getRoomsCount());
        inventoryRepository.cancelBooking(booking.getRoom().getId(), booking.getCheckInDate(),
                booking.getCheckOutDate(), booking.getRoomsCount());
        //handle the refund
        try {
            Session session = Session.retrieve(booking.getSessionId());
            RefundCreateParams refundParams = RefundCreateParams.builder()
                    .setPaymentIntent(session.getPaymentIntent())
                    .build();

            Refund.create(refundParams);
        } catch (StripeException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public List<BookingDTO> getAllBookingsByHotelId(Long hotelId)  {
        Hotel hotel = hotelRepository.findById(hotelId).orElseThrow(()->new ResourceNotFoundException("Hotel not found with id: "+hotelId));


        User user = getCurrentUser();

        if(!user.equals(hotel.getOwner())){
            throw  new RuntimeException("Owner not belong to this hotel");
        }
        List<Booking> bookingList = bookingRepository.findByHotel(hotel);

        return bookingList.stream().map((element) -> modelMapper.map(element, BookingDTO.class)).collect(Collectors.toList());
    }

    @Override
    public HotelReportDTO getReportByHotelId(Long hotelId, LocalDate startDate, LocalDate endDate) {
        Hotel hotel = hotelRepository.findById(hotelId).orElseThrow(()->new ResourceNotFoundException("Hotel not found with id: "+hotelId));


        User user = getCurrentUser();

        if(!user.equals(hotel.getOwner())){
            throw  new RuntimeException("Owner not belong to this hotel");
        }
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
        List<Booking>bookings = bookingRepository.findByHotelAndCreatedAtBetween(hotel,startDateTime,endDateTime);
        Long totalConfirmedBookings = bookings.stream()
                .filter(booking -> booking.getBookingStatus()==BookingStatus.CONFIRMED)
                .count();
        BigDecimal totalRevenueofConfirmedBookings = bookings.stream()
                .filter(booking -> booking.getBookingStatus()==BookingStatus.CONFIRMED)
                .map(Booking::getAmount)
                .reduce(BigDecimal.ZERO,BigDecimal::add);

        BigDecimal avgRevenue = totalRevenueofConfirmedBookings.divide(BigDecimal.valueOf(totalConfirmedBookings), RoundingMode.HALF_UP);
        return new HotelReportDTO(totalConfirmedBookings,totalRevenueofConfirmedBookings,avgRevenue);
    }

    @Override
    public  List<BookingDTO> getMyBookings() {
        User user = getCurrentUser();
        List<Booking>bookings = bookingRepository.findByUser(user);
        return bookings.stream().map((element) -> modelMapper.map(element, BookingDTO.class)).collect(Collectors.toList());




    }

    public Boolean hasBoookingExpired(Booking booking){
      if(booking.getCreatedAt().plusMinutes(10).isBefore(LocalDateTime.now())){
          booking.setBookingStatus(BookingStatus.EXPIRED);
          return true;
      }
      return false;
    }


}
