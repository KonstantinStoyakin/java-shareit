package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingDto {
    private Long id;

    @FutureOrPresent(message = "The start date must be in the present or future")
    private LocalDateTime start;

    @Future(message = "The end date must be in the future")
    private LocalDateTime end;

    private Long itemId;
}