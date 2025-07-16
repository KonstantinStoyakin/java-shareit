package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

    @Mock
    private UserService userService;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ItemServiceImpl itemService;

    @Test
    void addItem_whenNameIsBlank_shouldThrowValidationException() {
        User owner = new User(1L, "Owner", "owner@example.com");
        when(userService.getUser(anyLong())).thenReturn(owner);

        Item item = new Item();
        item.setName("");
        item.setDescription("Description");
        item.setAvailable(true);
        item.setOwner(owner);

        assertThrows(ValidationException.class, () -> itemService.addItem(item, 1L));
    }

    @Test
    void addItem_whenDescriptionIsBlank_shouldThrowValidationException() {
        User owner = new User(1L, "Owner", "owner@example.com");
        when(userService.getUser(anyLong())).thenReturn(owner);

        Item item = new Item();
        item.setName("Name");
        item.setDescription("");
        item.setAvailable(true);
        item.setOwner(owner);

        assertThrows(ValidationException.class, () -> itemService.addItem(item, 1L));
    }

    @Test
    void addItem_whenAvailableIsNull_shouldThrowValidationException() {
        User owner = new User(1L, "Owner", "owner@example.com");
        when(userService.getUser(anyLong())).thenReturn(owner);

        Item item = new Item();
        item.setName("Name");
        item.setDescription("Description");
        item.setAvailable(null);
        item.setOwner(owner);

        assertThrows(ValidationException.class, () -> itemService.addItem(item, 1L));
    }
}