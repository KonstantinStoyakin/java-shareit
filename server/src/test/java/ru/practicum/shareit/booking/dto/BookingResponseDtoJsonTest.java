package ru.practicum.shareit.booking.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.test.context.ContextConfiguration;
import ru.practicum.shareit.JsonTestConfig;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@JsonTest
@ContextConfiguration(classes = JsonTestConfig.class)
class BookingResponseDtoJsonTest {

    @Autowired
    private ObjectMapper objectMapper;

    private final String json = "{\"id\":1,\"start\":\"2023-01-01T12:00:00\"," +
            "\"end\":\"2023-01-02T12:00:00\",\"status\":\"APPROVED\"," +
            "\"booker\":{\"id\":2,\"name\":\"Booker\",\"email\":\"booker@example.com\"}," +
            "\"item\":{\"id\":3,\"name\":\"Item\",\"description\":\"Description\"," +
            "\"available\":true}}";

    @Test
    @SneakyThrows
    void shouldSerialize() {
        UserDto booker = new UserDto();
        booker.setId(2L);
        booker.setName("Booker");
        booker.setEmail("booker@example.com");

        ItemDto item = new ItemDto();
        item.setId(3L);
        item.setName("Item");
        item.setDescription("Description");
        item.setAvailable(true);

        BookingResponseDto dto = new BookingResponseDto();
        dto.setId(1L);
        dto.setStart(LocalDateTime.of(2023, 1, 1, 12, 0));
        dto.setEnd(LocalDateTime.of(2023, 1, 2, 12, 0));
        dto.setStatus(BookingStatus.APPROVED);
        dto.setBooker(booker);
        dto.setItem(item);

        String result = objectMapper.writeValueAsString(dto);

        assertTrue(result.contains("\"id\":1"));
        assertTrue(result.contains("\"status\":\"APPROVED\""));
        assertTrue(result.contains("\"booker\":{\"id\":2"));
        assertTrue(result.contains("\"item\":{\"id\":3"));
    }

    @Test
    @SneakyThrows
    void shouldDeserialize() {
        BookingResponseDto result = objectMapper.readValue(json, BookingResponseDto.class);

        assertEquals(1L, result.getId());
        assertEquals(BookingStatus.APPROVED, result.getStatus());
        assertEquals(2L, result.getBooker().getId());
        assertEquals(3L, result.getItem().getId());
    }

    @Test
    void shouldHandleNullFields() throws JsonProcessingException {
        BookingResponseDto dto = new BookingResponseDto();

        String result = objectMapper.writeValueAsString(dto);

        assertTrue(result.contains("\"id\":null"));
        assertTrue(result.contains("\"status\":null"));
        assertTrue(result.contains("\"booker\":null"));
    }
}