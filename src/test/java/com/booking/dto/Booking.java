package com.booking.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Booking {
    private Object roomid;
    private String firstname;
    private String lastname;
    private Object depositpaid;
    private BookingDates bookingdates;
    private String email;
    private String phone;
}