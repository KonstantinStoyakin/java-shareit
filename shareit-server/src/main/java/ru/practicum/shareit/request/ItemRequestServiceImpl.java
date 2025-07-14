package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository itemRequestRepository;

    private final UserRepository userRepository;

    private final ItemRepository itemRepository;

    private final ItemRequestMapper itemRequestMapper;

    private final ItemMapper itemMapper;

    @Override
    public ItemRequestDto createRequest(Long userId, String description) {
        User requester = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        ItemRequest request = new ItemRequest();
        request.setDescription(description);
        request.setRequester(requester);
        request.setCreated(LocalDateTime.now());

        ItemRequest savedRequest = itemRequestRepository.save(request);
        return itemRequestMapper.toDto(savedRequest);
    }

    @Override
    public List<ItemRequestDto> getUserRequests(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User not found");
        }

        return itemRequestRepository.findAllByRequesterIdOrderByCreatedDesc(userId).stream()
                .map(request -> {
                    List<ItemDto> items = itemRepository.findAllByRequestId(request.getId()).stream()
                            .map(itemMapper::toDto)
                            .collect(Collectors.toList());
                    ItemRequestDto dto = itemRequestMapper.toDto(request);
                    dto.setItems(items);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemRequestDto> getOtherUsersRequests(Long userId, Integer from, Integer size) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User not found");
        }

        PageRequest page = PageRequest.of(from / size, size, Sort.by(Sort.Direction.DESC, "created"));
        return itemRequestRepository.findAllByRequesterIdNotOrderByCreatedDesc(userId, page).stream()
                .map(request -> {
                    List<ItemDto> items = itemRepository.findAllByRequestId(request.getId()).stream()
                            .map(itemMapper::toDto)
                            .collect(Collectors.toList());
                    ItemRequestDto dto = itemRequestMapper.toDto(request);
                    dto.setItems(items);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public ItemRequestDto getRequestById(Long userId, Long requestId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User not found");
        }

        ItemRequest request = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Request not found"));

        List<Item> items = itemRepository.findAllByRequestId(requestId);

        ItemRequestDto dto = itemRequestMapper.toDto(request);
        dto.setItems(items.stream()
                .map(itemMapper::toDto)
                .collect(Collectors.toList()));

        return dto;
    }
}