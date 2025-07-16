package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.user.UserService;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserService userService;

    @Mock
    private ItemService itemService;

    @InjectMocks
    private BookingServiceImpl bookingService;

    @Test
    void validateBookingDates_whenStartAfterEnd_shouldThrowValidationException() {
        Booking booking = new Booking();
        booking.setStart(LocalDateTime.now().plusDays(2));
        booking.setEnd(LocalDateTime.now().plusDays(1));

        assertThrows(ValidationException.class,
                () -> bookingService.validateBookingDates(booking));
    }

    @Test
    void validateBookingDates_whenStartEqualsEnd_shouldThrowValidationException() {
        LocalDateTime now = LocalDateTime.now();
        Booking booking = new Booking();
        booking.setStart(now);
        booking.setEnd(now);

        assertThrows(ValidationException.class,
                () -> bookingService.validateBookingDates(booking));
    }

    @Test
    void validateBookingDates_whenStartInPast_shouldThrowValidationException() {
        Booking booking = new Booking();
        booking.setStart(LocalDateTime.now().minusDays(1));
        booking.setEnd(LocalDateTime.now().plusDays(1));

        assertThrows(ValidationException.class,
                () -> bookingService.validateBookingDates(booking));
    }
}
