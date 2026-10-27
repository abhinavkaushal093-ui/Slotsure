package com.slotsure.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class BookingServiceTest {

    private BookingService bookingService;

    @BeforeEach
    void setUp() {
        bookingService = new BookingService();
    }

    @Test
    void shouldRejectInvalidResourceOrUser() {
        LocalDateTime start = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);
        LocalDateTime end = start.plusHours(1);

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> bookingService.createBooking(0, 1, start, end, "Valid Purpose")
        );

        assertEquals("A valid resource and user are required.", exception.getMessage());
    }

    @Test
    void shouldRejectEndTimeBeforeStartTime() {
        LocalDateTime start = LocalDateTime.now().plusDays(1).withHour(12).withMinute(0);
        LocalDateTime end = start.minusHours(1); 

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> bookingService.createBooking(1, 1, start, end, "Valid Purpose")
        );

        assertEquals("End time must be later than start time.", exception.getMessage());
    }

    @Test
    void shouldRejectEmptyPurpose() {
        LocalDateTime start = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);
        LocalDateTime end = start.plusHours(1);

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> bookingService.createBooking(1, 1, start, end, "")
        );

        assertEquals("Purpose must contain 1–255 characters.", exception.getMessage());
    }
} 
