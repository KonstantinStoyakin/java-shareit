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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@JsonTest
@Import(JsonTestConfig.class)
class CommentDtoJsonTest {

    @Autowired
    private ObjectMapper objectMapper;

    private final String json = "{\"id\":1,\"text\":\"Test Comment\",\"authorName\":\"Author\"," +
            "\"created\":\"2023-01-01T12:00:00\"}";

    @Test
    @SneakyThrows
    void shouldSerialize() {
        CommentDto commentDto = new CommentDto(1L, "Test Comment", "Author",
                LocalDateTime.of(2023, 1, 1, 12, 0));

        String result = objectMapper.writeValueAsString(commentDto);

        assertTrue(result.contains("\"id\":1"));
        assertTrue(result.contains("\"text\":\"Test Comment\""));
        assertTrue(result.contains("\"authorName\":\"Author\""));
        assertTrue(result.contains("\"created\":\"2023-01-01T12:00:00\""));
    }

    @Test
    @SneakyThrows
    void shouldDeserialize() {
        CommentDto result = objectMapper.readValue(json, CommentDto.class);

        assertEquals(1L, result.getId());
        assertEquals("Test Comment", result.getText());
        assertEquals("Author", result.getAuthorName());
        assertEquals(LocalDateTime.of(2023, 1, 1, 12, 0), result.getCreated());
    }

    @Test
    void shouldHandleNullFields() throws JsonProcessingException {
        CommentDto commentDto = new CommentDto();

        String result = objectMapper.writeValueAsString(commentDto);

        assertTrue(result.contains("\"id\":null"));
        assertTrue(result.contains("\"text\":null"));
        assertTrue(result.contains("\"created\":null"));
    }
}
