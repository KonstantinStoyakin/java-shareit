package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemMapperTest {

    @Mock
    private CommentMapper commentMapper;

    @InjectMocks
    private ItemMapper itemMapper;

    @Test
    void toDto_shouldMapAllFields() {
        Item item = new Item();
        item.setId(1L);
        item.setName("Test Item");
        item.setDescription("Test Description");
        item.setAvailable(true);

        ItemRequest request = new ItemRequest();
        request.setId(10L);
        item.setRequest(request);

        Comment comment = new Comment();
        item.setComments(List.of(comment));

        CommentDto commentDto = new CommentDto();
        when(commentMapper.toDto(comment)).thenReturn(commentDto);

        ItemDto result = itemMapper.toDto(item);

        assertNotNull(result);
        assertEquals(item.getId(), result.getId());
        assertEquals(item.getName(), result.getName());
        assertEquals(item.getDescription(), result.getDescription());
        assertEquals(item.getAvailable(), result.getAvailable());
        assertEquals(request.getId(), result.getRequestId());
        assertEquals(1, result.getComments().size());
        assertEquals(commentDto, result.getComments().get(0));
    }

    @Test
    void toDto_shouldHandleNullRequest() {
        Item item = new Item();
        item.setId(1L);
        item.setComments(null);

        ItemDto result = itemMapper.toDto(item);

        assertNotNull(result);
        assertNull(result.getRequestId());
        assertNull(result.getComments());
    }

    @Test
    void toDto_shouldHandleEmptyComments() {
        Item item = new Item();
        item.setId(1L);
        item.setComments(Collections.emptyList());

        ItemDto result = itemMapper.toDto(item);

        assertNotNull(result);
        assertTrue(result.getComments().isEmpty());
    }

    @Test
    void toItem_shouldMapAllFields() {
        ItemDto itemDto = new ItemDto();
        itemDto.setId(1L);
        itemDto.setName("Test Item");
        itemDto.setDescription("Test Description");
        itemDto.setAvailable(true);
        itemDto.setRequestId(10L);

        Item result = itemMapper.toItem(itemDto);

        assertNotNull(result);
        assertEquals(itemDto.getId(), result.getId());
        assertEquals(itemDto.getName(), result.getName());
        assertEquals(itemDto.getDescription(), result.getDescription());
        assertEquals(itemDto.getAvailable(), result.getAvailable());
        assertNotNull(result.getRequest());
        assertEquals(itemDto.getRequestId(), result.getRequest().getId());
    }

    @Test
    void toItem_shouldHandleNullRequestId() {
        ItemDto itemDto = new ItemDto();
        itemDto.setId(1L);
        itemDto.setRequestId(null);

        Item result = itemMapper.toItem(itemDto);

        assertNotNull(result);
        assertNull(result.getRequest());
    }

    @Test
    void toCommentDto_shouldDelegateToCommentMapper() {
        Comment comment = new Comment();
        CommentDto expected = new CommentDto();
        when(commentMapper.toDto(comment)).thenReturn(expected);

        CommentDto result = itemMapper.toCommentDto(comment);

        assertSame(expected, result);
        verify(commentMapper).toDto(comment);
    }
}