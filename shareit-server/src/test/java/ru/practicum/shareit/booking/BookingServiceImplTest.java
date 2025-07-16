package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

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

    @Test
    void getUserBookings_shouldHandleAllStates() {
        for (BookingState state : BookingState.values()) {
            assertDoesNotThrow(() ->
                    bookingService.getUserBookings(1L, state.name(), 0, 10));
        }
    }

    @Test
    void approveBooking_shouldRejectAlreadyApproved() {
        User owner = new User();
        owner.setId(1L);

        Item item = new Item();
        item.setOwner(owner);

        Booking booking = new Booking();
        booking.setStatus(BookingStatus.APPROVED);
        booking.setItem(item);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThrows(ValidationException.class, () ->
                bookingService.approveBooking(1L, 1L, true));
    }

    @Test
    void addBooking_shouldCheckDateOverlap() {
        User booker = new User();
        booker.setId(1L);

        User owner = new User();
        owner.setId(2L);

        Item item = new Item();
        item.setId(1L);
        item.setOwner(owner);
        item.setAvailable(true);

        Booking booking = new Booking();
        booking.setStart(LocalDateTime.now().plusDays(1));
        booking.setEnd(LocalDateTime.now().plusDays(2));
        booking.setItem(item);
        booking.setBooker(booker);

        when(userService.getUser(1L)).thenReturn(booker);
        when(itemService.getItem(1L, 1L)).thenReturn(item);
        when(bookingRepository.existsByItemIdAndTimeRange(1L, booking.getStart(), booking.getEnd()))
                .thenReturn(true);

        assertThrows(ValidationException.class, () ->
                bookingService.addBooking(booking, 1L));
    }
}
