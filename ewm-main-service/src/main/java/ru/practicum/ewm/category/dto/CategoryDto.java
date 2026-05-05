package ru.practicum.ewm.category.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
public class CategoryDto {

    private Long id;

    @NotBlank
    @Size(max = 50)
    private String name;
}