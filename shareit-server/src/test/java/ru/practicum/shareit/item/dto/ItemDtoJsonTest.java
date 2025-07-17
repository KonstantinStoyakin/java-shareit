package ru.practicum.shareit.item.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.context.annotation.Import;
import ru.practicum.shareit.JsonTestConfig;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@JsonTest
@Import(JsonTestConfig.class)
class ItemDtoJsonTest {

    @Autowired
    private ObjectMapper objectMapper;

    private final String json = "{\"id\":1,\"name\":\"Test Item\",\"description\":\"Test Description\"," +
            "\"available\":true,\"lastBooking\":{\"id\":1,\"bookerId\":2}," +
            "\"nextBooking\":{\"id\":3,\"bookerId\":4},\"comments\":[{\"id\":5,\"text\":\"Comment\"," +
            "\"authorName\":\"Author\",\"created\":\"2023-01-01T12:00:00\"}],\"requestId\":10}";

    @Test
    @SneakyThrows
    void shouldSerialize() {
        ItemDto.BookingShort lastBooking = new ItemDto.BookingShort(1L, 2L);
        ItemDto.BookingShort nextBooking = new ItemDto.BookingShort(3L, 4L);
        CommentDto comment = new CommentDto(5L, "Comment", "Author", LocalDateTime.of(2023, 1,
                1, 12, 0));
        ItemDto itemDto = new ItemDto(1L, "Test Item", "Test Description", true,
                lastBooking, nextBooking, List.of(comment), 10L);

        String result = objectMapper.writeValueAsString(itemDto);

        assertTrue(result.contains("\"id\":1"));
        assertTrue(result.contains("\"name\":\"Test Item\""));
        assertTrue(result.contains("\"lastBooking\":{\"id\":1,\"bookerId\":2}"));
        assertTrue(result.contains("\"comments\":[{\"id\":5,\"text\":\"Comment\""));
    }

    @Test
    @SneakyThrows
    void shouldDeserialize() {
        ItemDto result = objectMapper.readValue(json, ItemDto.class);

        assertEquals(1L, result.getId());
        assertEquals("Test Item", result.getName());
        assertEquals(1L, result.getLastBooking().getId());
        assertEquals(5L, result.getComments().get(0).getId());
        assertEquals(10L, result.getRequestId());
    }

    @Test
    void shouldHandleNullFields() throws JsonProcessingException {
        ItemDto itemDto = new ItemDto();

        String result = objectMapper.writeValueAsString(itemDto);

        assertTrue(result.contains("\"id\":null"));
        assertTrue(result.contains("\"name\":null"));
        assertTrue(result.contains("\"lastBooking\":null"));
    }
}
