package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BookingStateTest {

    @Test
    void values_shouldReturnAllStates() {
        BookingState[] states = BookingState.values();
        assertEquals(6, states.length);
        assertEquals(BookingState.ALL, states[0]);
        assertEquals(BookingState.CURRENT, states[1]);
        assertEquals(BookingState.PAST, states[2]);
        assertEquals(BookingState.FUTURE, states[3]);
        assertEquals(BookingState.WAITING, states[4]);
        assertEquals(BookingState.REJECTED, states[5]);
    }

    @Test
    void valueOf_shouldReturnCorrectState() {
        assertEquals(BookingState.ALL, BookingState.valueOf("ALL"));
        assertEquals(BookingState.CURRENT, BookingState.valueOf("CURRENT"));
    }
}
