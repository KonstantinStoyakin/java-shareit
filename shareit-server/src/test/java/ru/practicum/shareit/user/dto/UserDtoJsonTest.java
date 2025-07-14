package ru.practicum.shareit.user.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.context.annotation.Import;
import ru.practicum.shareit.JsonTestConfig;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@JsonTest
@Import(JsonTestConfig.class)
class UserDtoJsonTest {

    @Autowired
    private ObjectMapper objectMapper;

    private final String json = "{\"id\":1,\"name\":\"Test User\",\"email\":\"test@example.com\"}";

    @Test
    @SneakyThrows
    void shouldSerialize() {
        UserDto userDto = new UserDto(1L, "Test User", "test@example.com");

        String result = objectMapper.writeValueAsString(userDto);

        assertTrue(result.contains("\"id\":1"));
        assertTrue(result.contains("\"name\":\"Test User\""));
        assertTrue(result.contains("\"email\":\"test@example.com\""));
    }

    @Test
    @SneakyThrows
    void shouldDeserialize() {
        UserDto result = objectMapper.readValue(json, UserDto.class);

        assertEquals(1L, result.getId());
        assertEquals("Test User", result.getName());
        assertEquals("test@example.com", result.getEmail());
    }

    @Test
    void shouldHandleNullFields() throws JsonProcessingException {
        UserDto userDto = new UserDto(null, null, null);

        String result = objectMapper.writeValueAsString(userDto);

        assertTrue(result.contains("\"id\":null"));
        assertTrue(result.contains("\"name\":null"));
        assertTrue(result.contains("\"email\":null"));
    }
}
