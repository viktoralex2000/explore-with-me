package ru.practicum.ewm.comment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RejectCommentRequest {

    @NotBlank(message = "Rejection reason cannot be empty")
    private String rejectionReason;
}