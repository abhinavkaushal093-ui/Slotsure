package com.slotsure.service;

import com.slotsure.config.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class BookingService {

    public long createBooking(
            long resourceId,
            long userId,
            LocalDateTime startAt,
            LocalDateTime endAt,
            String purpose) throws SQLException {

        validateInput(resourceId, userId, startAt, endAt, purpose);

        try (Connection connection = Database.getConnection()) {
            connection.setAutoCommit(false);

            try {
                lockActiveResource(connection, resourceId);

                if (hasConflict(
                        connection, resourceId, startAt, endAt)) {
                    throw new BookingConflictException(
                        "This resource is already booked for that time."
                    );
                }

                long bookingId = insertBooking(
                    connection,
                    resourceId,
                    userId,
                    startAt,
                    endAt,
                    purpose
                );

                connection.commit();
                return bookingId;

            } catch (SQLException | RuntimeException exception) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackException) {
                    exception.addSuppressed(rollbackException);
                }

                throw exception;
            }
        }
    }

    private void validateInput(
            long resourceId,
            long userId,
            LocalDateTime startAt,
            LocalDateTime endAt,
            String purpose) {

        if (resourceId <= 0 || userId <= 0) {
            throw new IllegalArgumentException(
                "A valid resource and user are required."
            );
        }

        if (startAt == null || endAt == null
                || !endAt.isAfter(startAt)) {
            throw new IllegalArgumentException(
                "End time must be later than start time."
            );
        }

        if (purpose == null || purpose.isBlank()
                || purpose.length() > 255) {
            throw new IllegalArgumentException(
                "Purpose must contain 1–255 characters."
            );
        }
    }

    private void lockActiveResource(
            Connection connection,
            long resourceId) throws SQLException {

        String sql = """
            SELECT id
            FROM resources
            WHERE id = ? AND is_active = TRUE
            FOR UPDATE
            """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(1, resourceId);

            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    throw new IllegalArgumentException(
                        "Resource does not exist or is inactive."
                    );
                }
            }
        }
    }

    private boolean hasConflict(
            Connection connection,
            long resourceId,
            LocalDateTime startAt,
            LocalDateTime endAt) throws SQLException {

        String sql = """
            SELECT 1
            FROM bookings
            WHERE resource_id = ?
              AND status = 'CONFIRMED'
              AND start_at < ?
              AND end_at > ?
            LIMIT 1
            """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(1, resourceId);
            statement.setTimestamp(2, Timestamp.valueOf(endAt));
            statement.setTimestamp(3, Timestamp.valueOf(startAt));

            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }

    private long insertBooking(
            Connection connection,
            long resourceId,
            long userId,
            LocalDateTime startAt,
            LocalDateTime endAt,
            String purpose) throws SQLException {

        String sql = """
            INSERT INTO bookings
                (resource_id, booked_by, start_at, end_at, purpose)
            VALUES (?, ?, ?, ?, ?)
            """;

        try (PreparedStatement statement = connection.prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setLong(1, resourceId);
            statement.setLong(2, userId);
            statement.setTimestamp(3, Timestamp.valueOf(startAt));
            statement.setTimestamp(4, Timestamp.valueOf(endAt));
            statement.setString(5, purpose);

            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException(
                        "Could not retrieve the booking ID."
                    );
                }

                return keys.getLong(1);
            }
        }
    }
}
