package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ItemMapper {
    private final CommentMapper commentMapper;

    public ItemDto toDto(Item item) {
        ItemDto itemDto = new ItemDto();
        itemDto.setId(item.getId());
        itemDto.setName(item.getName());
        itemDto.setDescription(item.getDescription());
        itemDto.setAvailable(item.getAvailable());

        if (item.getRequest() != null) {
            itemDto.setRequestId(item.getRequest().getId());
        }

        if (item.getComments() != null) {
            itemDto.setComments(item.getComments().stream()
                    .map(commentMapper::toDto)
                    .collect(Collectors.toList()));
        }

        return itemDto;
    }

    public Item toItem(ItemDto itemDto) {
        Item item = new Item();
        item.setId(itemDto.getId());
        item.setName(itemDto.getName());
        item.setDescription(itemDto.getDescription());
        item.setAvailable(itemDto.getAvailable());

        if (itemDto.getRequestId() != null) {
            ItemRequest request = new ItemRequest();
            request.setId(itemDto.getRequestId());
            item.setRequest(request);
        }

        return item;
    }

    public CommentDto toCommentDto(Comment comment) {
        return commentMapper.toDto(comment);
    }
}