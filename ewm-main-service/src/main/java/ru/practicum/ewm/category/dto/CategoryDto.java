package ru.practicum.ewm.category.dto;

import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Getter
@Setter
public class CategoryDto {

    private Long id;

    @NotBlank
    @Size(max = 50)
    private String name;
}