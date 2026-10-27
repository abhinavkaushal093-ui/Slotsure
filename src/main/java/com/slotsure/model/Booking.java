package com.slotsure.model;

import java.time.LocalDateTime;

public record Booking(
    long id,
    long resourceId,
    long bookedBy,
    LocalDateTime startAt,
    LocalDateTime endAt,
    String purpose,
    String status
) {
}
