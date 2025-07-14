package ru.practicum.shareit.request;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.stream.Collectors;

@Component
public class ItemRequestMapper {
    private final ItemMapper itemMapper;

    public ItemRequestMapper(ItemMapper itemMapper) {
        this.itemMapper = itemMapper;
    }

    public ItemRequestDto toDto(ItemRequest request) {
        ItemRequestDto dto = new ItemRequestDto();
        dto.setId(request.getId());
        dto.setDescription(request.getDescription());
        dto.setCreated(request.getCreated());

        if (request.getItems() != null) {
            dto.setItems(request.getItems().stream()
                    .map(itemMapper::toDto)
                    .collect(Collectors.toList()));
        }

        return dto;
    }
}