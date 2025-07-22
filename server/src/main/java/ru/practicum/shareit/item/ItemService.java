package ru.practicum.shareit.item;

import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemService {
    Item addItem(Item item, Long ownerId);

    Item updateItem(Item item, Long ownerId);

    Item getItem(Long itemId, Long userId);

    Comment addComment(Comment comment, Long itemId, Long userId);

    List<Item> getUserItems(Long userId, Integer from, Integer size);

    List<Item> searchItems(String text);
}
