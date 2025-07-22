package ru.practicum.shareit.booking.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;

public class BookingDtoTest {
    @Test
    void shouldHandleNullDates() {
        BookingDto dto = new BookingDto();
        dto.setStart(null);
        dto.setEnd(null);

        assertNull(dto.getStart());
        assertNull(dto.getEnd());
    }
}
