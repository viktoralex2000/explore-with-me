package ru.practicum.ewm.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateCommentDto {

    @NotBlank(message = "Comment text cannot be empty")
    @Size(min = 1, max = 2000, message = "Comment length must be between 1 and 2000 characters")
    private String text;
}