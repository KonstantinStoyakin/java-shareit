package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.ShareItServer;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = ShareItServer.class)
class BookingMapperTest {

    @Autowired
    private BookingMapper bookingMapper;

    @Test
    void toBooking_shouldMapDtoToEntity() {
        BookingDto dto = new BookingDto();
        dto.setStart(LocalDateTime.now().plusDays(1));
        dto.setEnd(LocalDateTime.now().plusDays(2));
        dto.setItemId(1L);

        Booking booking = bookingMapper.toBooking(dto);

        assertNotNull(booking);
        assertEquals(dto.getStart(), booking.getStart());
        assertEquals(dto.getEnd(), booking.getEnd());
        assertEquals(dto.getItemId(), booking.getItem().getId());
    }

    @Test
    void toResponseDto_shouldMapEntityToDto() {
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setStart(LocalDateTime.now().plusDays(1));
        booking.setEnd(LocalDateTime.now().plusDays(2));
        booking.setStatus(BookingStatus.WAITING);

        User booker = new User();
        booker.setId(2L);
        booker.setName("Booker");
        booking.setBooker(booker);

        Item item = new Item();
        item.setId(3L);
        item.setName("Item");
        booking.setItem(item);

        BookingResponseDto dto = bookingMapper.toResponseDto(booking);

        assertNotNull(dto);
        assertEquals(booking.getId(), dto.getId());
        assertEquals(booking.getStart(), dto.getStart());
        assertEquals(booking.getStatus(), dto.getStatus());
        assertEquals(booker.getId(), dto.getBooker().getId());
        assertEquals(item.getId(), dto.getItem().getId());
    }
}
