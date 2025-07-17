package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
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

    private User createUser(Long id) {
        User user = new User();
        user.setId(id);
        user.setName("User " + id);
        user.setEmail("user" + id + "@example.com");
        return user;
    }

    private Item createItem(Long id, User owner, boolean available) {
        Item item = new Item();
        item.setId(id);
        item.setName("Item " + id);
        item.setDescription("Description " + id);
        item.setAvailable(available);
        item.setOwner(owner);
        return item;
    }

    private Booking createBooking(Long id, User booker, Item item, BookingStatus status) {
        Booking booking = new Booking();
        booking.setId(id);
        booking.setStart(LocalDateTime.now().plusDays(1));
        booking.setEnd(LocalDateTime.now().plusDays(2));
        booking.setBooker(booker);
        booking.setItem(item);
        booking.setStatus(status);
        return booking;
    }

    @Test
    void addBooking_shouldSaveValidBooking() {
        User owner = createUser(1L);
        User booker = createUser(2L);
        Item item = createItem(1L, owner, true);
        Booking booking = createBooking(null, booker, item, null);

        when(userService.getUser(booker.getId())).thenReturn(booker);
        when(itemService.getItem(item.getId(), booker.getId())).thenReturn(item);
        when(bookingRepository.existsByItemIdAndTimeRange(item.getId(), booking.getStart(), booking.getEnd()))
                .thenReturn(false);
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        Booking result = bookingService.addBooking(booking, booker.getId());

        assertNotNull(result);
        assertEquals(BookingStatus.WAITING, result.getStatus());
        verify(bookingRepository).save(booking);
    }

    @Test
    void addBooking_shouldThrowWhenItemNotAvailable() {
        User owner = createUser(1L);
        User booker = createUser(2L);
        Item item = createItem(1L, owner, false);
        Booking booking = createBooking(null, booker, item, null);

        when(userService.getUser(booker.getId())).thenReturn(booker);
        when(itemService.getItem(item.getId(), booker.getId())).thenReturn(item);

        assertThrows(ValidationException.class, () ->
                bookingService.addBooking(booking, booker.getId()));
    }

    @Test
    void addBooking_shouldThrowWhenBookingOwnItem() {
        User owner = createUser(1L);
        Item item = createItem(1L, owner, true);
        Booking booking = createBooking(null, owner, item, null);

        when(userService.getUser(owner.getId())).thenReturn(owner);
        when(itemService.getItem(item.getId(), owner.getId())).thenReturn(item);

        assertThrows(NotFoundException.class, () ->
                bookingService.addBooking(booking, owner.getId()));
    }

    @Test
    void addBooking_shouldThrowWhenDatesOverlap() {
        User owner = createUser(1L);
        User booker = createUser(2L);
        Item item = createItem(1L, owner, true);
        Booking booking = createBooking(null, booker, item, null);

        when(userService.getUser(booker.getId())).thenReturn(booker);
        when(itemService.getItem(item.getId(), booker.getId())).thenReturn(item);
        when(bookingRepository.existsByItemIdAndTimeRange(item.getId(), booking.getStart(), booking.getEnd()))
                .thenReturn(true);

        assertThrows(ValidationException.class, () ->
                bookingService.addBooking(booking, booker.getId()));
    }

    @Test
    void approveBooking_shouldApproveWaitingBooking() {
        User owner = createUser(1L);
        User booker = createUser(2L);
        Item item = createItem(1L, owner, true);
        Booking booking = createBooking(1L, booker, item, BookingStatus.WAITING);

        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        Booking result = bookingService.approveBooking(booking.getId(), owner.getId(), true);

        assertEquals(BookingStatus.APPROVED, result.getStatus());
    }

    @Test
    void approveBooking_shouldRejectWaitingBooking() {
        User owner = createUser(1L);
        User booker = createUser(2L);
        Item item = createItem(1L, owner, true);
        Booking booking = createBooking(1L, booker, item, BookingStatus.WAITING);

        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        Booking result = bookingService.approveBooking(booking.getId(), owner.getId(), false);

        assertEquals(BookingStatus.REJECTED, result.getStatus());
    }

    @Test
    void approveBooking_shouldThrowWhenNotOwner() {
        User owner = createUser(1L);
        User otherUser = createUser(3L);
        User booker = createUser(2L);
        Item item = createItem(1L, owner, true);
        Booking booking = createBooking(1L, booker, item, BookingStatus.WAITING);

        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));

        assertThrows(ForbiddenException.class, () ->
                bookingService.approveBooking(booking.getId(), otherUser.getId(), true));
    }

    @Test
    void approveBooking_shouldThrowWhenAlreadyApproved() {
        User owner = createUser(1L);
        User booker = createUser(2L);
        Item item = createItem(1L, owner, true);
        Booking booking = createBooking(1L, booker, item, BookingStatus.APPROVED);

        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));

        assertThrows(ValidationException.class, () ->
                bookingService.approveBooking(booking.getId(), owner.getId(), true));
    }

    @Test
    void approveBooking_shouldThrowWhenAlreadyRejected() {
        User owner = createUser(1L);
        User booker = createUser(2L);
        Item item = createItem(1L, owner, true);
        Booking booking = createBooking(1L, booker, item, BookingStatus.REJECTED);

        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));

        assertThrows(ValidationException.class, () ->
                bookingService.approveBooking(booking.getId(), owner.getId(), true));
    }

    @Test
    void getBooking_shouldReturnBookingForBooker() {
        User owner = createUser(1L);
        User booker = createUser(2L);
        Item item = createItem(1L, owner, true);
        Booking booking = createBooking(1L, booker, item, BookingStatus.APPROVED);

        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));

        Booking result = bookingService.getBooking(booking.getId(), booker.getId());

        assertNotNull(result);
        assertEquals(booking.getId(), result.getId());
    }

    @Test
    void getBooking_shouldReturnBookingForOwner() {
        User owner = createUser(1L);
        User booker = createUser(2L);
        Item item = createItem(1L, owner, true);
        Booking booking = createBooking(1L, booker, item, BookingStatus.APPROVED);

        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));

        Booking result = bookingService.getBooking(booking.getId(), owner.getId());

        assertNotNull(result);
        assertEquals(booking.getId(), result.getId());
    }

    @Test
    void getBooking_shouldThrowWhenNotBookerOrOwner() {
        User owner = createUser(1L);
        User booker = createUser(2L);
        User otherUser = createUser(3L);
        Item item = createItem(1L, owner, true);
        Booking booking = createBooking(1L, booker, item, BookingStatus.APPROVED);

        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));

        assertThrows(NotFoundException.class, () ->
                bookingService.getBooking(booking.getId(), otherUser.getId()));
    }

    @Test
    void validateBookingDates_shouldThrowWhenStartAfterEnd() {
        Booking booking = new Booking();
        booking.setStart(LocalDateTime.now().plusDays(2));
        booking.setEnd(LocalDateTime.now().plusDays(1));

        assertThrows(ValidationException.class, () ->
                bookingService.validateBookingDates(booking));
    }

    @Test
    void validateBookingDates_shouldThrowWhenStartEqualsEnd() {
        LocalDateTime now = LocalDateTime.now();
        Booking booking = new Booking();
        booking.setStart(now);
        booking.setEnd(now);

        assertThrows(ValidationException.class, () ->
                bookingService.validateBookingDates(booking));
    }

    @Test
    void validateBookingDates_shouldThrowWhenStartInPast() {
        Booking booking = new Booking();
        booking.setStart(LocalDateTime.now().minusDays(1));
        booking.setEnd(LocalDateTime.now().plusDays(1));

        assertThrows(ValidationException.class, () ->
                bookingService.validateBookingDates(booking));
    }

    @Test
    void validateBookingDates_shouldNotThrowForValidDates() {
        Booking booking = new Booking();
        booking.setStart(LocalDateTime.now().plusDays(1));
        booking.setEnd(LocalDateTime.now().plusDays(2));

        assertDoesNotThrow(() -> bookingService.validateBookingDates(booking));
    }

    @Test
    void validateBookingDates_shouldThrowWhenStartOrEndIsNull() {
        Booking booking = new Booking();
        booking.setStart(null);
        booking.setEnd(LocalDateTime.now());
        assertThrows(ValidationException.class, () ->
                bookingService.validateBookingDates(booking));

        booking.setStart(LocalDateTime.now());
        booking.setEnd(null);
        assertThrows(ValidationException.class, () ->
                bookingService.validateBookingDates(booking));
    }

    @Test
    void validateBookingDates_shouldThrowWhenEndInPast() {
        Booking booking = new Booking();
        booking.setStart(LocalDateTime.now().plusDays(1));
        booking.setEnd(LocalDateTime.now().minusDays(1));
        assertThrows(ValidationException.class, () ->
                bookingService.validateBookingDates(booking));
    }

    @Test
    void getUserBookings_shouldHandleAllStates() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        when(userService.getUser(userId)).thenReturn(user);

        PageRequest page = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "start"));
        LocalDateTime now = LocalDateTime.now();

        for (String state : List.of("ALL", "CURRENT", "PAST", "FUTURE", "WAITING", "REJECTED")) {
            switch (state) {
                case "ALL" -> when(bookingRepository.findByBookerId(userId, page)).thenReturn(List.of());
                case "CURRENT" -> when(bookingRepository.findByBookerIdAndStartBeforeAndEndAfter(eq(userId), any(),
                        any(), eq(page))).thenReturn(List.of());
                case "PAST" -> when(bookingRepository.findByBookerIdAndEndBefore(eq(userId), any(), eq(page)))
                        .thenReturn(List.of());
                case "FUTURE" -> when(bookingRepository.findByBookerIdAndStartAfter(eq(userId), any(), eq(page)))
                        .thenReturn(List.of());
                case "WAITING" -> when(bookingRepository.findByBookerIdAndStatus(userId, BookingStatus.WAITING, page))
                        .thenReturn(List.of());
                case "REJECTED" -> when(bookingRepository.findByBookerIdAndStatus(userId, BookingStatus.REJECTED, page))
                        .thenReturn(List.of());
            }

            assertDoesNotThrow(() ->
                            bookingService.getUserBookings(userId, state, 0, 10),
                    "State: " + state);
        }

        assertThrows(ValidationException.class, () ->
                bookingService.getUserBookings(userId, "UNSUPPORTED", 0, 10));
    }



    @Test
    void getOwnerBookings_shouldHandleAllStates() {
        User owner = createUser(1L);
        when(userService.getUser(owner.getId())).thenReturn(owner);

        for (String state : List.of("ALL", "CURRENT", "PAST", "FUTURE", "WAITING", "REJECTED")) {
            switch (state) {
                case "ALL" -> when(bookingRepository.findByItemOwnerId(eq(owner.getId()), any(PageRequest.class)))
                        .thenReturn(List.of());
                case "CURRENT" -> when(bookingRepository.findByItemOwnerIdAndStartBeforeAndEndAfter(
                        eq(owner.getId()), any(LocalDateTime.class), any(LocalDateTime.class), any(PageRequest.class)))
                        .thenReturn(List.of());
                case "PAST" -> when(bookingRepository.findByItemOwnerIdAndEndBefore(
                        eq(owner.getId()), any(LocalDateTime.class), any(PageRequest.class)))
                        .thenReturn(List.of());
                case "FUTURE" -> when(bookingRepository.findByItemOwnerIdAndStartAfter(
                        eq(owner.getId()), any(LocalDateTime.class), any(PageRequest.class)))
                        .thenReturn(List.of());
                case "WAITING" -> when(bookingRepository.findByItemOwnerIdAndStatus(
                        eq(owner.getId()), eq(BookingStatus.WAITING), any(PageRequest.class)))
                        .thenReturn(List.of());
                case "REJECTED" -> when(bookingRepository.findByItemOwnerIdAndStatus(
                        eq(owner.getId()), eq(BookingStatus.REJECTED), any(PageRequest.class)))
                        .thenReturn(List.of());
            }

            assertDoesNotThrow(() ->
                    bookingService.getOwnerBookings(owner.getId(), state, 0, 10));
        }

        assertThrows(ValidationException.class, () ->
                bookingService.getOwnerBookings(owner.getId(), "INVALID", 0, 10));
    }


    @Test
    void getUserBookings_shouldUseCorrectSorting() {
        User user = createUser(1L);
        when(userService.getUser(user.getId())).thenReturn(user);
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC,
                "start"));
        when(bookingRepository.findByBookerId(eq(user.getId()), eq(pageRequest)))
                .thenReturn(List.of());

        bookingService.getUserBookings(user.getId(), "ALL", 0, 10);

        verify(bookingRepository).findByBookerId(eq(user.getId()), argThat(
                page -> page.getSort().getOrderFor("start").getDirection() == Sort.Direction.DESC
        ));
    }

    @Test
    void getOwnerBookings_shouldUseCorrectSorting() {
        User owner = createUser(1L);
        when(userService.getUser(owner.getId())).thenReturn(owner);
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC,
                "start"));
        when(bookingRepository.findByItemOwnerId(eq(owner.getId()), eq(pageRequest)))
                .thenReturn(List.of());

        bookingService.getOwnerBookings(owner.getId(), "ALL", 0, 10);

        verify(bookingRepository).findByItemOwnerId(eq(owner.getId()), argThat(
                page -> page.getSort().getOrderFor("start").getDirection() == Sort.Direction.DESC
        ));
    }
}
