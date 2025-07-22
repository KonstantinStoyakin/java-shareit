package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserService userService;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private ItemRequestRepository itemRequestRepository;

    @InjectMocks
    private ItemServiceImpl itemService;

    private Item createTestItem(Long id, String name, String description, Boolean available, User owner,
                                ItemRequest request) {
        Item item = new Item();
        item.setId(id);
        item.setName(name);
        item.setDescription(description);
        item.setAvailable(available);
        item.setOwner(owner);
        item.setRequest(request);
        return item;
    }

    private User createTestUser(Long id, String name, String email) {
        User user = new User();
        user.setId(id);
        user.setName(name);
        user.setEmail(email);
        return user;
    }

    private ItemRequest createTestRequest(Long id, String description, User requester, LocalDateTime created) {
        ItemRequest request = new ItemRequest();
        request.setId(id);
        request.setDescription(description);
        request.setRequester(requester);
        request.setCreated(created);
        return request;
    }

    @Test
    void addItem_shouldSaveItemWithOwner() {
        User owner = createTestUser(1L, "Owner", "owner@example.com");
        Item item = createTestItem(null, "Item", "Description", true, owner, null);
        Item savedItem = createTestItem(1L, "Item", "Description", true, owner,
                null);

        when(userService.getUser(1L)).thenReturn(owner);
        when(itemRepository.save(item)).thenReturn(savedItem);

        Item result = itemService.addItem(item, 1L);

        assertEquals(savedItem, result);
        verify(itemRepository).save(item);
    }

    @Test
    void addItem_shouldThrowWhenValidationFails() {
        User owner = createTestUser(1L, "Owner", "owner@example.com");
        Item invalidItem = createTestItem(null, "", "", null, owner, null);

        when(userService.getUser(1L)).thenReturn(owner);

        assertThrows(ValidationException.class, () -> itemService.addItem(invalidItem, 1L));
    }

    @Test
    void addItem_shouldSetRequestWhenRequestIdExists() {
        User owner = createTestUser(1L, "Owner", "owner@example.com");
        ItemRequest request = createTestRequest(1L, "Need item", owner, LocalDateTime.now());
        Item item = createTestItem(null, "Item", "Description", true, owner, request);
        Item savedItem = createTestItem(1L, "Item", "Description", true, owner, request);

        when(userService.getUser(1L)).thenReturn(owner);
        when(itemRequestRepository.findById(1L)).thenReturn(Optional.of(request));
        when(itemRepository.save(item)).thenReturn(savedItem);

        Item result = itemService.addItem(item, 1L);

        assertEquals(savedItem, result);
        verify(itemRequestRepository).findById(1L);
    }

    @Test
    void addItem_shouldThrowWhenRequestNotFound() {
        User owner = createTestUser(1L, "Owner", "owner@example.com");
        ItemRequest request = createTestRequest(1L, "Need item", owner, LocalDateTime.now());
        Item item = createTestItem(null, "Item", "Description", true, owner, request);

        when(userService.getUser(1L)).thenReturn(owner);
        when(itemRequestRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.addItem(item, 1L));
    }

    @Test
    void updateItem_shouldUpdateNameOnly() {
        User owner = createTestUser(1L, "Owner", "owner@example.com");
        Item existingItem = createTestItem(1L, "Old", "Old Desc", true, owner,
                null);
        Item updates = new Item();
        updates.setId(1L);
        updates.setName("New");

        when(itemRepository.findById(1L)).thenReturn(Optional.of(existingItem));
        when(itemRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Item result = itemService.updateItem(updates, 1L);

        assertEquals("New", result.getName());
        assertEquals("Old Desc", result.getDescription());
        assertTrue(result.getAvailable());
    }

    @Test
    void updateItem_shouldThrowWhenItemNotFound() {
        Item item = new Item();
        item.setId(1L);

        when(itemRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.updateItem(item, 1L));
    }

    @Test
    void updateItem_shouldThrowWhenNotOwner() {
        User owner = createTestUser(1L, "Owner", "owner@example.com");
        User otherUser = createTestUser(2L, "Other", "other@example.com");
        Item existingItem = createTestItem(1L, "Item", "Desc", true, owner, null);
        Item updates = new Item();
        updates.setId(1L);

        when(itemRepository.findById(1L)).thenReturn(Optional.of(existingItem));

        assertThrows(NotFoundException.class, () -> itemService.updateItem(updates, 2L));
    }

    @Test
    void getItem_shouldReturnItemWithBookingsForOwner() {
        User owner = createTestUser(1L, "Owner", "owner@example.com");
        Item item = createTestItem(1L, "Item", "Desc", true, owner, null);

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(bookingRepository.findLastBooking(eq(1L), any())).thenReturn(Collections.emptyList());
        when(bookingRepository.findNextBooking(eq(1L), any())).thenReturn(Collections.emptyList());
        when(commentRepository.findByItemId(1L)).thenReturn(Collections.emptyList());

        Item result = itemService.getItem(1L, 1L);

        assertEquals(item, result);
        verify(bookingRepository).findLastBooking(eq(1L), any());
        verify(bookingRepository).findNextBooking(eq(1L), any());
    }

    @Test
    void getItem_shouldReturnItemWithoutBookingsForNonOwner() {
        User owner = createTestUser(1L, "Owner", "owner@example.com");
        User otherUser = createTestUser(2L, "Other", "other@example.com");
        Item item = createTestItem(1L, "Item", "Desc", true, owner, null);

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(commentRepository.findByItemId(1L)).thenReturn(Collections.emptyList());

        Item result = itemService.getItem(1L, 2L);

        assertEquals(item, result);
        verify(bookingRepository, never()).findLastBooking(anyLong(), any());
        verify(bookingRepository, never()).findNextBooking(anyLong(), any());
    }

    @Test
    void getUserItems_shouldReturnPagedItemsWithBookingsAndComments() {
        User owner = createTestUser(1L, "Owner", "owner@example.com");
        Item item1 = createTestItem(1L, "Item1", "Desc1", true, owner, null);
        Item item2 = createTestItem(2L, "Item2", "Desc2", true, owner, null);
        PageRequest page = PageRequest.of(0, 10);

        when(itemRepository.findByOwnerId(1L, page)).thenReturn(List.of(item1, item2));
        when(bookingRepository.findLastBooking(anyLong(), any())).thenReturn(Collections.emptyList());
        when(bookingRepository.findNextBooking(anyLong(), any())).thenReturn(Collections.emptyList());
        when(commentRepository.findByItemId(anyLong())).thenReturn(Collections.emptyList());

        List<Item> result = itemService.getUserItems(1L, 0, 10);

        assertEquals(2, result.size());
        verify(bookingRepository, times(2)).findLastBooking(anyLong(), any());
        verify(commentRepository, times(2)).findByItemId(anyLong());
    }

    @Test
    void searchItems_shouldReturnEmptyListForBlankText() {
        List<Item> result = itemService.searchItems("   ");
        assertTrue(result.isEmpty());
    }

    @Test
    void searchItems_shouldReturnAvailableItems() {
        when(itemRepository.searchAvailableItems("query")).thenReturn(List.of(new Item()));

        List<Item> result = itemService.searchItems("query");
        assertEquals(1, result.size());
    }

    @Test
    void addComment_shouldSaveComment() {
        User author = createTestUser(1L, "Author", "author@example.com");
        User owner = createTestUser(2L, "Owner", "owner@example.com");
        Item item = createTestItem(1L, "Item", "Desc", true, owner, null);
        Comment comment = new Comment();
        comment.setText("Text");
        comment.setItem(item);
        comment.setAuthor(author);
        Comment savedComment = new Comment();
        savedComment.setId(1L);
        savedComment.setText("Text");
        savedComment.setItem(item);
        savedComment.setAuthor(author);
        savedComment.setCreated(LocalDateTime.now());

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(userService.getUser(1L)).thenReturn(author);
        when(bookingRepository.existsByItemIdAndBookerIdAndEndBefore(eq(1L), eq(1L), any()))
                .thenReturn(true);
        when(commentRepository.save(comment)).thenReturn(savedComment);

        Comment result = itemService.addComment(comment, 1L, 1L);

        assertEquals(savedComment, result);
        assertNotNull(result.getCreated());
    }

    @Test
    void addComment_shouldThrowWhenUserDidNotBookItem() {
        User author = createTestUser(1L, "Author", "author@example.com");
        Item item = createTestItem(1L, "Item", "Desc", true,
                createTestUser(2L, "Owner", "owner@example.com"), null);

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(userService.getUser(1L)).thenReturn(author);
        when(bookingRepository.existsByItemIdAndBookerIdAndEndBefore(eq(1L), eq(1L), any()))
                .thenReturn(false);

        assertThrows(ValidationException.class, () -> itemService.addComment(new Comment(), 1L, 1L));
    }
}