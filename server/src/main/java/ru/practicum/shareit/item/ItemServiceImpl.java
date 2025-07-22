package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
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
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;

    private final UserService userService;

    private final BookingRepository bookingRepository;

    private final CommentRepository commentRepository;

    private final ItemRequestRepository itemRequestRepository;

    @Override
    public Item addItem(Item item, Long ownerId) {
        User owner = userService.getUser(ownerId);
        item.setOwner(owner);
        validateItem(item);

        if (item.getRequest() != null && item.getRequest().getId() != null) {
            Long requestId = item.getRequest().getId();
            ItemRequest request = itemRequestRepository.findById(requestId)
                    .orElseThrow(() -> new NotFoundException("Request not found"));
            item.setRequest(request);
        }

        return itemRepository.save(item);
    }

    @Override
    public Item updateItem(Item item, Long ownerId) {
        Item existingItem = itemRepository.findById(item.getId())
                .orElseThrow(() -> new NotFoundException("Item not found"));

        if (!existingItem.getOwner().getId().equals(ownerId)) {
            throw new NotFoundException("Only owner can update item");
        }

        if (item.getName() != null) {
            existingItem.setName(item.getName());
        }

        if (item.getDescription() != null) {
            existingItem.setDescription(item.getDescription());
        }

        if (item.getAvailable() != null) {
            existingItem.setAvailable(item.getAvailable());
        }

        return itemRepository.save(existingItem);
    }

    @Override
    public Item getItem(Long itemId, Long userId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item not found"));

        if (item.getOwner().getId().equals(userId)) {
            LocalDateTime now = LocalDateTime.now();
            item.setLastBooking(bookingRepository.findLastBooking(itemId, now).stream().findFirst().orElse(null));
            item.setNextBooking(bookingRepository.findNextBooking(itemId, now).stream().findFirst().orElse(null));
        }

        item.setComments(commentRepository.findByItemId(itemId));
        return item;
    }

    @Override
    public List<Item> getUserItems(Long userId, Integer from, Integer size) {
        PageRequest page = PageRequest.of(from / size, size);
        List<Item> items = itemRepository.findByOwnerId(userId, page);
        LocalDateTime now = LocalDateTime.now();

        return items.stream().peek(item -> {
            item.setLastBooking(bookingRepository.findLastBooking(item.getId(), now)
                    .stream().findFirst().orElse(null));
            item.setNextBooking(bookingRepository.findNextBooking(item.getId(), now)
                    .stream().findFirst().orElse(null));
            item.setComments(commentRepository.findByItemId(item.getId()));
        }).collect(Collectors.toList());
    }

    @Override
    public List<Item> searchItems(String text) {
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }
        return itemRepository.searchAvailableItems(text);
    }

    @Override
    public Comment addComment(Comment comment, Long itemId, Long userId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item not found"));

        User author = userService.getUser(userId);

        if (!bookingRepository.existsByItemIdAndBookerIdAndEndBefore(itemId, userId, LocalDateTime.now())) {
            throw new ValidationException("User didn't book this item");
        }

        comment.setItem(item);
        comment.setAuthor(author);
        comment.setCreated(LocalDateTime.now());

        return commentRepository.save(comment);
    }

    private void validateItem(Item item) {
        if (item.getName() == null || item.getName().isBlank()) {
            throw new ValidationException("Name cannot be blank");
        }
        if (item.getDescription() == null || item.getDescription().isBlank()) {
            throw new ValidationException("Description cannot be blank");
        }
        if (item.getAvailable() == null) {
            throw new ValidationException("Available status cannot be null");
        }
    }
}