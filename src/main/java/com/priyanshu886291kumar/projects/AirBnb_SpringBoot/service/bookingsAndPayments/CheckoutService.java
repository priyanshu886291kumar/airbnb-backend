package com.priyanshu886291kumar.projects.AirBnb_SpringBoot.service.bookingsAndPayments;

import com.priyanshu886291kumar.projects.AirBnb_SpringBoot.entity.Bookings.Booking;

public interface CheckoutService {
    String getCheckoutSession(Booking booking, String successUrl, String failureUrl);
}
