package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import ru.practicum.shareit.TestConfig;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Import({TestConfig.class, ItemRequestServiceImpl.class, ItemRequestMapper.class, ItemMapper.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ItemRequestServiceImplIntegrationTest {

    @Autowired
    private ItemRequestService itemRequestService;

    @Autowired
    private TestEntityManager em;

    @Autowired
    private ItemRequestRepository itemRequestRepository;

    private User requester;
    private User owner;
    private ItemRequest request;

    @BeforeEach
    void setUp() {
        requester = new User();
        requester.setName("Requester");
        requester.setEmail("requester@example.com");
        em.persist(requester);

        owner = new User();
        owner.setName("Owner");
        owner.setEmail("owner@example.com");
        em.persist(owner);

        request = new ItemRequest();
        request.setDescription("Need item for testing");
        request.setRequester(requester);
        request.setCreated(LocalDateTime.now());
    }

    @Test
    void createRequest_shouldSaveRequestToDatabase() {
        ItemRequestDto createdRequest = itemRequestService.createRequest(
                requester.getId(), "Need item for testing");

        assertNotNull(createdRequest.getId());
        assertEquals("Need item for testing", createdRequest.getDescription());

        ItemRequest fromDb = em.find(ItemRequest.class, createdRequest.getId());
        assertEquals(createdRequest.getId(), fromDb.getId());
    }

    @Test
    void getUserRequests_shouldReturnRequestsForUser() {
        ItemRequest savedRequest = itemRequestRepository.save(request);

        Item item = new Item();
        item.setName("Test Item");
        item.setDescription("For testing");
        item.setAvailable(true);
        item.setOwner(owner);
        item.setRequest(savedRequest);
        em.persist(item);

        List<ItemRequestDto> requests = itemRequestService.getUserRequests(requester.getId());

        assertEquals(1, requests.size());
        assertEquals(savedRequest.getDescription(), requests.get(0).getDescription());
        assertEquals(1, requests.get(0).getItems().size());
    }

    @Test
    void getOtherUsersRequests_shouldReturnRequestsFromOtherUsers() {
        itemRequestRepository.save(request);

        User anotherUser = new User();
        anotherUser.setName("Another");
        anotherUser.setEmail("another@example.com");
        em.persist(anotherUser);

        List<ItemRequestDto> requests = itemRequestService.getOtherUsersRequests(
                anotherUser.getId(), 0, 10);

        assertEquals(1, requests.size());
        assertEquals(request.getDescription(), requests.get(0).getDescription());
    }

    @Test
    void getRequestById_shouldReturnRequestWithItems() {
        ItemRequest savedRequest = itemRequestRepository.save(request);

        Item item = new Item();
        item.setName("Test Item");
        item.setDescription("For testing");
        item.setAvailable(true);
        item.setOwner(owner);
        item.setRequest(savedRequest);
        em.persist(item);

        ItemRequestDto foundRequest = itemRequestService.getRequestById(
                owner.getId(), savedRequest.getId());

        assertEquals(savedRequest.getId(), foundRequest.getId());
        assertEquals(1, foundRequest.getItems().size());
    }

    @Test
    void createRequest_shouldThrowWhenUserNotFound() {
        assertThrows(NotFoundException.class,
                () -> itemRequestService.createRequest(999L, "Description"));
    }

    @Test
    void getRequestById_shouldThrowWhenRequestNotFound() {
        assertThrows(NotFoundException.class,
                () -> itemRequestService.getRequestById(requester.getId(), 999L));
    }

    @Test
    void getOtherUsersRequests_shouldReturnEmptyWhenNoOtherRequests() {
        List<ItemRequestDto> requests = itemRequestService.getOtherUsersRequests(
                requester.getId(), 0, 10);

        assertTrue(requests.isEmpty());
    }

    @Test
    void getRequestById_shouldReturnEmptyItemsWhenNoItems() {
        ItemRequest savedRequest = itemRequestRepository.save(request);

        ItemRequestDto foundRequest = itemRequestService.getRequestById(
                owner.getId(), savedRequest.getId());

        assertTrue(foundRequest.getItems().isEmpty());
    }
}