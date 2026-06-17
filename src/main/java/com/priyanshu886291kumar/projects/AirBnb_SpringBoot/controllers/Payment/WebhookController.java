package com.priyanshu886291kumar.projects.AirBnb_SpringBoot.controllers.Payment;

import com.priyanshu886291kumar.projects.AirBnb_SpringBoot.service.bookingsAndPayments.IBookingService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/webhook")
@RequiredArgsConstructor
public class WebhookController {
    private final IBookingService paymentService;
    @Value("${stripe.webhook.secret}")
    private String endPointSecret;
    @PostMapping("/payment")
    public ResponseEntity<Void>capturePayment(@RequestBody String payload, @RequestHeader("Stripe-Signature")String sigHeader){
        try {
            Event event = Webhook.constructEvent(payload,sigHeader,endPointSecret);
            paymentService.capturePayment(event);
            return ResponseEntity.noContent().build();
        }catch (SignatureVerificationException e){
            throw new RuntimeException(e);
        }
    }
}
