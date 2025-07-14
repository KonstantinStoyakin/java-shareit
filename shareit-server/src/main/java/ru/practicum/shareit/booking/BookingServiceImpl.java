package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;

    private final UserService userService;

    private final ItemService itemService;

    @Override
    public Booking addBooking(Booking booking, Long userId) {
        User booker = userService.getUser(userId);
        Item item = itemService.getItem(booking.getItem().getId(), userId);

        if (!item.getAvailable()) {
            throw new ValidationException("Item is not available for booking");
        }

        if (item.getOwner().getId().equals(userId)) {
            throw new NotFoundException("Owner cannot book own item");
        }

        if (bookingRepository.existsByItemIdAndTimeRange(
                booking.getItem().getId(),
                booking.getStart(),
                booking.getEnd())) {
            throw new ValidationException("Item is already booked for this time period");
        }

        validateBookingDates(booking);

        booking.setBooker(booker);
        booking.setItem(item);
        booking.setStatus(BookingStatus.WAITING);

        return bookingRepository.save(booking);
    }

    @Override
    public Booking approveBooking(Long bookingId, Long userId, boolean approved) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking not found"));

        if (!booking.getItem().getOwner().getId().equals(userId)) {
            throw new ForbiddenException("Only owner can approve booking");
        }

        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new ValidationException("Booking already approved/rejected");
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        return bookingRepository.save(booking);
    }

    @Override
    public Booking getBooking(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking not found"));

        if (!booking.getBooker().getId().equals(userId) && !booking.getItem().getOwner().getId().equals(userId)) {
            throw new NotFoundException("Only booker or owner can view booking");
        }

        return booking;
    }

    @Override
    public List<Booking> getUserBookings(Long userId, String state, Integer from, Integer size) {
        userService.getUser(userId);
        LocalDateTime now = LocalDateTime.now();
        PageRequest page = PageRequest.of(from / size, size, Sort.by(Sort.Direction.DESC, "start"));

        switch (state) {
            case "ALL":
                return bookingRepository.findByBookerId(userId, page);
            case "CURRENT":
                return bookingRepository.findByBookerIdAndStartBeforeAndEndAfter(
                        userId, now, now, page);
            case "PAST":
                return bookingRepository.findByBookerIdAndEndBefore(
                        userId, now, page);
            case "FUTURE":
                return bookingRepository.findByBookerIdAndStartAfter(
                        userId, now, page);
            case "WAITING":
                return bookingRepository.findByBookerIdAndStatus(
                        userId, BookingStatus.WAITING, page);
            case "REJECTED":
                return bookingRepository.findByBookerIdAndStatus(
                        userId, BookingStatus.REJECTED, page);
            default:
                throw new ValidationException("Unknown state: " + state);
        }
    }

    @Override
    public List<Booking> getOwnerBookings(Long userId, String state, Integer from, Integer size) {
        userService.getUser(userId);
        LocalDateTime now = LocalDateTime.now();
        PageRequest page = PageRequest.of(from / size, size, Sort.by(Sort.Direction.DESC, "start"));

        switch (state) {
            case "ALL":
                return bookingRepository.findByItemOwnerId(userId, page);
            case "CURRENT":
                return bookingRepository.findByItemOwnerIdAndStartBeforeAndEndAfter(
                        userId, now, now, page);
            case "PAST":
                return bookingRepository.findByItemOwnerIdAndEndBefore(
                        userId, now, page);
            case "FUTURE":
                return bookingRepository.findByItemOwnerIdAndStartAfter(
                        userId, now, page);
            case "WAITING":
                return bookingRepository.findByItemOwnerIdAndStatus(
                        userId, BookingStatus.WAITING, page);
            case "REJECTED":
                return bookingRepository.findByItemOwnerIdAndStatus(
                        userId, BookingStatus.REJECTED, page);
            default:
                throw new ValidationException("Unknown state: " + state);
        }
    }

    @Override
    public void validateBookingDates(Booking booking) {
        if (booking.getStart() == null || booking.getEnd() == null) {
            throw new ValidationException("Start and end dates must be specified");
        }

        if (booking.getStart().isAfter(booking.getEnd())) {
            throw new ValidationException("Start date must be before end date");
        }

        if (booking.getStart().equals(booking.getEnd())) {
            throw new ValidationException("Start and end dates cannot be equal");
        }

        if (booking.getStart().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Start date must be in the future");
        }

        if (booking.getEnd().isBefore(LocalDateTime.now())) {
            throw new ValidationException("End date must be in the future");
        }
    }
}
