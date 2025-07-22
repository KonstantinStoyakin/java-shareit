package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@DataJpaTest
class BookingRepositoryTest {

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
        booking.setStatus(BookingStatus.APPROVED);
        em.persist(booking);
    }

    @Test
    void findLastBooking_shouldReturnLastBooking() {
        List<Booking> result = bookingRepository.findLastBooking(
                item.getId(), LocalDateTime.now().plusDays(3));

        assertFalse(result.isEmpty());
        assertEquals(booking.getId(), result.get(0).getId());
    }

    @Test
    void findNextBooking_shouldReturnNextBooking() {
        List<Booking> result = bookingRepository.findNextBooking(
                item.getId(), LocalDateTime.now());

        assertFalse(result.isEmpty());
        assertEquals(booking.getId(), result.get(0).getId());
    }

    @Test
    void findByBookerId_shouldReturnBookings() {
        List<Booking> result = bookingRepository.findByBookerId(
                booker.getId(), PageRequest.of(0, 10));

        assertFalse(result.isEmpty());
        assertEquals(booking.getId(), result.get(0).getId());
    }

    @Test
    void existsByItemIdAndTimeRange_shouldReturnFalseWhenNoOverlap() {
        boolean exists = bookingRepository.existsByItemIdAndTimeRange(
                item.getId(),
                LocalDateTime.now().plusDays(3),
                LocalDateTime.now().plusDays(4));

        assertFalse(exists);
    }
}