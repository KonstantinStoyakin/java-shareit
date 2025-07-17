package ru.practicum.shareit.request.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.context.annotation.Import;
import ru.practicum.shareit.JsonTestConfig;
import ru.practicum.shareit.item.dto.ItemDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@JsonTest
@Import(JsonTestConfig.class)
class ItemRequestDtoJsonTest {

    @Autowired
    private ObjectMapper objectMapper;

    private final String json = "{\"id\":1,\"description\":\"Test Description\"," +
            "\"created\":\"2023-01-01T12:00:00\",\"items\":[{\"id\":2,\"name\":\"Item\"," +
            "\"description\":\"Item Description\",\"available\":true}]}";

    @Test
    @SneakyThrows
    void shouldSerialize() {
        ItemDto itemDto = new ItemDto(2L, "Item", "Item Description", true, null, null, null, null);
        ItemRequestDto dto = new ItemRequestDto(1L, "Test Description",
                LocalDateTime.of(2023, 1, 1, 12, 0), List.of(itemDto));

        String result = objectMapper.writeValueAsString(dto);

        assertTrue(result.contains("\"id\":1"));
        assertTrue(result.contains("\"description\":\"Test Description\""));
        assertTrue(result.contains("\"items\":[{\"id\":2"));
    }

    @Test
    @SneakyThrows
    void shouldDeserialize() {
        ItemRequestDto result = objectMapper.readValue(json, ItemRequestDto.class);

        assertEquals(1L, result.getId());
        assertEquals("Test Description", result.getDescription());
        assertEquals(2L, result.getItems().get(0).getId());
    }

    @Test
    void shouldHandleNullFields() throws JsonProcessingException {
        ItemRequestDto dto = new ItemRequestDto();

        String result = objectMapper.writeValueAsString(dto);

        assertTrue(result.contains("\"id\":null"));
        assertTrue(result.contains("\"description\":null"));
        assertTrue(result.contains("\"items\":null"));
    }
}
