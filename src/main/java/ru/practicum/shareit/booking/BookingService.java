package ru.practicum.shareit.booking;

import java.util.List;

public interface BookingService {
    Booking addBooking(Booking booking, Long userId);

    Booking approveBooking(Long bookingId, Long userId, boolean approved);

    Booking getBooking(Long bookingId, Long userId);

    List<Booking> getUserBookings(Long userId, String state);

    List<Booking> getOwnerBookings(Long userId, String state);

    void validateBookingDates(Booking booking);
}
