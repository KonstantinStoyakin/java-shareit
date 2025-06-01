package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final Map<Long, Item> items = new HashMap<>();
    private Long idCounter = 1L;

    private final UserService userService;

    @Override
    public Item addItem(Item item) {
        if (item.getOwnerId() == null || userService.getUser(item.getOwnerId()) == null) {
            throw new NotFoundException("Пользователь с ID " + item.getOwnerId() + " не найден");
        }
        if (item.getName() == null || item.getName().isBlank()) {
            throw new ValidationException("Название не может быть пустым");
        }
        if (item.getDescription() == null || item.getDescription().isBlank()) {
            throw new ValidationException("Описание не может быть пустым");
        }
        if (item.getAvailable() == null) {
            throw new ValidationException("Статус доступности должен быть указан");
        }

        item.setId(idCounter++);
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public Item updateItem(Item item) {
        if (!items.containsKey(item.getId())) {
            throw new NotFoundException("Вещь не найдена");
        }

        Item existingItem = items.get(item.getId());

        if (!existingItem.getOwnerId().equals(item.getOwnerId())) {
            throw new NotFoundException("Обновлять вещь может только владелец");
        }

        if (item.getName() != null) {
            if (item.getName().isBlank()) {
                throw new ValidationException("Название не может быть пустым");
            }
            existingItem.setName(item.getName());
        }

        if (item.getDescription() != null) {
            if (item.getDescription().isBlank()) {
                throw new ValidationException("Описание не может быть пустым");
            }
            existingItem.setDescription(item.getDescription());
        }

        if (item.getAvailable() != null) {
            existingItem.setAvailable(item.getAvailable());
        }

        return existingItem;
    }

    @Override
    public Item getItem(Long itemId) {
        if (!items.containsKey(itemId)) {
            throw new NotFoundException("Вещь не найдена");
        }
        return items.get(itemId);
    }

    @Override
    public List<Item> getUserItems(Long userId) {
        return items.values().stream()
                .filter(item -> item.getOwnerId().equals(userId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Item> searchItems(String text) {
        if (text == null || text.isBlank()) {
            return new ArrayList<>();
        }

        String searchText = text.toLowerCase();
        return items.values().stream()
                .filter(item -> Boolean.TRUE.equals(item.getAvailable()))
                .filter(item -> item.getName() != null &&
                        item.getName().toLowerCase().contains(searchText) ||
                        (item.getDescription() != null &&
                                item.getDescription().toLowerCase().contains(searchText)))
                .collect(Collectors.toList());
    }
}
