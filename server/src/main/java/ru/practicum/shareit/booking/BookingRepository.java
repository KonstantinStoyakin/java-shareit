package ru.practicum.shareit.booking;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    @Query("SELECT b FROM Booking b " +
            "WHERE b.item.id = :itemId " +
            "AND b.start < :now " +
            "AND b.status = 'APPROVED' " +
            "ORDER BY b.start DESC")
    List<Booking> findLastBooking(@Param("itemId") Long itemId, @Param("now") LocalDateTime now);

    @Query("SELECT b FROM Booking b " +
            "WHERE b.item.id = :itemId " +
            "AND b.start > :now " +
            "AND b.status = 'APPROVED' " +
            "ORDER BY b.start ASC")
    List<Booking> findNextBooking(@Param("itemId") Long itemId, @Param("now") LocalDateTime now);

    @Query("SELECT COUNT(b) > 0 FROM Booking b " +
            "WHERE b.item.id = :itemId " +
            "AND b.booker.id = :bookerId " +
            "AND b.end < :now")
    boolean existsByItemIdAndBookerIdAndEndBefore(
            @Param("itemId") Long itemId,
            @Param("bookerId") Long bookerId,
            @Param("now") LocalDateTime now);

    List<Booking> findByBookerId(Long bookerId, Pageable pageable);

    List<Booking> findByBookerIdAndStartBeforeAndEndAfter(
            Long bookerId, LocalDateTime start, LocalDateTime end, Pageable pageable);

    List<Booking> findByBookerIdAndEndBefore(
            Long bookerId, LocalDateTime end, Pageable pageable);

    List<Booking> findByBookerIdAndStartAfter(
            Long bookerId, LocalDateTime start, Pageable pageable);

    List<Booking> findByBookerIdAndStatus(
            Long bookerId, BookingStatus status, Pageable pageable);

    List<Booking> findByItemOwnerId(Long ownerId, Pageable pageable);

    List<Booking> findByItemOwnerIdAndStartBeforeAndEndAfter(
            Long ownerId, LocalDateTime start, LocalDateTime end, Pageable pageable);

    List<Booking> findByItemOwnerIdAndEndBefore(
            Long ownerId, LocalDateTime end, Pageable pageable);

    List<Booking> findByItemOwnerIdAndStartAfter(
            Long ownerId, LocalDateTime start, Pageable pageable);

    List<Booking> findByItemOwnerIdAndStatus(
            Long ownerId, BookingStatus status, Pageable pageable);

    @Query("SELECT COUNT(b) > 0 FROM Booking b " +
            "WHERE b.item.id = :itemId " +
            "AND b.status = 'APPROVED' " +
            "AND ((b.start BETWEEN :start AND :end) OR " +
            "     (b.end BETWEEN :start AND :end) OR " +
            "     (b.start <= :start AND b.end >= :end))")
    boolean existsByItemIdAndTimeRange(
            @Param("itemId") Long itemId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
}