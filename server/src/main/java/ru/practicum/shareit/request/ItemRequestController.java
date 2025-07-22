package ru.practicum.shareit.request;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.UserService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
public class ItemRequestController {
    private final ItemRequestService itemRequestService;

    private final UserService userService;

    @PostMapping
    public ItemRequestDto createRequest(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @RequestBody Map<String, String> requestBody) {
        userService.getUser(userId);
        String description = requestBody.get("description");
        if (description == null || description.isBlank()) {
            throw new ValidationException("Description cannot be blank");
        }
        return itemRequestService.createRequest(userId, description);
    }


    @GetMapping
    public List<ItemRequestDto> getUserRequests(
            @RequestHeader("X-Sharer-User-Id") Long userId) {

        userService.getUser(userId);

        return itemRequestService.getUserRequests(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> getOtherUsersRequests(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @PositiveOrZero @RequestParam(defaultValue = "0") Integer from,
            @Positive @RequestParam(defaultValue = "10") Integer size) {
        userService.getUser(userId);
        return itemRequestService.getOtherUsersRequests(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ItemRequestDto getRequestById(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @PathVariable Long requestId) {
        userService.getUser(userId);
        return itemRequestService.getRequestById(userId, requestId);
    }
}
