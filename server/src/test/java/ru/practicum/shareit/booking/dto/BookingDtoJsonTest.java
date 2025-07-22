package ru.practicum.shareit.booking.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.test.context.ContextConfiguration;
import ru.practicum.shareit.JsonTestConfig;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@JsonTest
@ContextConfiguration(classes = JsonTestConfig.class)
class BookingDtoJsonTest {

    @Autowired
    private ObjectMapper objectMapper;

    private final String json = "{\"id\":1,\"start\":\"2023-01-01T12:00:00\"," +
            "\"end\":\"2023-01-02T12:00:00\",\"itemId\":10}";

    @Test
    @SneakyThrows
    void shouldSerialize() {
        BookingDto bookingDto = new BookingDto(1L,
                LocalDateTime.of(2023, 1, 1, 12, 0),
                LocalDateTime.of(2023, 1, 2, 12, 0),
                10L);

        String result = objectMapper.writeValueAsString(bookingDto);

        assertTrue(result.contains("\"id\":1"));
        assertTrue(result.contains("\"start\":\"2023-01-01T12:00:00\""));
        assertTrue(result.contains("\"itemId\":10"));
    }

    @Test
    @SneakyThrows
    void shouldDeserialize() {
        BookingDto result = objectMapper.readValue(json, BookingDto.class);

        assertEquals(1L, result.getId());
        assertEquals(LocalDateTime.of(2023, 1, 1, 12, 0), result.getStart());
        assertEquals(10L, result.getItemId());
    }

    @Test
    void shouldHandleNullFields() throws JsonProcessingException {
        BookingDto bookingDto = new BookingDto();

        String result = objectMapper.writeValueAsString(bookingDto);

        assertTrue(result.contains("\"id\":null"));
        assertTrue(result.contains("\"start\":null"));
        assertTrue(result.contains("\"itemId\":null"));
    }
}
