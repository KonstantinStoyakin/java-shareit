package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemDto {
    private Long id;

    private String name;

    private String description;

    private Boolean available;

    private BookingShort lastBooking;

    private BookingShort nextBooking;

    private List<CommentDto> comments;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BookingShort {
        private Long id;

        private Long bookerId;
    }
}
