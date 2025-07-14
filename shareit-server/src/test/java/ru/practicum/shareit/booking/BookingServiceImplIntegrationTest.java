package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import ru.practicum.shareit.TestConfig;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
@Import({TestConfig.class, BookingServiceImpl.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class BookingServiceImplIntegrationTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private TestEntityManager em;

    @Autowired
    private BookingRepository bookingRepository;

    private User owner;
    private User booker;
    private Item item;
    private Booking booking;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setName("Owner");
        owner.setEmail("owner@example.com");
        em.persist(owner);

        booker = new User();
        booker.setName("Booker");
        booker.setEmail("booker@example.com");
        em.persist(booker);

        item = new Item();
        item.setName("Test Item");
        item.setDescription("Test Description");
        item.setAvailable(true);
        item.setOwner(owner);
        em.persist(item);

        booking = new Booking();
        booking.setStart(LocalDateTime.now().plusDays(1));
        booking.setEnd(LocalDateTime.now().plusDays(2));
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.WAITING);
    }

    @Test
    void addBooking_shouldSaveBookingToDatabase() {
        Booking savedBooking = bookingService.addBooking(booking, booker.getId());

        assertNotNull(savedBooking.getId());
        assertEquals(booking.getStart(), savedBooking.getStart());
        assertEquals(booking.getEnd(), savedBooking.getEnd());
        assertEquals(BookingStatus.WAITING, savedBooking.getStatus());

        Booking fromDb = em.find(Booking.class, savedBooking.getId());
        assertEquals(savedBooking, fromDb);
    }

    @Test
    void approveBooking_shouldUpdateBookingStatus() {
        Booking savedBooking = bookingRepository.save(booking);

        Booking approvedBooking = bookingService.approveBooking(
                savedBooking.getId(), owner.getId(), true);

        assertEquals(BookingStatus.APPROVED, approvedBooking.getStatus());
    }

    @Test
    void getBooking_shouldReturnBookingFromDatabase() {
        Booking savedBooking = bookingRepository.save(booking);

        Booking foundBooking = bookingService.getBooking(savedBooking.getId(), booker.getId());

        assertEquals(savedBooking, foundBooking);
    }

    @Test
    void getUserBookings_shouldReturnBookingsForUser() {
        bookingRepository.save(booking);

        Booking pastBooking = new Booking();
        pastBooking.setStart(LocalDateTime.now().minusDays(2));
        pastBooking.setEnd(LocalDateTime.now().minusDays(1));
        pastBooking.setItem(item);
        pastBooking.setBooker(booker);
        pastBooking.setStatus(BookingStatus.APPROVED);
        bookingRepository.save(pastBooking);

        List<Booking> bookings = bookingService.getUserBookings(
                booker.getId(), "ALL", 0, 10);

        assertEquals(2, bookings.size());
    }

    @Test
    void getOwnerBookings_shouldReturnBookingsForOwner() {
        bookingRepository.deleteAll();

        Booking futureBooking = new Booking();
        futureBooking.setStart(LocalDateTime.now().plusDays(3));
        futureBooking.setEnd(LocalDateTime.now().plusDays(4));
        futureBooking.setItem(item);
        futureBooking.setBooker(booker);
        futureBooking.setStatus(BookingStatus.APPROVED);
        bookingRepository.save(futureBooking);

        List<Booking> bookings = bookingService.getOwnerBookings(
                owner.getId(), "FUTURE", 0, 10);

        assertEquals(1, bookings.size());
        assertEquals(futureBooking.getStart(), bookings.get(0).getStart());
    }

    @Test
    void addBooking_shouldThrowWhenItemNotAvailable() {
        item.setAvailable(false);
        em.persist(item);

        assertThrows(ValidationException.class,
                () -> bookingService.addBooking(booking, booker.getId()));
    }

    @Test
    void addBooking_shouldThrowWhenBookingOwnItem() {
        assertThrows(NotFoundException.class,
                () -> bookingService.addBooking(booking, owner.getId()));
    }

    @Test
    void approveBooking_shouldThrowWhenNotOwner() {
        Booking savedBooking = bookingRepository.save(booking);

        assertThrows(ForbiddenException.class,
                () -> bookingService.approveBooking(savedBooking.getId(), booker.getId(), true));
    }

    @Test
    void validateBookingDates_shouldThrowWhenEndBeforeStart() {
        booking.setEnd(booking.getStart().minusDays(1));

        assertThrows(ValidationException.class,
                () -> bookingService.validateBookingDates(booking));
    }
}
