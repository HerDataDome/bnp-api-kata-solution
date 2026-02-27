package com.booking.factory;

import com.booking.dto.Booking;
import com.booking.dto.BookingDates;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

/**
 * Centralized generation of test payloads.
 * Decouples "what data is sent" from the step definitions.
 */
public final class TestDataFactory {

    private static final ObjectMapper mapper = new ObjectMapper();

    private TestDataFactory() {
        // Utility class
    }

    /**
     * Generates a fully valid booking baseline.
     */
    public static Booking validBooking() {
        // Dynamic dates: Safely within a 6-month window to avoid API "too far in future" blocks.
        // Starts 2 month from today, adds 0-99 days randomly to prevent test collisions.
        java.time.LocalDate checkin = java.time.LocalDate.now()
                .plusMonths(2)
                .plusDays(System.currentTimeMillis() % 100);
        // Dynamic Room ID: Randomize between 1 and 199 to prevent 409 test data collisions
        int randomRoomId = java.util.concurrent.ThreadLocalRandom.current().nextInt(1, 200);

        return Booking.builder()
                .roomid(randomRoomId)
                .firstname("John")
                .lastname("Doe")
                .depositpaid(true)
                .bookingdates(BookingDates.builder()
                        .checkin(checkin.toString())
                        .checkout(checkin.plusDays(4).toString())
                        .build())
                .email("john.doe@example.com")
                .phone("07911123456")
                .build();
    }

    public static Booking bookingWithFirstname(String firstname) {
        Booking booking = validBooking();
        booking.setFirstname(firstname);
        return booking;
    }

    public static Booking bookingWithLastname(String lastname) {
        Booking booking = validBooking();
        booking.setLastname(lastname);
        return booking;
    }

    public static Booking bookingWithPhone(String phone) {
        Booking booking = validBooking();
        booking.setPhone(phone);
        return booking;
    }

    public static Booking bookingWithEmail(String email) {
        Booking booking = validBooking();
        booking.setEmail(email);
        return booking;
    }

    public static Booking bookingWithDates(String checkin, String checkout) {
        Booking booking = validBooking();
        booking.setBookingdates(BookingDates.builder()
                .checkin(checkin)
                .checkout(checkout)
                .build());
        return booking;
    }

    /**
     * Data-type negative testing: Passing roomid as an empty string.
     */
    public static Map<String, Object> bookingWithRoomIdAsEmptyString() {
        Booking validBooking = validBooking();
        Map<String, Object> bookingMap = mapper.convertValue(validBooking, new TypeReference<Map<String, Object>>() {});
        bookingMap.put("roomid", "");
        return bookingMap;
    }
    /**
     * Data-type negative testing: Passing a String instead of an Integer.
     */
    public static Booking bookingWithRoomIdAsString(String invalidRoomId) {
        Booking booking = validBooking();
        booking.setRoomid(invalidRoomId);
        return booking;
    }

    /**
     * Data-type negative testing: Passing an Integer instead of a Boolean.
     */
    public static Booking bookingWithDepositPaidAsInteger(int invalidDepositPaid) {
        Booking booking = validBooking();
        booking.setDepositpaid(invalidDepositPaid);
        return booking;
    }

    /**
   * Returns an auth payload with the password key completely absent.
   * A Map is used instead of TokenRequest DTO so the password key is truly missing in the serialized JSON, not null.
   */
    public static Map<String, Object> tokenRequestWithoutPassword(String username) {
        return Map.of("username", username);
    }
    /**
     * Removes a key completely from the JSON payload (rather than sending null).
     * Returns a Map instead of a Booking POJO to bypass strict typing.
     */
    public static Map<String, Object> bookingWithoutField(String fieldName) {
        Booking validBooking = validBooking();
        // Convert POJO to Map
        Map<String, Object> bookingMap = mapper.convertValue(validBooking, new TypeReference<Map<String, Object>>() {});
        
        // Handle nested bookingdates field omission if required
        if (fieldName.equals("checkin") || fieldName.equals("checkout")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> datesMap = (Map<String, Object>) bookingMap.get("bookingdates");
            datesMap.remove(fieldName);
        } else {
            bookingMap.remove(fieldName);
        }
        
        return bookingMap;
    }
}