package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.ShareItServer;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = ShareItServer.class)
class ItemRequestMapperTest {

    @Autowired
    private ItemRequestMapper itemRequestMapper;

    @Test
    void toDto_shouldMapEntityToDto() {
        ItemRequest request = new ItemRequest();
        request.setId(1L);
        request.setDescription("Need item");
        request.setCreated(LocalDateTime.now());

        User requester = new User();
        requester.setId(2L);
        request.setRequester(requester);

        ItemRequestDto dto = itemRequestMapper.toDto(request);

        assertNotNull(dto);
        assertEquals(request.getId(), dto.getId());
        assertEquals(request.getDescription(), dto.getDescription());
        assertEquals(request.getCreated(), dto.getCreated());
    }

    @Test
    void toDto_shouldMapItemsWhenPresent() {
        ItemRequest request = new ItemRequest();
        request.setId(1L);

        Item item = new Item();
        item.setId(10L);
        item.setName("Test Item");
        request.setItems(List.of(item));

        ItemRequestDto dto = itemRequestMapper.toDto(request);

        assertNotNull(dto.getItems());
        assertEquals(1, dto.getItems().size());
        assertEquals(item.getId(), dto.getItems().get(0).getId());
    }

    @Test
    void toDto_shouldMapEmptyItemsToEmptyList() {
        ItemRequest request = new ItemRequest();
        request.setItems(Collections.emptyList());

        ItemRequestDto dto = itemRequestMapper.toDto(request);

        assertNotNull(dto.getItems());
        assertTrue(dto.getItems().isEmpty());
    }
}
