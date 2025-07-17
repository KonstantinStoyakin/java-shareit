package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import ru.practicum.shareit.TestConfig;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Import({TestConfig.class, ItemServiceImpl.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ItemServiceImplIntegrationTest {

    @Autowired
    private ItemService itemService;

    @Autowired
    private TestEntityManager em;

    @Autowired
    private ItemRepository itemRepository;

    private User owner;
    private Item item;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setName("Owner");
        owner.setEmail("owner@example.com");
        em.persist(owner);

        item = new Item();
        item.setName("Test Item");
        item.setDescription("Test Description");
        item.setAvailable(true);
        item.setOwner(owner);
    }

    @Test
    void addItem_shouldSaveItemWithOwner() {
        Item savedItem = itemService.addItem(item, owner.getId());

        assertNotNull(savedItem.getId());
        assertEquals(owner.getId(), savedItem.getOwner().getId());
        assertEquals("Test Item", savedItem.getName());

        Item fromDb = em.find(Item.class, savedItem.getId());
        assertEquals(savedItem, fromDb);
    }

    @Test
    void updateItem_shouldUpdateItemFields() {
        Item savedItem = itemRepository.save(item);

        Item updates = new Item();
        updates.setId(savedItem.getId());
        updates.setName("Updated Name");
        updates.setDescription("Updated Description");

        Item updatedItem = itemService.updateItem(updates, owner.getId());

        assertEquals(savedItem.getId(), updatedItem.getId());
        assertEquals("Updated Name", updatedItem.getName());
        assertEquals("Updated Description", updatedItem.getDescription());
        assertEquals(savedItem.getAvailable(), updatedItem.getAvailable());
    }

    @Test
    void getItem_shouldReturnItemFromDatabase() {
        Item savedItem = itemRepository.save(item);

        Item foundItem = itemService.getItem(savedItem.getId(), owner.getId());

        assertEquals(savedItem, foundItem);
    }

    @Test
    void getUserItems_shouldReturnItemsForOwner() {
        itemRepository.save(item);

        Item item2 = new Item();
        item2.setName("Item 2");
        item2.setDescription("Desc 2");
        item2.setAvailable(true);
        item2.setOwner(owner);
        itemRepository.save(item2);

        List<Item> items = itemService.getUserItems(owner.getId(), 0, 10);

        assertEquals(2, items.size());
        assertTrue(items.stream().anyMatch(i -> i.getName().equals("Test Item")));
        assertTrue(items.stream().anyMatch(i -> i.getName().equals("Item 2")));
    }

    @Test
    void searchItems_shouldReturnAvailableItemsContainingText() {
        itemRepository.save(item);

        Item unavailableItem = new Item();
        unavailableItem.setName("Unavailable Item");
        unavailableItem.setDescription("Unavailable Description");
        unavailableItem.setAvailable(false);
        unavailableItem.setOwner(owner);
        itemRepository.save(unavailableItem);

        List<Item> foundItems = itemService.searchItems("test");

        assertEquals(1, foundItems.size());
        assertEquals(item.getName(), foundItems.get(0).getName());
    }

    @Test
    void addItem_shouldThrowWhenOwnerNotFound() {
        assertThrows(NotFoundException.class, () -> itemService.addItem(item, 999L));
    }

    @Test
    void updateItem_shouldThrowWhenNotOwner() {
        User owner = new User();
        owner.setName("Owner");
        owner.setEmail("owner2@example.com");
        em.persist(owner);

        User anotherUser = new User();
        anotherUser.setName("Another");
        anotherUser.setEmail("another2@example.com");
        em.persist(anotherUser);

        Item item = new Item();
        item.setName("Original");
        item.setDescription("Original description");
        item.setAvailable(true);
        item.setOwner(owner);
        em.persist(item);

        em.flush();

        Item updates = new Item();
        updates.setId(item.getId());
        updates.setName("Updated");

        assertThrows(NotFoundException.class,
                () -> itemService.updateItem(updates, anotherUser.getId()));
    }


    @Test
    void addItem_shouldThrowWhenValidationFails() {
        Item invalidItem = new Item();
        invalidItem.setName("");
        invalidItem.setDescription("");
        invalidItem.setAvailable(null);
        invalidItem.setOwner(owner);

        assertThrows(ValidationException.class,
                () -> itemService.addItem(invalidItem, owner.getId()));
    }
}
