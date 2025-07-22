package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Validated
public class BookingController {
    private final BookingClient bookingClient;

    @PostMapping
    public ResponseEntity<Object> create(
            @RequestHeader("X-Sharer-User-Id") long userId,
            @RequestBody @Valid BookItemRequestDto requestDto) {
        return bookingClient.bookItem(userId, requestDto);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> approve(
            @RequestHeader("X-Sharer-User-Id") long userId,
            @PathVariable long bookingId,
            @RequestParam boolean approved) {
        return bookingClient.approveBooking(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getById(
            @RequestHeader("X-Sharer-User-Id") long userId,
            @PathVariable long bookingId) {
        return bookingClient.getBooking(userId, bookingId);
    }

    @GetMapping
    public ResponseEntity<Object> getAllByUser(
            @RequestHeader("X-Sharer-User-Id") long userId,
            @RequestParam(name = "state", defaultValue = "ALL") String stateParam,
            @PositiveOrZero @RequestParam(defaultValue = "0") Integer from,
            @Positive @RequestParam(defaultValue = "10") Integer size) {
        BookingState state = BookingState.from(stateParam)
                .orElseThrow(() -> new IllegalArgumentException("Unknown state: " + stateParam));
        return bookingClient.getBookings(userId, state, from, size);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getAllByOwner(
            @RequestHeader("X-Sharer-User-Id") long userId,
            @RequestParam(name = "state", defaultValue = "ALL") String stateParam,
            @PositiveOrZero @RequestParam(defaultValue = "0") Integer from,
            @Positive @RequestParam(defaultValue = "10") Integer size) {
        BookingState state = BookingState.from(stateParam)
                .orElseThrow(() -> new IllegalArgumentException("Unknown state: " + stateParam));
        return bookingClient.getOwnerBookings(userId, state, from, size);
    }
}