package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BookingStatusTest {
    @Test
    void values_shouldReturnAllStatuses() {
        BookingStatus[] statuses = BookingStatus.values();
        assertEquals(4, statuses.length);
        assertEquals(BookingStatus.WAITING, statuses[0]);
        assertEquals(BookingStatus.APPROVED, statuses[1]);
        assertEquals(BookingStatus.REJECTED, statuses[2]);
        assertEquals(BookingStatus.CANCELLED, statuses[3]);
    }

    @Test
    void valueOf_shouldReturnCorrectStatus() {
        assertEquals(BookingStatus.WAITING, BookingStatus.valueOf("WAITING"));
        assertEquals(BookingStatus.APPROVED, BookingStatus.valueOf("APPROVED"));
    }
}
