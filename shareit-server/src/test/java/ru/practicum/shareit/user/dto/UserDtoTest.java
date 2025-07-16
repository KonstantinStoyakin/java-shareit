package ru.practicum.shareit.user.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class UserDtoTest {
    @Test
    void testEqualsAndHashCode() {
        UserDto dto1 = new UserDto(1L, "Name", "email@example.com");
        UserDto dto2 = new UserDto(1L, "Name", "email@example.com");
        UserDto dto3 = new UserDto(2L, "Other", "other@example.com");

        assertEquals(dto1, dto2);
        assertNotEquals(dto1, dto3);
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    void testNoArgsConstructor() {
        UserDto dto = new UserDto();
        assertNull(dto.getId());
        assertNull(dto.getName());
        assertNull(dto.getEmail());
    }
}
