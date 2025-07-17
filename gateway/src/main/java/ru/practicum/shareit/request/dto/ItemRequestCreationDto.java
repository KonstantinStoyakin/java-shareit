package ru.practicum.shareit.request.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ItemRequestCreationDto {
    @NotBlank(message = "Description cannot be empty")
    private String description;
}
